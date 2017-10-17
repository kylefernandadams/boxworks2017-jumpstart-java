package com.box.sdk.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxUser;
import com.box.sdk.CreateUserParams;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;


public final class Exercise2_CreateFolder {

    private static final String EXTERNAL_APP_USER_ID = "MY\rexternalId";
    private static final int MAX_CACHE_ENTRIES = 100;

    private Exercise2_CreateFolder() { }

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

		//Print out BOX App User Name
        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s!\n\n", userInfo.getName());
        
        //Create Folder in Box App User Root
        BoxFolder boxFolder = BoxFolder.getRootFolder(api);
        BoxFolder.Info createdFolder = boxFolder.createFolder("Exercise2_Folder");
        System.out.format("Created new folder with ID: %s!\n\n", createdFolder.getID());
    }
}