package com.alexk.ChatbotGPT.Service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class MessageStorageRunner implements CommandLineRunner {

    @Autowired
    StoredChatService storedChatService;
    @Autowired
    ChatGPTResponseService chatGPTResponseService;

    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;
    @Value("${openai.api.chat.storedmessages.file}")
    private String fileName;

    @Value("${openai.api.chat.uselatestmessages}")
    private String latestMessages;

    @Override
    public void run(String... args) throws Exception {
        // Call the method you want to run after startup
        // Read existing messages from the file if it exists
        JSONArray jsonMessageArray = getNLatestMessages();

        Mono<ResponseEntity<String>> responseMono =  this.chatGPTResponseService.sendInitialAssistantPrepMessages(apiKey,jsonMessageArray);

        responseMono.subscribe(responseEntity -> {
            ResponseEntity<String> response = responseEntity;

            // Now you can work with the ResponseEntity
            // For example, you can get status code, body, headers, etc.
            int statusCode = response.getStatusCodeValue();
            String responseBody = response.getBody();

            System.out.println("Status Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);

            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray jsArray = jsonObject.getJSONArray("choices");
            JSONObject jsonObject1 = jsArray.getJSONObject(0);
            JSONObject responseMessage = (JSONObject)(jsonObject1.get("message"));
            String str = responseMessage.getString("content");
            storeMessages(null, str);
        });
    }

    private JSONArray getNLatestMessages() {
        JSONArray jsonMessageArray = new JSONArray();
        File file = new File(fileName);
        if (file.exists()) {
            try{
                String fileContent = this.storedChatService.readFileContent(file);
                jsonMessageArray = new JSONArray(fileContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(jsonMessageArray.length() > Integer.parseInt(this.latestMessages)) {
            JSONArray firstTwo = getFirstTwoSubarray(jsonMessageArray);
            jsonMessageArray = getLatestSubarray(jsonMessageArray, Integer.parseInt(this.latestMessages));
            jsonMessageArray = combineArrays(firstTwo, jsonMessageArray);
        }


        return jsonMessageArray;
    }
    public static JSONArray getLatestSubarray(JSONArray jsonArray, int count) {
        int arrayLength = jsonArray.length();
        int startIndex = Math.max(0, arrayLength - count); // Ensure the start index is not negative

        JSONArray subarray = new JSONArray();
        for (int i = startIndex; i < arrayLength; i++) {
            subarray.put(jsonArray.get(i));
        }
        return subarray;
    }

    public static JSONArray getFirstTwoSubarray(JSONArray jsonArray) {
        int arrayLength = jsonArray.length();
        int endIndex = Math.min(2, arrayLength); // Ensure endIndex doesn't exceed array length

        JSONArray subarray = new JSONArray();
        for (int i = 0; i < endIndex; i++) {
            subarray.put(jsonArray.get(i));
        }
        return subarray;
    }

    public static JSONArray combineArrays(JSONArray array1, JSONArray array2) {
        JSONArray combinedArray = new JSONArray();

        for (int i = 0; i < array1.length(); i++) {
            combinedArray.put(array1.get(i));
        }

        for (int i = 0; i < array2.length(); i++) {
            combinedArray.put(array2.get(i));
        }

        return combinedArray;
    }
    public void storeMessages(String requestMessage, String responseMessage) {
        // Your message storage logic here
        this.storedChatService.storeMessages(requestMessage, responseMessage);
    }
}
