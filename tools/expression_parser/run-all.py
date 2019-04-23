# inputs 文件夹和 testplans 中获得输入，生成 json 格式的测试用例

import json
from subprocess import run, PIPE
from os import path

join = path.join

def execf(params):
    p=run(['./a.exe']+params, stdout=PIPE)
    return(p.stdout)
    
with open('./data.txt') as fr:
    plans = fr.readlines()

res=[]

for item in plans:
    print('running {}'.format(item))
    
    # 防止 * 被当成通配符
    params = [f'"{a}"' for a in item.strip().split()]
    
    res.append({
        'name': item,
        'params': params,
        'output': execf(params).decode('ascii'),
    })

with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
