# inputs 文件夹和 testplans 中获得输入，生成 json 格式的测试用例

"""
由于这个程序有文件读写功能，请将文件夹安排成以下结构，以保证结果一致：

- root
|- source
|  |- orig
|     |- a.exe
|     |- run-all.py
|
|- testplans
|  |- inputs
|     |- [各种输入文件]
|
|- testplans.alt
   |- universe

然后在 run-all.py 的目录下执行 python run-all.py
"""

import json
from subprocess import run, PIPE
from os import path

join = path.join

def execf(params, inputStr):
    p=run(['a.exe']+params, stdout=PIPE, input=inputStr.encode('ascii'))
    return(p.stdout)

def readInput(filename):
    basePath = '../../testplans/inputs'

    with open(join(basePath, filename)) as fr:
        return fr.read()

with open('../../testplans.alt/universe') as fr:
    plans = fr.readlines()

res=[]

for item in plans:
    print('running {}'.format(item))

    splited = item.strip().split(' ')

    if splited[0] == '<':
        # input from stdin
        # < newtst148.tst

        stdin = readInput(splited[1])

        res.append({
            'name': item,
            'params': [],
            "input": stdin,
            'output': execf([], stdin).decode('ascii'),
        })

    elif splited[0].startswith('../'):
        # input from file
        # ../inputs/jk1

        # 在java中执行的时候，需要更换参数以保证从工作路径能找到输入文件
        param = '../../testplans/' + splited[0][3:]
        res.append({
            'name': item,
            'params': [param],
            "input": '',
            'output': execf([param], '').decode('ascii'),
        })

    else:
        # other params
        # one doesntliketwo

        res.append({
            'name': item,
            'params': splited,
            "input": '',
            'output': execf(splited, '').decode('ascii'),
        })


with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
