//package com.sonnguyen.notificationservice.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/ws")
//public class SockJsInfoController {
//
//    @GetMapping("/info")
//    public Map<String, Object> info() {
//        Map<String, Object> info = new HashMap<>();
//        info.put("entropy", -570222356);           // Bất kỳ số int
//        info.put("origins", List.of("*:*"));       // Luôn "*:*"
//        info.put("cookie_needed", true);           // Phải đúng
//        info.put("websocket", true);               // Cho phép WebSocket
//        return info;
//    }
//}
//
