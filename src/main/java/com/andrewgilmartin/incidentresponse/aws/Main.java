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
        String slackVerificationToken = null;
        int port = 5000;
        String path = "/ir";
        String awsSimpleDbDomain = null;
        String awsSecretName = "com.andrewgilmartin.incidentresponse.config";
        String awsCredentialsProfile = "com.andrewgilmartin.incidentresponse";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                    port = Integer.parseInt(args[i + 1]);
                    i += 1;
                    break;
                case "--path":
                    path = args[i + 1];
                    i += 1;
                    break;
                case "--token":
                    slackVerificationToken = args[i + 1];
                    i += 1;
                    break;
                case "--domain":
                    awsSimpleDbDomain = args[i + 1];
                    i += 1;
                    break;
                case "--secret":
                    awsSecretName = args[i + 1];
                    i += 1;
                    break;
                case "--profile":
                    awsCredentialsProfile = args[i + 1];
                    i += 1;
                    break;
                default:
                    System.err.printf(
                            "usage: %s "
                            + "--port http-port-number "
                            + "--path url-path "
                            + "--token slack-verification-token "
                            + "--domain aws-simpledb-name "
                            + "--profile aws-profile-name "
                            + "--secret aws-secret-name",
                            Main.class.getName()
                    );
                    System.exit(1);
            }
        }

        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProviderChain(
                new EnvironmentVariableCredentialsProvider(),
                new ProfileCredentialsProvider(awsCredentialsProfile),
                new EC2ContainerCredentialsProviderWrapper()
        );

        if (slackVerificationToken == null || awsSimpleDbDomain == null) {
            Map<String, String> secrets = getSecrets(awsCredentialsProvider, awsSecretName);
            if (slackVerificationToken == null) {
                slackVerificationToken = secrets.get("slack.verification-token");
            }
            if (awsSimpleDbDomain == null) {
                awsSimpleDbDomain = secrets.get("aws.simpledb.domain");
            }
        }

        IncidentResponseSlackApp slackApp = new IncidentResponseSlackApp(
                new AwsController(awsSimpleDbDomain, awsCredentialsProvider),
                slackVerificationToken
        );

        HttpServerSlackServer server = new HttpServerSlackServer(port, path, slackApp);
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
