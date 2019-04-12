# inputs 文件夹和 testplans 中获得输入，生成 json 格式的测试用例

import json
from subprocess import run, PIPE
from os import path

join = path.join

def execf(params, inputStr):
    p=run(['./source/a.exe']+params, stdout=PIPE, input=inputStr.encode('ascii'))
    return(p.stdout)

def readInput(filename):
    basePath = './inputs'
    
    with open(join(basePath, filename)) as fr:
        return fr.read()
    
with open('./testplans.alt/universe') as fr:
    plans = fr.readlines()

res=[]

for item in plans:
    print('running {}'.format(item))
    
    splited = item.split('<')
    
    params = splited[0].strip().split()
    stdin = readInput(splited[1].strip())
    
    res.append({
        'name': item,
        'params': params,
        "input": stdin,
        'output': execf(params, stdin).decode('ascii'),
    })

with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
