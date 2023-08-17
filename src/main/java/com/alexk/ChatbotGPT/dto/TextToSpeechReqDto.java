package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class TextToSpeechReqDto {
    String message;
    private String errorMessage;

    public TextToSpeechReqDto(String message) {
        this.message = message;
    }
}
