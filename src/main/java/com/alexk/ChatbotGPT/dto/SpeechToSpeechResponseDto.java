package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class SpeechToSpeechResponseDto {
    byte[] speechResponseMessage;
    private String errorMessage;

    public SpeechToSpeechResponseDto(byte[] speechResponseMessage) {
        this.speechResponseMessage = speechResponseMessage;
    }
}
