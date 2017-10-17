package com.box.sdk.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxUser;
import com.box.sdk.BoxWebHook;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;


public final class Exercise6_CreateWebhookV2 {

    private static final String EXTERNAL_APP_USER_ID = "MY\rexternalId";
    private static final String TARGET_FOLDER = "Exercise2_Folder";
    private static final String TARGET_WEBHOOK_URL = "ADD_TARGET_WEBHOOK_URL_HERE";
    
    private static final int MAX_CACHE_ENTRIES = 100;
    
    private Exercise6_CreateWebhookV2() { }

    public static void main(String[] args) throws IOException {
    	 // Turn off logging to prevent polluting the output.
        Logger.getLogger("com.box.sdk").setLevel(Level.OFF);


        //It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
        //For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
        //implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
        IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

        Reader reader = new FileReader("box_config.json");
        BoxConfig boxConfig = BoxConfig.readFrom(reader);

        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(
                boxConfig, accessTokenCache);

        //Find existing Box App User
		Iterable<BoxUser.Info> userSearchResult = BoxUser.getAppUsersByExternalAppUserID(api, EXTERNAL_APP_USER_ID, BoxUser.ALL_FIELDS);
        String userId = userSearchResult.iterator().next().getID();

		//Impersonate the target Box App User Folder
		api = BoxDeveloperEditionAPIConnection.getAppUserConnection(userId, boxConfig, accessTokenCache);

        BoxFolder boxFolder = BoxFolder.getRootFolder(api);
        String targetFolderId = "";
            
        //Find target folder from Exercise2
        for (BoxItem.Info itemInfo : boxFolder) {
        	if(itemInfo instanceof BoxFolder.Info) {
        		if(itemInfo.getName().equalsIgnoreCase(TARGET_FOLDER)) {
                    targetFolderId = itemInfo.getID();
        			System.out.println("Found Folder: " + itemInfo.getID());
        		}
        	}
        }
        
        //Get Target Folder as Service Account
        BoxFolder targetFolder = new BoxFolder(api, targetFolderId);
        URL webhookURL = new URL(TARGET_WEBHOOK_URL);
        BoxWebHook.Info webhookInfo = BoxWebHook.create(targetFolder, webhookURL, BoxWebHook.Trigger.FILE_UPLOADED);
        
        System.out.println("Webhook Created, ID: " + webhookInfo.getID());
    }
}