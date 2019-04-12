#!/usr/bin/env bash
# 运行镜像，将使用当前目录作为结果目录

IMAGE=liu233w/private-project:theoretical-evaluation

docker pull $IMAGE
docker run -it --rm -v $(dirname $0):/app/target/outputs $IMAGE $1
