package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class TextToTextReqDto {
    private String convertedText;

    public TextToTextReqDto(String convertedText) {
        this.convertedText = convertedText;
    }
}
