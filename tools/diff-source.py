# 比较多个源代码，生成 xml，方便导入到 excel 中
# 使用了 diff 命令。windows 版本需要安装 diffutils
# 假如您使用的是 msys2，可以使用以下命令安装 diffutils： pacman -S diffutils

from os import path
import os
import subprocess
import xml.etree.ElementTree as ET

program_name = 'tot_info'

base_dir = 'd:/Sources/project/theoretical-evaluation/src/main/resources/'+program_name
orig_file = path.join(base_dir, 'origin/' +program_name+ '.c')
versions_dir = path.join(base_dir, 'versions')

root = ET.Element('root')

# 按照版本名排序
dirs = sorted(os.listdir(versions_dir), key=lambda s: s[1:].zfill(2))

for version_dir in dirs:
    vers_file = path.join(versions_dir, version_dir, program_name+'.c')
    result = subprocess.run(['diff', '--strip-trailing-cr', orig_file, vers_file], stdout=subprocess.PIPE)

    ver_element = ET.SubElement(root, 'version', {"name": version_dir})
    ver_element.text = result.stdout.decode('utf-8')

def indent(elem, level=0):
    """
    用于缩进 xml，来自 https://stackoverflow.com/a/33956544
    """
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

indent(root)
print(ET.tostring(root).decode('ansi'))
