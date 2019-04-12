FROM maven:3.6-jdk-11

#RUN rm -rf /etc/apt/sources.list.d
RUN apt-get update && apt-get install apt-transport-https -y

# 国内的镜像源
RUN echo '\
# 默认注释了源码镜像以提高 apt update 速度，如有需要可自行取消注释\n\
deb https://mirrors.tuna.tsinghua.edu.cn/debian/ stretch main contrib non-free\n\
# deb-src https://mirrors.tuna.tsinghua.edu.cn/debian/ stretch main contrib non-free\n\
deb https://mirrors.tuna.tsinghua.edu.cn/debian/ stretch-updates main contrib non-free\n\
# deb-src https://mirrors.tuna.tsinghua.edu.cn/debian/ stretch-updates main contrib non-free\n\
deb https://mirrors.tuna.tsinghua.edu.cn/debian/ stretch-backports main contrib non-free\n\
# deb-src https://mirrors.tuna.tsinghua.edu.cn/debian/ stretch-backports main contrib non-free\n\
deb https://mirrors.tuna.tsinghua.edu.cn/debian-security stretch/updates main contrib non-free\n\
# deb-src https://mirrors.tuna.tsinghua.edu.cn/debian-security stretch/updates main contrib non-free\n\
'\
> /etc/apt/sources.list && apt-get update

# 部分源程序不能在最新的 gcc 上编译，所以使用旧版
RUN apt-get install \
    build-essential \
    -y

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
RUN mvn compile

ENTRYPOINT ["sh", "entry-point.sh"]
