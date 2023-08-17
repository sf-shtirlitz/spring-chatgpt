package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class SpeechToSpeechRequestDto {
    byte[] speechReqMessage;
    String fileName;

    public SpeechToSpeechRequestDto(byte[] speechReqMessage, String fileName) {
        this.speechReqMessage = speechReqMessage;
        this.fileName = fileName;
    }
}
