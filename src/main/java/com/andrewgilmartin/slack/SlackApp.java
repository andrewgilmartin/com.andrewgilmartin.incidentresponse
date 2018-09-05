package com.andrewgilmartin.slack;

/**
 * A Slack slash command application.
 */
public interface SlackApp {

    /**
     * Get the Slack verification token for this application.
     * @return 
     */
    String getVerificationToken();

    /**
     * Handle the request and its response.
     */
    void request(SlackRequest request, SlackResponse response);
}

// END

