# inputs 文件夹和 testplans 中获得输入，生成 json 格式的测试用例

"""
需要在 linux 下执行程序，因为这个的参数带有 * ，在 windows 下会被当成通配符。
在linux下执行时会绕过bash，就不用担心这个了。
"""

import json
from subprocess import run, PIPE
from os import path
from collections import OrderedDict

join = path.join

def execf(params):
    p=run(['./a.out']+params, stdout=PIPE)
    return(p.stdout)

with open('./data.txt') as fr:
    plans = fr.readlines()

res=[]

for item in plans:
    print('running {}'.format(item))

    params = item.strip().split()

    res.append(OrderedDict([
        ('name', item),
        ('params', params),
        ('output', execf(params).decode('ascii')),
    ]))

with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
