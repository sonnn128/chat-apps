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