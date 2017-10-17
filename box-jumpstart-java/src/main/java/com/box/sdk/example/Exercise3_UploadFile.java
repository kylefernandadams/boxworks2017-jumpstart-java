package com.box.sdk.example;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxResource;
import com.box.sdk.BoxUser;
import com.box.sdk.CreateUserParams;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.ProgressListener;


public final class Exercise3_UploadFile {

    private static final String EXTERNAL_APP_USER_ID = "MY\rexternalId";
    private static final String TARGET_FOLDER="Exercise2_Folder";
    private static final String TARGET_FILE="Box Works Jump Start Java.pptx";
    private static final int MAX_CACHE_ENTRIES = 100;

    private Exercise3_UploadFile() { }

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

		//Impersonate the target Box App User
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
        
        //Get Target Folder 
        BoxFolder targetFolder = new BoxFolder(api, targetFolderId);
        
        //Upload File
        FileInputStream stream = new FileInputStream(TARGET_FILE);
        final double fileSize = stream.getChannel().size();
        
        BoxFile.Info newFileInfo = targetFolder.uploadFile(stream, TARGET_FILE, 1024, new ProgressListener() {
        	
            public void onProgressChanged(long numBytes, long totalBytes) {
                double percentComplete = ( (numBytes) / (fileSize)) * 100;
                System.out.format("File uploaded %f of %d\n", percentComplete, 100);
            }
        });
        stream.close();
        
        System.out.println("File Created: " + newFileInfo.getID());
        
        
        
        

    }
}