package com.andrewgilmartin.slack;

public interface SlackResponse {

    /**
     * Should everyone on the channel receive the response?
     */
    boolean isBroadcastResponse();

    /**
     * Set if everyone on the channel should receive the response.
     */
    void setBroadcastResponse(boolean isBroadcast);

    /**
     * Was there an error in handling the request?
     */
    boolean isError();

    /**
     * Set the error message.
     */
    void setErrorText(String errorText);

    /**
     * Use the response content to compose the Slack response message.
     */
    SlackResponseContent getResponseContent();
    
}
