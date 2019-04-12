FROM openjdk:8-jdk-alpine

# 国内的镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories && \
    sed -i 's/http:/https:/g' /etc/apk/repositories && \
    apk update

# 部分源程序不能在最新的 gcc 上编译，所以使用旧版
RUN apk add \
    gcc \
    g++ \
    make \
    --no-cache
