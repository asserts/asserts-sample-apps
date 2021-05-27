FROM python:3.9.1-slim as python-base
RUN pip install boto3

FROM python-base
WORKDIR /app
COPY discoverecs.py /app
ENTRYPOINT ["python", "discoverecs.py", "--directory", "/app/ecs-sd-files"]
