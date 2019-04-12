#!/usr/bin/env bash
# 构建并发布镜像，在使用前需要先用 docker login 登录

set -e
export DOCKER_BUILDKIT=0

# 要执行的类名
MAIN_CLASS=edu.nwpu.machunyan.theoreticalEvaluation.application.Main

BASE_IMAGE_TAG=theoretical-evaluation-base

# 输出和发布的镜像名
OUTPUT_IMAGE_TAG=liu233w/private-project:theoretical-evaluation

# quick and dirty fix: see https://github.com/GoogleContainerTools/jib/issues/1468#issuecomment-462827203
docker run --rm -d -p 5000:5000 --name registry registry:2
function finish {
    docker container stop registry
}
trap finish EXIT
BASE_IMAGE_TAG=localhost:5000/liu233w/${BASE_IMAGE_TAG}

#BASE_IMAGE_TAG=liu233w/${BASE_IMAGE_TAG}

docker build -t ${BASE_IMAGE_TAG} --cache-from ${BASE_IMAGE_TAG} - < base.Dockerfile
docker push ${BASE_IMAGE_TAG}

MSYS2_ARG_CONV_EXCL='-Djib.container.workingDirectory' \
mvn compile com.google.cloud.tools:jib-maven-plugin:1.0.2:dockerBuild \
    -Djib.useCurrentTimestamp=true \
    -Djib.allowInsecureRegistries=true \
    -Djib.from.image=${BASE_IMAGE_TAG} \
    -Djib.to.image=${OUTPUT_IMAGE_TAG} \
    -Djib.container.mainClass=${MAIN_CLASS} \
    -Djib.container.workingDirectory=/workdir \
    -Djib.container.args=-Dfile.encoding=UTF8 \
    -Djib.httpTimeout=60000

docker push ${OUTPUT_IMAGE_TAG}
