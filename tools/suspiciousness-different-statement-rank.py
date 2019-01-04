# 如果两个 suspiciousness factor 中同一语句有不同的排名，就输出它
import json


with open('tot_info-suspiciousness-factors.json') as f:
    withoutWeight = json.load(f)
with open('tot_info-suspiciousness-factors-with-weight.json') as f:
    withWeight = json.load(f)

result = []

max_version_num = 23
version = 1
while version <= max_version_num:

    versionResult = []

    left = withoutWeight[version-1]["factors of Op"]
    right=withWeight[version-1]["factors of Op"]

    #print(left[:5])

    def getStatements(left):
        result = {}
        for i in range(0, len(left)):
            result[left[i]['statementIndex']] = {
                'rank': i,
                'factor for Op': left[i]['suspiciousnessFactor']
            }
        return result

    left_statements = getStatements(left)
    right_statements = getStatements(right)
    for statementIndex, item in left_statements.items():
        if item['rank'] != right_statements[statementIndex]['rank']:
            versionResult.append({
                'statementIndex': statementIndex,
                'rankInWithoutWeight': item,
                'rankInWithWeight': right_statements[statementIndex],
            })

    result.append({
        'version': 'v'+str(version),
        'result': versionResult,
    })

    version = version + 1

with open('different-statement-index.json', 'w') as f:
    json.dump(result, f, indent=2)
