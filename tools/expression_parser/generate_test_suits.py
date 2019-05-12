import random

function_list = [
    'pow',
    'sqrt',
    'log',
    'exp',
    'sin',
    'asin',
    'cos',
    'acos',
    'tan',
    'atan',
    'atan2',
    'abs',
    'fabs',
    'floor',
    'ceil',
    'round',
]

operator_list = ['+','-','*','/']

res = []

for _ in range(1000):

    inp = ''

    exp_num = random.randint(1, 5)
    for i in range(exp_num):
        inp += '('+str(random.uniform(-9999,9999)) +')'
        inp += random.choice(operator_list)

    inp = inp[0:len(inp)-1] # remove suffix operator

    if random.choice([True, False]):
        inp = random.choice(function_list)+'('+inp+')'

    res.append(inp)

with open('res.txt', 'w') as fr:
    for line in res:
        print(line, file=fr)
