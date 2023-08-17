package com.alexk.ChatbotGPT.Controller;

import com.alexk.ChatbotGPT.Service.SpeechToSpeechReactService;
import com.alexk.ChatbotGPT.dto.SpeechToSpeechRequestDto;
import com.alexk.ChatbotGPT.dto.SpeechToSpeechResponseDto;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RestController
public class SpeechToSpeechController {

    @Autowired
    private SpeechToSpeechReactService speechToSpeechReactService;

    @PostMapping("/speech-to-speech")
    public Mono<ResponseEntity<SpeechToSpeechResponseDto>> speechToSpeechRequest(
            @RequestBody byte[] audio,
            @RequestHeader("AudioFileName") String audioFileName
            ) {
        System.out.println("Controller reactive speech-to-speech is called...");
        System.out.println("AudioFileName: " + audioFileName);

        Mono<SpeechToSpeechRequestDto> requestDtoMono = Mono.just(new SpeechToSpeechRequestDto(audio,audioFileName));
        return this.speechToSpeechReactService.processSpeechToSpeechReq(requestDtoMono,audioFileName)
                .map(ResponseEntity::ok)
                .onErrorReturn(WebClientResponseException.class, ResponseEntity.badRequest().build())
                .onErrorReturn(WebClientRequestException.class, ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

    }

    @PostMapping("/speech-to-speech-byte")
    public ResponseEntity<byte[]>  speechToSpeechRequestByte(
            @RequestBody byte[] audio,
            @RequestHeader("AudioFileName") String audioFileName
    ) {
        System.out.println("Controller reactive speech-to-speech-byte is called...");
        System.out.println("AudioFileName: " + audioFileName);

        Mono<SpeechToSpeechRequestDto> requestDtoMono = Mono.just(new SpeechToSpeechRequestDto(audio,audioFileName));
        Optional<Mono<ResponseEntity<SpeechToSpeechResponseDto>>> optional = Optional.of(this.speechToSpeechReactService.processSpeechToSpeechReq(requestDtoMono,audioFileName)
                .map(ResponseEntity::ok)
                .onErrorReturn(WebClientResponseException.class, ResponseEntity.badRequest().build())
                .onErrorReturn(WebClientRequestException.class, ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()));

        if(optional.isPresent()) {
            ResponseEntity<SpeechToSpeechResponseDto> response = optional.get().block();
            if (response != null) {
                if (response.getBody() != null) {
                    return ResponseEntity.ok(response.getBody().getSpeechResponseMessage());
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
