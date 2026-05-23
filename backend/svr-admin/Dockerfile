# eclipse-temurin:17-jammy
FROM bellsoft/liberica-openjdk-debian:17.0.14

LABEL top.wyhao.image.authors="wangyonghao@163.com"

COPY ./app/lib/. /app/lib/
COPY ./app/config/. /app/config/
COPY ./app/bin/wyh-admin.jar /app/bin/app.jar

WORKDIR /app

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+UseZGC", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.profiles.active=dev", \
            "-jar", \
            "bin/app.jar"]