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

# 添加 maven 镜像源
RUN mkdir /root/.m2 && sed '$i<mirrors><mirror><id>alimaven</id><name>aliyunmaven</name><url>https://maven.aliyun.com/nexus/content/groups/public/</url><mirrorOf>*</mirrorOf></mirror></mirrors>' /usr/share/maven/ref/settings-docker.xml > /root/.m2/settings.xml

WORKDIR /app

COPY pom.xml .
RUN mvn verify --fail-never
RUN mvn dependency:copy-dependencies

COPY . .
RUN mvn compile && rm ./target/theoretical-evaluation-framework-1.0-SNAPSHOT.jar

ENTRYPOINT ["sh", "entry-point.sh"]
