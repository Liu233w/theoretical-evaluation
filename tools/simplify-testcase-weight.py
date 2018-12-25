# 读取 testcase-weight 数据，把其中不是 1.0 的取出来

import json

with open('tcas-testcase-weight - 副本.json', 'r') as fr:
    results = json.load(fr)
output = []

for record in results:
    for item in record['weight-for-testcases']:
        if item['testcase-weight'] != 1.0:
            output.append({
                'index': record['version']+'-'+str(item['testcase-index']),
                'weight': item['testcase-weight'],
            })

print(json.dumps(output, indent=2))
