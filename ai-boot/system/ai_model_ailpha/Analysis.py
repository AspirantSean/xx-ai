# -*- coding:utf-8 -*-
from __future__ import division
import os
import json
import codecs
from random import randrange
import math
import pandas as pd
import numpy as np
from statsmodels.tsa.arima_model import ARMA
from statsmodels.tsa.api import ExponentialSmoothing, SimpleExpSmoothing, Holt
import sys

from anomaly import *
from subspace import AILPHA_RPCASST


# 读取配置文件
class Properties(object):

    def __init__(self, file_name):
        self.file_name = file_name
        self.properties = {}
        try:
            property_file = open(self.file_name, 'r')
            for line in property_file:
                line = line.strip()
                if line.find('=') > 0 and not line.startswith('#'):
                    strs = line.split('=')
                    self.properties[strs[0].strip()] = strs[1].strip()
        except Exception, e:
            print 'read properties error'
        finally:
            property_file.close()

    def has_key(self, key):
        return key in self.properties

    def get(self, key, default_value=''):
        if key in self.properties:
            return self.properties[key]
        return default_value


def choice_algorithm(algorithmId, parameter, ts):
    if algorithmId == 'ExponentialSmoothing':
        if parameter.get('ExponentialSmoothing.auto', 'true').lower() == 'true':
            return AILPHA_ExponentialSmoothing(ts, auto=True, confidenceLevelThreshold=0.9973, alpha=1)
        else:
            a1 = float(parameter.get(
                'ExponentialSmoothing.confidenceLevelThreshold', '0.9973'))
            a2 = float(parameter.get('ExponentialSmoothing.alpha', '1'))
            return AILPHA_ExponentialSmoothing(ts, auto=False, confidenceLevelThreshold=a1, alpha=a2)
    if algorithmId == 'WeeklyGaussianEstimation':
        if parameter.get('WeeklyGaussianEstimation.auto', 'true').lower() == 'true':
            return AILPHA_WeeklyGaussianEstimation(ts, stdThreshold=3)
        else:
            a1 = float(parameter.get(
                'WeeklyGaussianEstimation.stdThreshold', '3'))
            return AILPHA_WeeklyGaussianEstimation(ts, stdThreshold=a1)
    if algorithmId == 'ARIMA':
        if parameter.get('ARIMA.auto', 'true').lower() == 'true':
            return AILPHA_ARIMA(ts, auto=True, confidenceLevelThreshold=0.9973, p=1, q=1, d=0)
        else:
            a1 = float(parameter.get(
                'ARIMA.confidenceLevelThreshold', '0.9973'))
            a2 = float(parameter.get('ARIMA.p', '1'))
            a3 = float(parameter.get('ARIMA.q', '1'))
            a4 = float(parameter.get('ARIMA.d', '0'))
            return AILPHA_ARIMA(ts, auto=True, confidenceLevelThreshold=a1, p=a2, q=a3, d=a4)
    if algorithmId == 'RPCASST':
        if parameter.get('RPCASST.auto', 'true').lower() == 'true':
            return AILPHA_RPCASST(ts, detectionSensitivity=0)
        else:
            a1 = float(parameter.get('RPCASST.detectionSensitivity', '0'))
            return AILPHA_RPCASST(ts, detectionSensitivity=a1)


def generateData(startDate, periods, data_list):

    df = pd.DataFrame(data_list, columns=['value'], index=pd.date_range(startDate, periods=periods, freq='10min'))
    return df

if __name__ == '__main__':
    print 'PID: ' + str(os.getpid())
    algorithmId = sys.argv[1]
    filePath = sys.argv[2]
    startDate = sys.argv[3] + ' ' + sys.argv[4]
    periods = int(sys.argv[5])
    data_list = [float(x) for x in sys.argv[6:len(sys.argv)]]

    data = generateData(startDate, periods, data_list)

    ts = data['value']  # DataFrame转TimeSeries

    if not os.path.exists(filePath):
        os.makedirs(filePath)
        print '=====create folder success====='
    else:
        print '=====folder is already exist====='

    parameter = Properties(sys.argv[0].split('Analysis')[0] + 'config' + os.sep + 'ai_algorithm.properties')

    # 开始计算
    output_dict = choice_algorithm(algorithmId, parameter, ts)
    with codecs.open(filePath + os.sep + algorithmId + '.json', 'w', 'utf-8') as fout:
        outStr = json.dumps(output_dict, ensure_ascii=False, indent=4, separators=(',', ':'))
        fout.write(outStr)
        print os.path.abspath(fout.name)

    print '=====calculations complete====='
