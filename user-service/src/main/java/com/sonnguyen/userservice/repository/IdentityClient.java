package com.sonnguyen.userservice.repository;

import com.sonnguyen.userservice.dto.identity.TokenExchangeParam;
import com.sonnguyen.userservice.dto.identity.TokenExchangeResponse;
import com.sonnguyen.userservice.dto.identity.UserCreationParam;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "identity-client", url = "${keycloak.auth-server-url}")
public interface IdentityClient {
    @PostMapping(value = "/realms/chat-apps/protocol/openid-connect/token",
            consumes = "application/x-www-form-urlencoded")
    TokenExchangeResponse getClientToken(@QueryMap TokenExchangeParam params);

    @PostMapping(value = "/realms/chat-apps/protocol/openid-connect/token",
            consumes = "application/x-www-form-urlencoded")
    TokenExchangeResponse getUserToken(@QueryMap TokenExchangeParam params);


    @PostMapping(value = "/admin/realms/chat-apps/users",
            consumes = "application/json")
    ResponseEntity<?> createUser(
            @RequestHeader("authorization") String token,
            @RequestBody UserCreationParam param);

}
