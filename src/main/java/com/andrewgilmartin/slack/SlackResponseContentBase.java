package com.andrewgilmartin.slack;

import com.andrewgilmartin.util.JsonWriter;
import java.awt.Color;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * See https://api.slack.com/docs/message-formatting
 */
public class SlackResponseContentBase implements SlackResponseContent {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private final List<Attachment> attachments = new LinkedList<>();
    private Attachment attachment;

    public static class Attachment {

        private Color color;
        private String title;
        private final StringBuilder text = new StringBuilder();

        public boolean hasTitle() {
            return title != null;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean hasColor() {
            return color != null;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public String getText() {
            return text.toString();
        }
    }

    public SlackResponseContentBase() {
        attachment();
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public SlackResponseContent attachment() {
        attachment = new Attachment();
        attachments.add(attachment);
        return this;
    }

    @Override
    public SlackResponseContent title(String title) {
        if (attachments.size() < 2) {
            throw new IllegalStateException("only attachments can have titles");
        }
        attachment.setTitle(JsonWriter.escapeXml(title));
        return this;
    }

    @Override
    public SlackResponseContent color(Color color) {
        if (attachments.size() < 2) {
            throw new IllegalStateException("only attachments can have color");
        }
        attachment.setColor(color);
        return this;
    }

    @Override
    public SlackResponseContent space() {
        if (attachment.text.length() > 0) {
            attachment.text.append(' ');
        }
        return this;
    }

    @Override
    public SlackResponseContent line() {
        if (attachment.text.length() > 0) {
            attachment.text.append('\n');
        }
        return this;
    }

    @Override
    public SlackResponseContent text(Object object) {
        attachment.text.append(JsonWriter.escapeXml(object.toString()));
        return this;
    }

    @Override
    public SlackResponseContent text(Date date) {
        attachment.text.append(DATE_FORMAT.format(date));
        return this;
    }

    @Override
    public SlackResponseContent text(Timestamp timestamp) {
        attachment.text.append(TIMESTAMP_FORMAT.format(timestamp));
        return this;
    }

    @Override
    public SlackResponseContent text(Number number) {
        attachment.text.append(number.toString());
        return this;
    }

    @Override
    public SlackResponseContent text(String text) {
        attachment.text.append(JsonWriter.escapeXml(text));
        return this;
    }

    @Override
    public SlackResponseContent textf(String format, Object... parameters) {
        attachment.text.append(JsonWriter.escapeXml(String.format(format, parameters)));
        return this;
    }

    @Override
    public SlackResponseContent link(String text, String url) {
        attachment.text.append('<').append(url).append('|').append(JsonWriter.escapeXml(text)).append('>');
        return this;
    }

    @Override
    public SlackResponseContent link(String url) {
        attachment.text.append('<').append(url).append('>');
        return this;
    }

    @Override
    public SlackResponseContent image(String text, String url) {
        attachment.text.append('<').append(url).append('|').append(JsonWriter.escapeXml(text)).append('>');
        return this;
    }

    @Override
    public SlackResponseContent image(String url) {
        attachment.text.append('<').append(url).append('>');
        return this;
    }

    @Override
    public SlackResponseContent user(String id, String name) {
        attachment.text.append('<').append(id).append('|').append(name).append('>');
        return this;
    }

    @Override
    public SlackResponseContent user(SlackUser user) {
        return user(user.getId(), user.getName());
    }

    @Override
    public SlackResponseContent emoji(String name) {
        attachment.text.append(':').append(name).append(':');
        return this;
    }

    @Override
    public <T> SlackResponseContent accept(BiConsumer<SlackResponseContent, T> formatter, T item) {
        formatter.accept(this, item);
        return this;
    }

    @Override
    public <T extends Iterable<U>, U> SlackResponseContent acceptAll(BiConsumer<SlackResponseContent, U> formatter, T items) {
        for (U item : items) {
            formatter.accept(this, item);
        }
        return this;
    }

    @Override
    public <T extends Stream<U>, U> SlackResponseContent acceptAll(BiConsumer<SlackResponseContent, U> formatter, T items) {
        items.forEach((item) -> formatter.accept(this, item));
        return this;
    }

}
