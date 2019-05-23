#!/bin/env python

"""
找到 defects4j 里各个版本中最小的测试用例数量和最大的测试用例数量
"""

import os
import json
import itertools

dir = '../target/outputs/defects4j-testcases'

files = os.listdir(dir)

def lenIter(j):
    inner=j['inner']
    for item in inner:
        yield len(item['testcases'])

for item in files:
    name = item[0:len(item)-5]
    with open(os.path.join(dir, item)) as fr:
        j=json.load(fr)

    mi=min(lenIter(j))
    ma=max(lenIter(j))

    print(f'{name} {mi}~{ma}')
