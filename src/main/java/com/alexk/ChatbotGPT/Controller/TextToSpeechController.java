package com.alexk.ChatbotGPT.Controller;

import com.alexk.ChatbotGPT.Service.TextToSpeechReactService;
import com.alexk.ChatbotGPT.Service.TextToSpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("texttospeech")
public class TextToSpeechController {

    @Value("${openai.elevenlabs.key}")
    private String apiKey;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private TextToSpeechReactService textToSpeechReactService;

    @PostMapping("")
    public Mono<ResponseEntity<byte[]>> textToSpeechElevenLabsRequest(
            @RequestHeader("xi-api-key") String authorizationHeader,
            @RequestBody String message) {
        System.out.println("Controller textToSpeech is called...");
        // Verify API key from the header
        if (StringUtils.isEmpty(apiKey) || !authorizationHeader.contains(apiKey)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key".getBytes()));
        }

        try {
            return this.textToSpeechService.sendTextToSpeechRequestWebFlux(message);
        } catch (/*IOException*/ Throwable e) {
            e.printStackTrace();
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing audio".getBytes()));
        }
    }
}