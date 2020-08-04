# -*- coding: utf-8 -*-
import logging

from flask.logging import default_handler


def init_log(app):
    app.logger.removeHandler(default_handler)
    gunicorn_logger = logging.getLogger('gunicorn.error')
    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)
