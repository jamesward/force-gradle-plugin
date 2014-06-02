package com.jamesward.forcegradleplugin;

import com.sforce.soap.metadata.*;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class ForceMetadataUtil {

    public static final Double API_VERSION = 29.0;

    public static final String LOGIN_URL = "https://login.salesforce.com/services/Soap/u/" + API_VERSION;


    public static MetadataConnection createMetadataConnection(
            final String username,
            final String password,
            final String loginUrl) throws ConnectionException {

        final ConnectorConfig loginConfig = new ConnectorConfig();
        loginConfig.setAuthEndpoint(loginUrl);
        loginConfig.setServiceEndpoint(loginUrl);
        loginConfig.setManualLogin(true);
        LoginResult loginResult = (new PartnerConnection(loginConfig)).login(username, password);

        final ConnectorConfig metadataConfig = new ConnectorConfig();
        metadataConfig.setServiceEndpoint(loginResult.getMetadataServerUrl());
        metadataConfig.setSessionId(loginResult.getSessionId());
        return new MetadataConnection(metadataConfig);
    }

    // Block and poll for the result.  I feel dirty.
    public static AsyncResult waitForResult(MetadataConnection metadataConnection, String asyncResultId, int maxPolls, int millisPerPoll) throws Exception {

        int poll = 0;

        AsyncResult asyncResult = null;

        while (!(asyncResult = metadataConnection.checkStatus(new String[]{asyncResultId})[0]).isDone()) {
            Thread.sleep(millisPerPoll);

            if (poll++ > maxPolls) {
                throw new Exception("Request timed out.");
            }
        }

        if (asyncResult.getState() != AsyncRequestState.Completed) {
            throw new Exception("Could not retrieve metadata: " + asyncResult.getMessage());
        }

        return asyncResult;
    }

}