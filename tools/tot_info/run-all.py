# 从 extract-to-json 获得的文件中运行程序，生成 json 格式的测试用例

import json
from subprocess import run, PIPE

def execf(inputStr):
    p=run(['../source/tot_info.exe'], stdout=PIPE, input=inputStr.encode('ascii'))
    return(p.stdout)

with open('input.json') as fr:
    ip=json.load(fr)

res=[]

for item in ip:
    print('running {}'.format(item['name']))
    res.append({
        'name': item['name'],
        "input": item['content'],
        'output': execf(item['content']).decode('ascii'),
    })

with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
