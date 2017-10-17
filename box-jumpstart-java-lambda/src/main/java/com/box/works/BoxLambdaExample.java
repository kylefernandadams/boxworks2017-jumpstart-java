package com.box.works;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.box.sdk.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.io.*;
import java.util.Map;

public class BoxLambdaExample implements RequestHandler<Map<String, Object>, Object> {
    private LambdaLogger logger;
    private BoxDeveloperEditionAPIConnection api = null;
    private static final int MAX_CACHE_ENTRIES = 100;

    public Object handleRequest(Map<String, Object> input, Context context) {
        logger = context.getLogger();
        logger.log("Starting box lambda function...");

        JsonObject responseJson = Json.object();
        try {
            // Get the JSON object from the webhook
            Object requestBody = input.get("body");
            logger.log("Parsing box webhook object: " + String.valueOf(requestBody));
            JsonObject boxWebhookJson = Json.parse(String.valueOf(requestBody)).asObject();

            // Get the file id
            String fileId = boxWebhookJson.get("source").asObject().get("id").asString();
            logger.log("Found file id: " + fileId);

            // Load the box config file from the classpath and get a Box connection
            ClassLoader classLoader = getClass().getClassLoader();
            BoxConfig boxConfig = BoxConfig.readFrom(new FileReader(classLoader.getResource("box_config.json").getFile()));
            IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

            // Get the existing Box App User that owns the webhook
            String userId = boxWebhookJson.get("source").asObject().get("owned_by").asObject().get("id").asString();
            api = BoxDeveloperEditionAPIConnection.getAppUserConnection(userId, boxConfig, accessTokenCache);

            BoxUser user = BoxUser.getCurrentUser(api);
            logger.log("Successfully logged in with user: " + user.getInfo().getLogin());

            // Get file
            BoxFile file = new BoxFile(api, fileId);

            // Add metadata
            file.createMetadata(
                    "boxworks",
                    new Metadata()
                            .add("/session", "JumpStart Java")
                            .add("/presenters", "Ryan Schaller, Kyle Adams")
                            .add("/rating", 11));

            // Respond to webhook request
            responseJson
                    .add("status", 200)
                    .add("response", "Successfully added metadata to file: " + fileId);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.log("Failed to add metadata to file: " + e.getMessage());
            responseJson
                    .add("status", 500)
                    .add("response", "Failed to add metadata to file: " + e.getMessage());
        }

        return responseJson;
    }
}
