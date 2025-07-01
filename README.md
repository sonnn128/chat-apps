```
                  +-------------------------+
                  |         CLIENT          |
                  +-------------------------+
                           |
                           | requests
                           v
+---------------------------------------------------------------------------------+
|                       TẦNG BẢO VỆ (API GATEWAY)                                 |
|                    - check token trừ public endpoint                            |
+---------------------------------------------------------------------------------+
        |                            |                              |             |
        | 2. send requests           | 3. Chuyển người vào các phòng ban          |
        |                            |                                            |
        v                            '--------------------------------------------'
+------------------+                                      |
|   PHÒNG NHÂN SỰ  |                                      |
|  (Auth Service)  |                                      v
|                  |            +-------------------------------------------------+
| - check token    |            |          KHU VỰC VĂN PHÒNG (Microservices)      |
+------------------+            | +-----------------+   +-------------------+   +--------------------+
                                | | PHÒNG SẢN PHẨM  |   |  PHÒNG HỒ SƠ      |   |  PHÒNG THÔNG BÁO   |
                                | | (Product Svc)   |   |  (User Svc)       |   | (Notification Svc) |
                                | |                 |   |                   |   |                    |
                                | | - Kiểm tra thẻ  |   | - Kiểm tra thẻ    |   | - Gửi tin nhắn     |
                                | |   nhân viên.    |   |   nhân viên.      |   |   (WebSocket)      |
                                | | - Phân quyền.   |   | - Phân quyền.     |   |                    |
                                | +-----------------+   +-------------------+   +--------------------+

```

```
### 1. API parttern
1. api public . 
        - add path to publicEndpoints[] in AuthenticationFilter [API GATEWAY]
        - config SecurityFilterChain on service (example: class SecurityFilterChain on products service)
        - add endpoint
2. api token
        - define api
3. api token role admin
        - define api + @PreAuthorize

### 2. when add a service
1. add service
2. add config yml in API GATEWAY
3. SecurityConfig + CustomHeaderAuthenticationFilter (Like order service)

### 3. service to service (Example order service)
1. add class FeignClientConfiguration (Like in order service)
2. add annotation (Like OrderServiceApplication)
3. add ProductServiceClient interface (like orderServiceClient )
4. DemoController

```
```
    docker compose -f compose.dev.yml
    docker-compose exec postgres psql -U dbuser -l

```