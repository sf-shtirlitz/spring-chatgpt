package com.alexk.ChatbotGPT.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
@Service
public class StoredChatService {

    @Value("${openai.api.chat.storedmessages.file}")
    private String fileName;
    public void storeMessages(String requestMessage, String responseMessage) {
        try {
            JSONArray jsonArray;

            // Read existing messages from the file if it exists
            File file = new File(fileName);
            if (file.exists()) {
                String fileContent = readFileContent(file);
                jsonArray = new JSONArray(fileContent);
            } else {
                jsonArray = new JSONArray();
            }

 /*           JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant.");
            jsonArray.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", "Hello!");
            jsonArray.put(userMessage);
*/
            // Add new messages to the array
            if(requestMessage!=null) {
                JSONObject newRequestMessage = new JSONObject();
                newRequestMessage.put("role", "user");
                newRequestMessage.put("content", requestMessage);
                jsonArray.put(newRequestMessage);
            }

            if(responseMessage!=null) {
                JSONObject newResponseMessage = new JSONObject();
                newResponseMessage.put("role", "assistant");
                newResponseMessage.put("content", responseMessage);
                jsonArray.put(newResponseMessage);
            }

            // Write the JSON array to a file
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(jsonArray.toString(4)); // Use 4 as an indentation level for pretty printing
            fileWriter.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public String readFileContent(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        return content.toString();
    }
}
