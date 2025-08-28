# For development
```
    docker compose -f compose.dev.yml
    run discovery server
    run api-gateway
```
```
C:\Users\son>docker ps
CONTAINER ID   IMAGE                             COMMAND                  CREATED              STATUS                        PORTS                                         NAMES
5bc00b7168be   tchiotludo/akhq:latest            "docker-entrypoint.s…"   About a minute ago   Up About a minute (healthy)   0.0.0.0:9999->8080/tcp, [::]:9999->8080/tcp   akhq
c2fc77af3a0a   confluentinc/cp-kafka:7.3.0       "/etc/confluent/dock…"   About a minute ago   Up About a minute             0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp   kafka
876cf323cab2   confluentinc/cp-zookeeper:7.3.0   "/etc/confluent/dock…"   About a minute ago   Up About a minute             2181/tcp, 2888/tcp, 3888/tcp                  zookeeper
44507cf23955   cassandra:latest                  "docker-entrypoint.s…"   About a minute ago   Up About a minute (healthy)   0.0.0.0:9042->9042/tcp, [::]:9042->9042/tcp   chat-apps-cassandra-1
14d8cb499bc2   postgres:16                       "docker-entrypoint.s…"   About a minute ago   Up About a minute             0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   chat-apps-postgres-1

C:\Users\son>
```
# For production
# For docker
```
    docker-compose exec postgres psql -U dbuser -l
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
        
com/sonnguyen/notificationservice/events/dto/NewMessageSentEvent


```
{
    "success": true,
    "message": "Get All channels successfully",
    "data": [
        {
            "id": "91b7787b-cb13-4130-b58f-a4493c0a8b11",
            "channelName": "Project X Discussion",
            "createdBy": "593ef423-b798-4872-8064-c35d7699e663",
            "createdAt": "2025-07-04T10:34:12.676602",
            "messages": [
                {
                    "key": {
                        "channelId": "91b7787b-cb13-4130-b58f-a4493c0a8b11",
                        "messageId": "b1e3a6f0-5914-11f0-8078-11dc4a1c81ac"
                    },
                    "userId": "593ef423-b798-4872-8064-c35d7699e663",
                    "content": "Hello team, let's start!",
                    "type": "CHAT",
                    "timestamp": "2025-07-04T20:23:06.734+00:00"
                },
                {
                    "key": {
                        "channelId": "91b7787b-cb13-4130-b58f-a4493c0a8b11",
                        "messageId": "ceac1f10-5914-11f0-8078-11dc4a1c81ac"
                    },
                    "userId": "593ef423-b798-4872-8064-c35d7699e663",
                    "content": "Hello team, let's start!",
                    "type": "CHAT",
                    "timestamp": "2025-07-04T20:23:55.009+00:00"
                },
                {
                    "key": {
                        "channelId": "91b7787b-cb13-4130-b58f-a4493c0a8b11",
                        "messageId": "1809dda0-5979-11f0-91ea-615923b71365"
                    },
                    "userId": "593ef423-b798-4872-8064-c35d7699e663",
                    "content": "Hello team, let's start!",
                    "type": "CHAT",
                    "timestamp": "2025-07-05T08:21:47.781+00:00"
                },
                {
                    "key": {
                        "channelId": "91b7787b-cb13-4130-b58f-a4493c0a8b11",
                        "messageId": "c5c612a0-597a-11f0-91ea-615923b71365"
                    },
                    "userId": "593ef423-b798-4872-8064-c35d7699e663",
                    "content": "Hello team, let's start!",
                    "type": "CHAT",
                    "timestamp": "2025-07-05T08:33:48.746+00:00"
                }
            ]
        }
    ]
}
```
# Git commit invention
```
feat: chat 
feat(ui): create Emoji message
feat(api): create api GET /users

fix(ui):
fix(api):

test(ui):
test(api):


```
