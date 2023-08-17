package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class TextToSpeechRespDto {
    byte[] speechResponseMessage;
    private String errorMessage;

    public TextToSpeechRespDto(byte[] speechResponseMessage) {
        this.speechResponseMessage = speechResponseMessage;
    }
}
