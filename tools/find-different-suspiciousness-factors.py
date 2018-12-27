# 找出两个 suspiciousness factors 的区别

import json

with open('tot_info-suspiciousness-factors.json') as f:
    withoutWeight = json.load(f)
with open('tot_info-suspiciousness-factors-with-weight.json') as f:
    withWeight = json.load(f)


result = []

for left, right in zip(withoutWeight, withWeight):
    fO= []
    for lefts, rights in zip(left['factors of O'], right['factors of O']):
        if lefts['statementIndex'] != rights['statementIndex']:
            fO.append({
                "without-weight": lefts,
                "with-weight": rights,
            })
    fOp= []
    for lefts, rights in zip(left['factors of Op'], right['factors of Op']):
        if lefts['statementIndex'] != rights['statementIndex']:
            fOp.append({
                "without-weight": lefts,
                "with-weight": rights,
            })
    result.append({
        "version": left['version'],
        "O-difference": fO,
        "Op-difference": fOp,
    })

with open('difference-tot_info-suspiciousness-factor.json', 'w') as f:
    json.dump(result, f, indent=2)

