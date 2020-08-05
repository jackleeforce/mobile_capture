# -*- coding: utf-8 -*-
import time

from flask import Flask, make_response, jsonify
from flask_restful import Resource, Api, reqparse

import log

app = Flask(__name__)
api = Api(app)


class OperateResult:
    '''
    报文应答类。
    '''
    message = ''
    errorcode = 0

    def __init__(self, errorcode, message):
        self.errorcode = errorcode
        self.message = message

    def serialize(self):
        """Return object data in easily serializeable format"""
        return {
            'errorcode': self.errorcode,
            'message': self.message
        }


class OneWayAuth(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('time_format', required=False)
        args = parser.parse_args()

        time_format = args['time_format']

        if time_format is None:
            time_format = '%Y-%m-%d %H:%M:%S'

        localtime = time.strftime(time_format, time.localtime())

        result = OperateResult('OneWayAuth', localtime)

        return make_response(jsonify(result.serialize()), 200)


class TwoWayAuth(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('time_format', required=False)
        args = parser.parse_args()

        time_format = args['time_format']

        if time_format is None:
            time_format = '%Y%m%d%H%M%S'

        localtime = time.strftime(time_format, time.localtime())

        result = OperateResult('TwoWayAuth', localtime)

        return make_response(jsonify(result.serialize()), 200)


api.add_resource(OneWayAuth, '/mobile-capture/v1/one-way')
api.add_resource(TwoWayAuth, '/mobile-capture/v1/two-way')

if __name__ != '__main__':
    log.init_log(app)

if __name__ == '__main__':
    app.run()
