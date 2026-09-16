FROM registry.cn-shenzhen.aliyuncs.com/iwindplus/openjdk:17-alpine
ARG JAR_FILE="*.jar"
ENV LANG="C.UTF-8" \
    LC_ALL="C.UTF-8" \
    TZ="Asia/Shanghai" \
    JAR="app.jar" \
    JAVA_OPTS=""
RUN \
  sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && \
  apk update && apk upgrade && \
  apk add --no-cache net-tools iputils procps curl busybox-extras curl bash tini && \
  apk add --no-cache tzdata && \
  apk add --no-cache ttf-dejavu fontconfig && \
  cp /usr/share/zoneinfo/$TZ /etc/localtime && \
  echo $TZ > /etc/timezone && \
  ln -s /lib /lib64 && \
  rm -rf /var/cache/apk/*
WORKDIR /home
COPY ${JAR_FILE} ${JAR}
CMD java -Dfile.encoding=UTF-8 ${JAVA_OPTS} -jar ${JAR}