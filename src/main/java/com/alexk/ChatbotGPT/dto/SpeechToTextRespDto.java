package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class SpeechToTextRespDto {
    private String convertedText;
    private String errorMessage;
    public SpeechToTextRespDto(String convertedText) {
        this.convertedText = convertedText;
    }
}
