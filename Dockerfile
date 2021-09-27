FROM alpine

LABEL br.com.alpine.key.manager.authors="Thiago Hayaci Zup"

RUN apk update && apk upgrade

RUN apk add openjdk11

RUN adduser -D keymanager

USER keymanager

WORKDIR /home/keymanager

COPY build/libs/*-all.jar keymanager.jar

CMD ["java", "-jar", "keymanager.jar"]