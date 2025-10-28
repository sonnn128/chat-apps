##### Dockerfile #####
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn install -DskipTests=true

FROM eclipse-temurin:17.0.8.1_1-jre-ubi9-minimal

RUN unlink /etc/localtime;ln -s  /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime
COPY --from=build app/target/user-service-0.0.1-SNAPSHOT.jar /run/user-service-0.0.1-SNAPSHOT.jar

EXPOSE 9006

ENV JAVA_OPTIONS="-Xmx2048m -Xms256m"
ENTRYPOINT ["java", "-jar", "/run/user-service-0.0.1-SNAPSHOT.jar"]

# RUN: Build the image: RUN apt-get update && apt-get install -y python3
# CMD ["python3", "app.py"]
# CMD ["npm", "start"]
# 