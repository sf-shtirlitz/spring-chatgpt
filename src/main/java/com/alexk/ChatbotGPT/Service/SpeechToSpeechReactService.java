package com.alexk.ChatbotGPT.Service;

import com.alexk.ChatbotGPT.client.SpeechToTextClient;
import com.alexk.ChatbotGPT.dto.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

@Service
public class SpeechToSpeechReactService {
    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;

    @Value("${openai.elevenlabs.key}") // Load API key from application properties or environment variables
    private String textToSpeechApiKey;

    //"https://api.openai.com/v1/audio/transcriptions";
    @Value("${openai.api.url}")
    private String openApiUrl;

    @Autowired
    private SpeechToTextReactService speechToTextReactService;

    @Autowired
    private TextToTextReactService textToTextReactService;

    //@Autowired
    //private SpeechToTextClient speechToTextClient;

    @Autowired
    SpeechToTextService speechToTextService;

    @Autowired
    TextToSpeechReactService textToSpeechReactService;

    public Mono<SpeechToSpeechResponseDto> processSpeechToSpeechReq(Mono<SpeechToSpeechRequestDto> requestDtoMono, String audioFileName){

        return requestDtoMono.map(speechToSpeechRequestDto ->  new SpeechToSpeechRequestContext(speechToSpeechRequestDto, audioFileName))
                .flatMap(sToSReqCtx -> {
                    try{
                        Mono<SpeechToSpeechRequestContext> stsContext = this.speechToTextRequestResponse(sToSReqCtx);
                        return stsContext;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::textToTextRequestResponse)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(sToSReqCtx -> {
                    Mono<SpeechToSpeechRequestContext> stsContext = this.textToSpeechRequestResponse(sToSReqCtx);
                    System.out.println("TextToSpeechRespDto: " + stsContext.block().getTextToSpeechRespDto().toString().substring(0,100) + " ...");
                    return stsContext;
                })
                .map(context -> new SpeechToSpeechResponseDto(context.getSpeechToSpeechRespDto().getSpeechResponseMessage()))
                .subscribeOn(Schedulers.boundedElastic());//this uses a dedicated thread so the blocking call above would not suspend the execution
    }

 /*           try {
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
    }*/
    private Mono<SpeechToSpeechRequestContext> speechToTextRequestResponse(SpeechToSpeechRequestContext rc) throws IOException {
        return this.speechToTextReactService.sendTranscriptionRequestWebFluxViaDto(rc.getSpeechToTextReqDto())
                .doOnNext(speechToTextRespDto -> {
                    rc.setSpeechToTextRespDto(speechToTextRespDto);
                    rc.setTextToTextReqDto(speechToTextRespDto);
                })
                .doOnError(throwable -> this.processException(throwable))
//                .onErrorResume(throwable -> {
//                    // Here, you can extract and handle the error message from the server response
//                    String errorMessage = "Unknown error occurred from server";
//                    if (throwable instanceof ServiceUnavailbleException) {
//                        errorMessage = ((ServiceUnavailbleException)throwable).getMessage();
//                    }
//                    System.err.println("Error occurred: " + errorMessage);
//                })
                .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(1)))
                .thenReturn(rc);
    }

    private Mono<SpeechToSpeechRequestContext> textToTextRequestResponse(SpeechToSpeechRequestContext rc){
        System.out.println("-------------textToTextRequestResponse--------------");
        try {
            return this.textToTextReactService.sendChatGPTRequestWebFlux(rc.getTextToTextReqDto())
                    .doOnNext(textToTextRespDto -> {
                        System.out.println(textToTextRespDto);
                        rc.setTextToTextRespDto(textToTextRespDto);
                        System.out.println(rc.getTextToTextRespDto());
                        TextToSpeechReqDto textToSpeechReqDto = new TextToSpeechReqDto(textToTextRespDto.getMessage());
                        rc.setTextToSpeechReqDto(textToSpeechReqDto);
                        System.out.println("textToSpeechReqDto: " + textToSpeechReqDto);
                    })
                    .doOnError(throwable -> System.out.println("Error occurred: " + throwable.getMessage()))
                    .thenReturn(rc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<SpeechToSpeechRequestContext> textToSpeechRequestResponse(SpeechToSpeechRequestContext rc) {
        System.out.println("-------------textToSpeechRequestResponse--------------");
        try {
            return this.textToSpeechReactService.sendTextToSpeechRequestWebFlux(rc.getTextToSpeechReqDto())
                    .doOnNext(textToSpeechRespDto -> {
                        System.out.println("Setting up textToSpeechRespDto to byte[]...");
                        rc.setTextToSpeechRespDto(textToSpeechRespDto);
                        System.out.println("Setting up SpeechToSpeechRespDto to byte[]...");
                        SpeechToSpeechResponseDto speechToSpeechRespDto = new SpeechToSpeechResponseDto(textToSpeechRespDto.getSpeechResponseMessage());
                        rc.setSpeechToSpeechRespDto(speechToSpeechRespDto);
                        if(rc.getTextToSpeechRespDto()!=null) {
                            System.out.println("TextToSpeech has been set to not null");
                        }else {
                            System.out.println("There seems to be a problem. TextToSpeech is NULL...");
                        }
                    })
                    .doOnError(throwable -> System.out.println("Error occurred: " + throwable.getMessage()))
                    .thenReturn(rc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void processException(Throwable e) {
        if (e instanceof IOException) {
            IOException responseException = (IOException) e;
            String errorMessage = responseException.getMessage();
            System.err.println("Error occurred: " + e.getMessage() + " - " +errorMessage);
        } else {
            System.err.println("Unknown error occurred: " + e.getMessage());
        }
    }



}
