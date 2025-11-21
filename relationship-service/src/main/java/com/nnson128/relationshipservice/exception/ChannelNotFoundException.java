package com.nnson128.relationshipservice.exception;

import com.nnson128.chatapps_base.exception.ResourceNotFoundException;

public class ChannelNotFoundException extends ResourceNotFoundException {
    public ChannelNotFoundException(String message) {
        super(message);
    }
}
