package com.alexk.ChatbotGPT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Signal;

@Data
@ToString
@NoArgsConstructor
@Component
public class SpeechToSpeechRequestContext {
        private SpeechToSpeechRequestDto speechToSpeechReqDto;
        private SpeechToSpeechResponseDto speechToSpeechRespDto;
        private SpeechToTextReqDto speechToTextReqDto;
        private SpeechToTextRespDto speechToTextRespDto;
        private TextToTextReqDto textToTextReqDto;
        private TextToTextRespDto textToTextRespDto;
        private TextToSpeechRespDto textToSpeechRespDto;
        private TextToSpeechReqDto textToSpeechReqDto;

        public SpeechToSpeechRequestContext(SpeechToSpeechRequestDto speechToSpeechReqDto, String fileName) {
            this.speechToSpeechReqDto = speechToSpeechReqDto;
            this.speechToTextReqDto = new SpeechToTextReqDto(speechToSpeechReqDto.getSpeechReqMessage(), fileName);
        }

        public void setTextToTextReqDto(SpeechToTextRespDto speechToTextRespDto) {
                this.textToTextReqDto = new TextToTextReqDto(speechToTextRespDto.getConvertedText());
        }

        public void setTextToTextRespDto(TextToTextRespDto textToTextRespDto) {
                this.textToTextRespDto = new TextToTextRespDto(textToTextRespDto.getMessage());
                System.out.println("SpeechToSpeechRequestContext.setTextToTextRespDto: " + textToTextRespDto);
                System.out.println(this.getTextToTextRespDto());
        }
}
