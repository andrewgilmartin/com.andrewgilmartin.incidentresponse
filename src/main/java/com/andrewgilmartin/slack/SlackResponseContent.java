package com.andrewgilmartin.slack;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * SlackResponseContent is used to build a response message. This class uses the
 * fluent interface of method chaining to build the message. The message is
 * built from front to back. Slack messages have two parts. The first part is a
 * simple textual message. The second part is a list of attachment messages. An
 * attachment message has more features such as a title and a colored left
 * vertical border. For example,
 *
 * {@code
 *
 * getResponseContent()
 *   .text("My simple response")
 *   .attachment()
 *     .color(Color.BLUE)
 *     .title("My first attachment")
 *     .text("the")
 *     .space()
 *     .text("first attachment text");
 * }
 *
 */
public interface SlackResponseContent {

    /**
     * Start a new attachment.
     */
    SlackResponseContent attachment();

    /**
     * Set the title for the attachment. Only the last setting is retained.
     */
    SlackResponseContent title(String text);

    /**
     * Set the color for the attachment. Only the last setting is retained.
     */
    SlackResponseContent color(Color color);

    /**
     * Add a space to the message or attachment text iff there is preceeding
     * text.
     */
    SlackResponseContent space();

    /**
     * Add a newline to the message or attachment text iff there is preceeding
     * text.
     */
    SlackResponseContent line();

    /**
     * Adds the object's string value to the message or attachment text.
     */
    SlackResponseContent text(Object object);

    /**
     * Add the date's string value to the message or attachment text.
     */
    SlackResponseContent text(Date date);

    /**
     * Add the timestamp's string value to the message or attachment text.
     */
    SlackResponseContent text(Timestamp timestamp);

    /**
     * Add the number's string value to the message or attachment text.
     */
    SlackResponseContent text(Number number);

    /**
     * Add the text to the message or attachment text.
     */
    SlackResponseContent text(String text);

    /**
     * Add the text to the message or attachment text. The formating uses the
     * String.format().
     */
    SlackResponseContent textf(String format, Object... parameters);

    /**
     * Add the alttext and link to the message or attachment text.
     */
    SlackResponseContent link(String alttext, String url);

    /**
     * Add the link to the message or attachment text.
     */
    SlackResponseContent link(String url);

    /**
     * Add the alttext and image to the message or attachment text.
     */
    SlackResponseContent image(String alttext, String url);

    /**
     * Add the image to the message or attachment text.
     */
    SlackResponseContent image(String url);

    /**
     * Add the Slack user to the message or attachment text.
     */
    SlackResponseContent user(String id, String name);

    /**
     * Add the Slack user to the message or attachment text.
     */
    SlackResponseContent user(SlackUser user);

    /**
     * Add the named emoji to the message or attachment text.
     */
    SlackResponseContent emoji(String name);

    /**
     * Add the output of formating the given item to the message or attachment
     * text.
     */
    <T> SlackResponseContent accept(BiConsumer<SlackResponseContent, T> formatter, T item);

    /**
     * Add the output of formating the given items to the message or attachment
     * text.
     */
    <T extends Iterable<U>, U> SlackResponseContent acceptAll(BiConsumer<SlackResponseContent, U> formatter, T items);

    /**
     * Add the output of formating the given items to the message or attachment
     * text.
     */
    <T extends Stream<U>, U> SlackResponseContent acceptAll(BiConsumer<SlackResponseContent, U> formatter, T items);
}
