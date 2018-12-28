# 读取 testcase-weight 数据，把其中不是 1.0 的取出来

import json

with open('tot_info-testcase-weight.json', 'r') as fr:
    results = json.load(fr)
with open('totInfoRunningResult.json') as fr:
    versionToCases = {
            item['program']['title']: item['runCases']
            for item in json.load(fr)
    }
output = []

#print(versionToCases['v1'][:5])

for record in results:
    for item in record['weight-for-testcases']:
        if item['testcase-weight'] != 1.0:
            versionStr = record['version']
            testCaseIndex = item['testcase-index']
            try:
                output.append({
                    'index': versionStr+'-'+str(testCaseIndex),
                    'weight': item['testcase-weight'],
                    'correct': versionToCases[versionStr][testCaseIndex]['correct']
                })
            except:
                print('out of range {}-{}'.format(versionStr, testCaseIndex))

print(json.dumps(output, indent=2))
