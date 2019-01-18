# 比较多个源代码，生成 xml，方便导入到 excel 中
# 使用了 diff 命令。windows 版本需要安装 diffutils
# 假如您使用的是 msys2，可以使用以下命令安装 diffutils： pacman -S diffutils

from os import path
import subprocess
import xml.etree.ElementTree as ET

base_dir = 'd:/Sources/project/theoretical-evaluation/target/sources/totinfo/'
orig_file = path.join(base_dir, 'source.alt/source.orig/tot_info.c')
versions_dir = path.join(base_dir, 'versions.alt/versions.orig')

root = ET.Element('root')

for i in range(1, 23 + 1):
    version_dir = 'v' + str(i)
    vers_file = path.join(versions_dir, version_dir, 'tot_info.c')
    result = subprocess.run(['diff', orig_file, vers_file], stdout=subprocess.PIPE)
    
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