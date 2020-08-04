#! /bin/bash

pipenv sync
pipenv run gunicorn -c gunicorn_conf.py app:app