FROM python:3.10-alpine

RUN apk add curl py3-numpy

ENV PYTHONPATH=/usr/lib/python3.10/site-packages

WORKDIR /app
ADD . /app
RUN pip install -r requirements.txt
