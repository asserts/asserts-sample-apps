import logging
import os
from datetime import datetime
from random import Random

import requests
from flask import Flask, request
from flask_sqlalchemy import SQLAlchemy
from json_log_formatter import JSONFormatter
from kubernetes import config, client
from prometheus_client import Gauge, Counter
from prometheus_flask_exporter import PrometheusMetrics
from flask_apscheduler import APScheduler
from pytz import timezone

app = Flask(__name__)
metrics = PrometheusMetrics(app)
random = Random(1)

build_info = Gauge('karte_build_info', 'Build information',
                   ['branch', 'revision', 'version'])
db_counter = Counter('postgres_calls', 'Calls to Postgres',
                     ['dst_workload', 'dst_namespace'])


log_formatter = JSONFormatter()
log_handler = logging.StreamHandler()
log_handler.setFormatter(log_formatter)
# noinspection PyArgumentList
logging.basicConfig(level=logging.ERROR, handlers=[log_handler])

scheduler = APScheduler()
scheduler.init_app(app)
scheduler.start()

GOOD_BUILD_INFO = ['HEAD', '3e86781f2880e95781aab292a78906572b49d073', '1.0.0']
BAD_BUILD_INFO = ['HEAD', 'd3421337a09132240d7056ed7a2657c930c28975', '1.0.1']


def set_use_extra_database_calls(extra_db_calls):
    global use_extra_db_queries
    use_extra_db_queries = extra_db_calls

    if use_extra_db_queries:
        if build_info.labels(*GOOD_BUILD_INFO):
            build_info.remove(*GOOD_BUILD_INFO)
        build_info.labels(*BAD_BUILD_INFO).set(1)
    else:
        if build_info.labels(*BAD_BUILD_INFO):
            build_info.remove(*BAD_BUILD_INFO)
        build_info.labels(*GOOD_BUILD_INFO).set(1)


@scheduler.task('interval', minutes=1)
def update_database_settings():
    if postgres_service:
        utc_now = datetime.utcnow()
        us_pacific_tz = timezone('US/Pacific')
        local_time = utc_now.astimezone(us_pacific_tz)

        # Send excessive database calls between 9:30 and 10am pacific time.
        extra_db_calls = local_time.hour == 9 and local_time.minute >= 30
        set_use_extra_database_calls(extra_db_calls)


postgres_service = os.getenv('POSTGRES_SERVICE')
if postgres_service:
    postgres_user = os.getenv('POSTGRES_USERNAME')
    postgres_password = os.getenv('POSTGRES_PASSWORD')
    assert postgres_user and postgres_password
    app.config['SQLALCHEMY_DATABASE_URI'] = f'postgresql://{postgres_user}:{postgres_password}@{postgres_service}:5432'
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    update_database_settings()

db = SQLAlchemy(app)


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)


if postgres_service:
    db.create_all()
    print('Created database tables.')

use_extra_db_queries = False


# Map service endpoints:
@app.route('/map')
def load_maps():
    r = requests.get('http://user/user')
    assert r.status_code == 200
    _call_auth(int(os.getenv('AUTHV2_PERCENTAGE', default='0')))
    return ''


def _call_auth(authv2_percentage: int):
    assert authv2_percentage >= 0
    assert authv2_percentage <= 100
    endpoint = random.choices(['auth', 'authv2'], weights=[100 - authv2_percentage, authv2_percentage])[0]
    r = requests.get(f'http://{endpoint}/{endpoint}')
    assert r.status_code == 200


# User service endpoints:
@app.route('/user', methods=['GET', 'POST'])
def load_users():
    if request.method == 'POST':
        for _ in range(1000):
            db.session.add(User())
        db.session.commit()
        users = db.session.query(User).all()
        db.session.commit()
        return str(len(users))
    elif request.method == 'GET':
        users = _fetch_users()

        if use_extra_db_queries:
            for _ in range(200):
                _fetch_users()

        db.session.commit()

        return str(len(users))


def _fetch_users():
    users = db.session.query(User).all()
    db_counter.labels('karte-eks-postgresql', 'default').inc()
    return users


# Auth service endpoints:
@app.route('/auth')
def check_auth():
    return ''


# Authv2 service endpoints:
@app.route('/authv2')
def check_authv2():
    raise MemoryError('out of memory while handling request "/authv2"')


@app.route('/')
def main():
    return ''


@app.route('/error')
def error():
    return '', 500


# Periodically call other services to generate links between the services that aren't directly involved in the
# demo story to add complexity to the graph.
additional_services = os.getenv('ADDITIONAL_SERVICE_NAMES', default='').split(',')


@scheduler.task('interval', seconds=1)
def call_additional_services():
    service_name = random.choice(additional_services)
    if service_name:
        r = requests.get(f'http://{service_name}/')
        assert r.status_code == 200


should_call_map_service = os.getenv('CALL_MAP_SERVICE', default='') != ''
should_update_map_deployment = os.getenv('UPDATE_MAP_DEPLOYMENT', default='') != ''


@scheduler.task('interval', seconds=1)
def call_map_service():
    if should_call_map_service:
        requests.get(f'http://map/map')


@scheduler.task('interval', minutes=1)
def update_map_deployment():
    if should_update_map_deployment:
        config.load_incluster_config()
        v1 = client.AppsV1Api()
        map_deployment = v1.read_namespaced_deployment('map', 'default')

        auth_v2_env_var = _find_env_var_by_name(map_deployment, 'AUTHV2_PERCENTAGE')
        if auth_v2_env_var is None:
            print('Could not find AUTHV2_PERCENTAGE environment variable; ensure it is defined in map deployment.')
        else:
            desired_authv2_percentage = _calculate_desired_authv2_percentage()
            if int(auth_v2_env_var.value) != desired_authv2_percentage:
                auth_v2_env_var.value = str(desired_authv2_percentage)
                print(f'Updating map service authv2 percentage to {auth_v2_env_var.value}.')
                v1.patch_namespaced_deployment(name='map', namespace='default', body=map_deployment)


def _find_env_var_by_name(deployment, name):
    for env_var in deployment.spec.template.spec.containers[0].env:
        if env_var.name == name:
            return env_var
    return None


def _calculate_desired_authv2_percentage():
    utc_now = datetime.utcnow()
    us_pacific_tz = timezone('US/Pacific')
    local_time = utc_now.astimezone(us_pacific_tz)

    # Put the application in the problem state -- in which 10% of map service traffic is sent to the bad authv2
    # service -- between 10:30 and 11am.
    if local_time.hour == 10 and local_time.minute >= 30:
        return 10
    else:
        return 0


# The code below demonstrates various features of the Flask prometheus exporter.


# static information as metric
metrics.info('app_info', 'Application info', version='1.0.0')


@app.route('/skip')
@metrics.do_not_track()
def skip():
    pass  # default metrics are not collected


@app.route('/<item_type>')
@metrics.do_not_track()
@metrics.counter('invocation_by_type', 'Number of invocations by type',
                 labels={'item_type': lambda: request.view_args['type']})
def by_type(item_type):
    pass  # only the counter is collected, not the default metrics


@app.route('/long-running')
@metrics.gauge('in_progress', 'Long running requests in progress')
def long_running():
    pass


@app.route('/status/<int:status>')
@metrics.do_not_track()
@metrics.summary('requests_by_status', 'Request latencies by status',
                 labels={'status': lambda r: r.status_code})
@metrics.histogram('requests_by_status_and_path', 'Request latencies by status and path',
                   labels={'status': lambda r: r.status_code, 'path': lambda: request.path})
def echo_status(status):
    return 'Status: %s' % status, status
