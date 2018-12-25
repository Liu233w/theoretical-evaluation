# 将 tot_info 中的测试数据导出成 json 文件。避免压缩包中的linux格式文件名和 windows 不兼容导致的测试数据丢失

import json
import tarfile

fr=tarfile.open('totinfo_2.0.tar - 副本.gz')

out=[]

beginPath = 'totinfo/inputs/universe/'

for item in fr:
    if item.name.startswith(beginPath) and item.isreg():
        f=fr.extractfile(item)
        try:
            out.append({
                "name": item.name[len(beginPath):],
                "content": f.read().decode('ascii'),
            })
        except:
            print('err when process {}'.format(item.name[len(beginPath):]))

with open('output.json', 'w') as fr:
    json.dump(out, fr, indent=2)
