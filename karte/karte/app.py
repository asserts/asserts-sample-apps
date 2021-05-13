import logging
import os
from random import Random

import requests
from flask import Flask, request
from json_log_formatter import JSONFormatter
from prometheus_flask_exporter import PrometheusMetrics
from flask_apscheduler import APScheduler

app = Flask(__name__)
metrics = PrometheusMetrics(app)
random = Random(1)

log_formatter = JSONFormatter()
log_handler = logging.StreamHandler()
log_handler.setFormatter(log_formatter)
# Turn on logging for outgoing requests to more easily tell where requests are going:
# noinspection PyArgumentList
logging.basicConfig(level=logging.DEBUG, handlers=[log_handler])

scheduler = APScheduler()
scheduler.init_app(app)
scheduler.start()


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
@app.route('/user')
def load_users():
    return ''


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
