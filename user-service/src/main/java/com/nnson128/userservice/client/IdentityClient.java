package com.nnson128.userservice.client;

import com.nnson128.userservice.dto.identity.TokenExchangeParam;
import com.nnson128.userservice.dto.identity.TokenExchangeResponse;
import com.nnson128.userservice.dto.identity.UserCreationParam;
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

    @org.springframework.web.bind.annotation.PutMapping(value = "/admin/realms/chat-apps/users/{userId}/reset-password",
            consumes = "application/json")
    ResponseEntity<?> resetPassword(
            @RequestHeader("authorization") String token,
            @org.springframework.web.bind.annotation.PathVariable("userId") String userId,
            @RequestBody com.nnson128.userservice.dto.identity.Credential credential);

}
