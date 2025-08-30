# HOW TO RUN THE APP?  
## 1. For production
## 2. For docker
```
docker compose up -d
then visit localhost:5173 to access the app
```

## 3. For development
### 3.1 Run database / MQ with docker
```
docker compose -f compose.dev.yml up -d
```
### 3.2 Check containers
```
C:\Users\son>docker ps
IMAGE                             PORTS                                         NAMES
tchiotludo/akhq:latest            0.0.0.0:9999->8080/tcp, [::]:9999->8080/tcp   akhq
confluentinc/cp-kafka:7.3.0       0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp   kafka
confluentinc/cp-zookeeper:7.3.0   2181/tcp, 2888/tcp, 3888/tcp                  zookeeper
cassandra:latest                  0.0.0.0:9042->9042/tcp, [::]:9042->9042/tcp   chat-apps-cassandra-1
postgres:16                       0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   chat-apps-postgres-1

```
### 3.3 Run configuration service
```
cd <project folder/discovery-server>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/discovery-server-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/api-gateway>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/api-gateway-0.0.1-SNAPSHOT.jar
```
### 3.4 Run services
```
cd <project folder/services/auth-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/auth-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/services/user-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/user-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/services/chat-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/chat-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/services/channel-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/channel-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/services/notification-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/notification-service-0.0.1-SNAPSHOT.jar
```
Then visit: localhost:5173 to access this app
## Git commit invention
```
feat: chat 
feat(ui): create Emoji message
feat(api): create api GET /users

fix(ui):
fix(api):

test(ui):
test(api):


```

