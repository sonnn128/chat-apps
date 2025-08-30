### For development
```
docker compose -f compose.dev.yml
run discovery server
run api-gateway
```

```
C:\Users\son>docker ps
CONTAINER ID   IMAGE                             PORTS                                         NAMES
5bc00b7168be   tchiotludo/akhq:latest            0.0.0.0:9999->8080/tcp, [::]:9999->8080/tcp   akhq
c2fc77af3a0a   confluentinc/cp-kafka:7.3.0       0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp   kafka
876cf323cab2   confluentinc/cp-zookeeper:7.3.0   2181/tcp, 2888/tcp, 3888/tcp                  zookeeper
44507cf23955   cassandra:latest                  0.0.0.0:9042->9042/tcp, [::]:9042->9042/tcp   chat-apps-cassandra-1
14d8cb499bc2   postgres:16                       0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   chat-apps-postgres-1

```
### For production
### For docker
```
docker compose up -d
Then visit: localhost:5173
```
```angular2html

api-gateway
discovery-server
services
        auth-service
        channel-service
        chat-service
        notification-service
        user-service
```
        

### Git commit invention
```
feat: chat 
feat(ui): create Emoji message
feat(api): create api GET /users

fix(ui):
fix(api):

test(ui):
test(api):


```

