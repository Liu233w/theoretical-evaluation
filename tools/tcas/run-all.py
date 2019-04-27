# inputs 文件夹和 testplans 中获得输入，生成 json 格式的测试用例

import json
from subprocess import run, PIPE
from os import path

join = path.join

def execf(params):
    p=run(['./source/a.out']+params, stdout=PIPE)
    return(p.stdout)

with open('./testplans.alt/universe') as fr:
    plans = fr.readlines()

res=[]

for item in plans:
    print('running {}'.format(item))

    params = item.strip().split()

    res.append({
        'params': params,
        'output': execf(params).decode('ascii'),
    })

with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
