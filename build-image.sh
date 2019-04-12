#!/usr/bin/env bash
# 构建并发布镜像，在使用前需要先用 docker login 登录

set -e
export DOCKER_BUILDKIT=0

# 输出和发布的镜像名
OUTPUT_IMAGE_TAG=liu233w/private-project:theoretical-evaluation
docker build -t ${OUTPUT_IMAGE_TAG} $(dirname $0)

docker push ${OUTPUT_IMAGE_TAG}
