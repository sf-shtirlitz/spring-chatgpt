package com.alexk.ChatbotGPT.Controller;

import com.alexk.ChatbotGPT.Service.ChatGPTResponseService;
import com.alexk.ChatbotGPT.Service.SpeechToTextService;
import com.alexk.ChatbotGPT.Service.StoredChatService;
import com.alexk.ChatbotGPT.Service.TextToSpeechService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@RequestMapping("chatbot")
public class ChatbotController {

    public ChatbotController() {
        System.out.println("ChatbotController constructor called...");
        System.out.println("apiKey=" + apiKey);
    }
    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;

    @Value("${openai.elevenlabs.key}") // Load API key from application properties or environment variables
    private String textToSpeechApiKey;
    @Autowired
    private SpeechToTextService speechToTextService;
    @Autowired
    private ChatGPTResponseService chatGPTResponseService;

    @Autowired
    StoredChatService storedChatService;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @GetMapping()
    public ResponseEntity<String> healthCheck(){
        return ResponseEntity.ok("Healthy");
    }

    @PostMapping("transcribe")
    public Mono<ResponseEntity<?>> convertSpeechToText(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("AudioFileName") String audioFileName,
            @RequestBody byte[] audio){
        System.out.println("Controller transcribe is called...");
        return Mono.defer(() -> {
            // Verify API key from the header
            if (StringUtils.isEmpty(apiKey) || !authorizationHeader.contains(apiKey)) {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key"));
            }

            // Process the audio file and perform transcription using the OpenAI API
            try {
                InputStream inputStreamAudio = new ByteArrayInputStream(audio);
                return Mono.just(ResponseEntity.status(HttpStatus.OK).body(this.speechToTextService.sendTranscriptionRequest(apiKey, inputStreamAudio, audioFileName)));
            } catch (/*IOException*/ Throwable e) {
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing audio"));
            }
        });
    };

    @PostMapping("voice-for-voice")
    public ResponseEntity<byte[]> speechForSpeechRequest(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("AudioFileName") String audioFileName,
            @RequestBody byte[] audio){
        System.out.println("Controller voice-for-voice is called...");
        // Verify API key from the header
        if (StringUtils.isEmpty(apiKey) || !authorizationHeader.contains(apiKey)) {
            return null;//ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key");
        }

        // Process the audio file and perform transcription using the OpenAI API
        try {
            InputStream inputStreamAudio = new ByteArrayInputStream(audio);
            ResponseEntity<String> transcribedResponse = this.speechToTextService.sendTranscriptionRequest(apiKey, inputStreamAudio, audioFileName);

            System.out.println("Status Code: " + transcribedResponse.getStatusCodeValue());
            System.out.println("Response Body: " + transcribedResponse.getBody());

            JSONObject jsonResponseBody = new JSONObject(transcribedResponse.getBody());
            ResponseEntity<String> chatBotResponse = chatGPTResponseService.sendChatGPTRequest(apiKey, jsonResponseBody.getString("text"));

            ResponseEntity<String> chatBotResponseString = chatBotResponse;
            // ... other operations with the ResponseEntity

            System.out.println("Status Code: " + chatBotResponseString.getStatusCodeValue());
            System.out.println("Response Body: " + chatBotResponseString.getBody());

            JSONObject jsonObject = new JSONObject(chatBotResponseString.getBody());
            String botResponse = ((JSONObject)(jsonObject.getJSONArray("choices").getJSONObject(0).get("message")))
                    .getString("content");
            System.out.println("ChatGPT response: " + botResponse);

            ResponseEntity<byte[]> botVoiceResponse = this.textToSpeechService.sendTextToSpeechRequest(textToSpeechApiKey,botResponse);

            return botVoiceResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;//ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing audio");
        }
    }

    @PostMapping("chat")
    public Mono<ResponseEntity<?>> chatGPTRequest(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody String message) {
        System.out.println("Controller chat is called...");
        return Mono.defer(() -> {
            // Verify API key from the header
            if (StringUtils.isEmpty(apiKey) || !authorizationHeader.contains(apiKey)) {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key"));
            }

            // Process the audio file and perform transcription using the OpenAI API
            try {
                Mono<ResponseEntity<String>> responseMono = this.chatGPTResponseService.sendChatGPTRequestWebFlux(apiKey,message);
                //-------------------------
                responseMono.subscribe(responseEntity -> {
                    ResponseEntity<String> response = responseEntity;

                    // Now you can work with the ResponseEntity
                    // For example, you can get status code, body, headers, etc.
                    int statusCode = response.getStatusCodeValue();
                    String responseBody = response.getBody();

                    System.out.println("Status Code: " + statusCode);
                    System.out.println("Response Body: " + responseBody);

                    JSONObject jsonObject = new JSONObject(responseBody);
                    //JSONArray jsArray = jsonObject.getJSONArray("choices");
                    //JSONObject jsonObject1 = jsArray.getJSONObject(0);
                    //JSONObject responseMessage = (JSONObject)(jsonObject1.get("message"));
                    String botResponse = ((JSONObject)(jsonObject.getJSONArray("choices").getJSONObject(0).get("message")))
                            .getString("content");

                    JSONObject jsonOrigRequestMessage = new JSONObject(message);
                    String chatRequest = jsonOrigRequestMessage
                            .getJSONArray("messages")
                            .getJSONObject(0)
                            .getString("content");
                    this.storedChatService.storeMessages(chatRequest,botResponse );
                });
                return responseMono;
                //---------------------------
            } catch (/*IOException*/ Throwable e) {
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing audio"));
            }
        });
    };
}
