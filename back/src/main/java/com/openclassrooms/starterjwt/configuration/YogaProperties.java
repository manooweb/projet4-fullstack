package com.openclassrooms.starterjwt.configuration;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor
public class YogaProperties {

    private final YogaMessagesProperties messages;
}
