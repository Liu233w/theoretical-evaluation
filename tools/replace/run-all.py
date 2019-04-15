# inputs 文件夹和 testplans 中获得输入，生成 json 格式的测试用例

"""
这里的输入比较复杂，包括
'$' ''\''Z y<j$`3-b6{hC,KW4dJZ\tWkm' < input/ruin.1104
这样的输入，有转义操作。

对于
'@n' '[0-9]&&[a-z]' <input/ruin.144
这样的输入，shlex会把最右边的io重定位符号和参数放到一起，最好使用第三方的库

import bashlex
"""

import json
from subprocess import run, PIPE
from os import path
import bashlex

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

    # '$' ''\''Z y<j$`3-b6{hC,KW4dJZ\tWkm' < input/ruin.1104
    splited = list(bashlex.split(item.strip()))
    
    # ['$', "'Z y<j$`3-b6{hC,KW4dJZ\tWkm", '<', 'input/ruin.1104']
    stdin = readInput(splited[-1])
    params = splited[:-2]

    res.append({
        'name': item,
        'params': params,
        "input": stdin,
        'output': execf(params, stdin).decode('ascii'),
    })

with open('cases.json','w') as fr:
    json.dump(res, fr, indent=2)
