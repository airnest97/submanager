package com.interswitch.submanager.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class SendMessageEvent extends ApplicationEvent {
    private final String type;
    public SendMessageEvent(Object source,String type) {
        super(source);
        this.type = type;
    }
}
