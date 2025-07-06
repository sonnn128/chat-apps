package com.sonnguyen.userservice.dto.request;

import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateChannelRequest {
    private String channelName; // Tên kênh (tùy chọn, cho group chat)
    private Set<UUID> memberIds; // Danh sách ID của các thành viên muốn thêm vào
}