FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app

# Copy toàn bộ project (nhờ build context là root)
COPY . .

# Build toàn bộ project từ root (Maven sẽ tự xử lý thứ tự dependency)
# -pl : chỉ build module chat-service và các dependency của nó (chatapps-base)
# -am : also make dependents
RUN mvn clean install -DskipTests -pl chat-service -am

FROM eclipse-temurin:17.0.8.1_1-jre-ubi9-minimal
RUN unlink /etc/localtime;ln -s  /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime

# Copy jar từ target của module
COPY --from=build /app/chat-service/target/chat-service-0.0.1-SNAPSHOT.jar /run/chat-service-0.0.1-SNAPSHOT.jar

EXPOSE 9002
ENV JAVA_OPTIONS="-Xmx2048m -Xms256m"
ENTRYPOINT ["java", "-jar", "/run/chat-service-0.0.1-SNAPSHOT.jar"]
