```
    docker compose -f compose.dev.yml
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