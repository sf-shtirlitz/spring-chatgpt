package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@ToString
@NoArgsConstructor
@Component
public class SpeechToTextReqDto {
    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;

    private byte[] speechMessage;
    private String fileName;

    public SpeechToTextReqDto(byte[] speechMessage, String fileName) {
        this.speechMessage = speechMessage;
        this.fileName = fileName;
    }
}
