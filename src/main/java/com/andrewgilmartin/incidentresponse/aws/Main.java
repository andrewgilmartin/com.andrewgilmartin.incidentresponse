package com.andrewgilmartin.incidentresponse.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.AWSSecretsManagerException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.andrewgilmartin.incidentresponse.IncidentResponseSlackApp;
import com.andrewgilmartin.slack.httpserver.HttpServerSlackServer;
import com.andrewgilmartin.util.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String... args) throws Exception {

        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProviderChain(
                new EnvironmentVariableCredentialsProvider(),
                new ProfileCredentialsProvider("com.andrewgilmartin.incidentresponse"),
                new EC2ContainerCredentialsProviderWrapper()
        );

        Map<String, String> secrets = getSecrets(awsCredentialsProvider, "com.andrewgilmartin.incidentresponse.config");
        String slackVerificationToken = secrets.get("slack.verification-token");
        String awsSimpleDbDomain = secrets.get("aws.simpledb.domain");

        IncidentResponseSlackApp slackApp = new IncidentResponseSlackApp(
                new AwsController(awsSimpleDbDomain, awsCredentialsProvider),
                slackVerificationToken
        );

        HttpServerSlackServer server = new HttpServerSlackServer(8080, "/", slackApp);
        server.run();
    }

    private static Map<String, String> getSecrets(AWSCredentialsProvider credentialsProvider, String secretId) {
        AWSSecretsManager manager = AWSSecretsManagerClientBuilder.standard().withCredentials(credentialsProvider).build();
        try {
            GetSecretValueResult value = manager.getSecretValue(new GetSecretValueRequest().withSecretId(secretId));
            if (value != null) {
                Map<String, String> secretMap = new ObjectMapper().readValue(value.getSecretString(), HashMap.class);
                return secretMap;
            }
        } catch (AWSSecretsManagerException | InvalidParameterException | IOException e) {
            logger.error(e, "unable to get secrets");
        }
        return Collections.emptyMap();
    }
}
