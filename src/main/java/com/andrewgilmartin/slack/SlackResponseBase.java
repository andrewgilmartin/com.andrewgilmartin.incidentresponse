package com.andrewgilmartin.slack;

import com.andrewgilmartin.slack.SlackResponseContentBase.Attachment;
import com.andrewgilmartin.util.JsonWriter;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SlackResponseBase implements SlackResponse {

    private boolean broadcast = false;
    private String errorText = null;
    private SlackResponseContentBase content;

    public SlackResponseBase(SlackResponseContentBase content) {
        this.content = content;
    }

    public SlackResponseBase() {
        this(new SlackResponseContentBase());
    }

    @Override
    public boolean isBroadcastResponse() {
        return broadcast;
    }

    @Override
    public void setBroadcastResponse(boolean broadcast) {
        this.broadcast = broadcast;
    }

    @Override
    public boolean isError() {
        return errorText != null;
    }

    @Override
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getErrorText() {
        return errorText;
    }

    @Override
    public SlackResponseContent getResponseContent() {
        return content;
    }

    public void render(OutputStream out) throws IOException {
        try (JsonWriter json = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            Iterator<Attachment> i = content.getAttachments().iterator();
            if (i.hasNext()) {
                Attachment attachment = i.next();
                json.hash();
                {
                    if (isError()) {
                        json.value("response_type", "ephemeral");
                        json.value("text", getErrorText());
                    } else {
                        json.value("response_type", isBroadcastResponse() ? "in_channel" : "ephemeral");
                        json.value("text", attachment.getText());
                        if (i.hasNext()) {
                            json.key("attachments");
                            json.array();
                            {
                                do {
                                    json.hash();
                                    {
                                        attachment = i.next();
                                        if (attachment.hasTitle()) {
                                            json.value("title", attachment.getTitle());
                                        }
                                        if (attachment.hasColor()) {
                                            json.value("color", toSlackColor(attachment.getColor()));
                                        }
                                        json.value("text", attachment.getText());
                                        json.value("mrkdwn", true);
                                    }
                                    json.end();
                                } while (i.hasNext());
                            }
                            json.end();
                        }
                    }
                }
                json.end();
            }
        }
    }

    protected String toSlackColor(Color color) {
        return "#" + toHex(color.getRed()) + toHex(color.getGreen()) + toHex(color.getBlue());
    }

    protected String toHex(int i) {
        return new String(
                new char[]{
                    HEX_DIGITS[(i >> 4) & 0x0F],
                    HEX_DIGITS[i & 0x0F]
                }
        );
    }

    protected final static char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

}
