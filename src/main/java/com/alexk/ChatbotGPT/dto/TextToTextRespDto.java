package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
public class TextToTextRespDto {
    private String message;
    private String errorMessage;
    public TextToTextRespDto(String message) {
        this.message = message;
    }

    public TextToTextRespDto(TextToTextRespDto dto) {
        this.message = dto.getMessage();
        this.errorMessage = dto.getErrorMessage();
    }
}
