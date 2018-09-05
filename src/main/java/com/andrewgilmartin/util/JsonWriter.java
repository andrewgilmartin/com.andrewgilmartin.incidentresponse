package com.andrewgilmartin.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

/**
 * A helper class to manually write out a JSON representation of a data
 * structure. For example, the JSON
 *
 * {@code 
 * 
 * { "a": "b", "c": [ 1, 2 ], "d": 3 } 
 * 
 * }
 *
 * is created with
 *
 * {@code
 * 
 * JsonWriter b = new JsonWriter(System.out);
 * b
 *  .hash()
 *      .value("a","b")
 *      .key("c")
 *          .array()
 *              .value(1)
 *              .value(2)
 *          .end()
 *      .value("d",3)
 *  .end();
 * 
 * }
 */
public class JsonWriter implements AutoCloseable {

    // runtime
    private Writer writer;
    private Deque<H> heirarchy = new LinkedList<>();
    private boolean surpressComma = false;

    public JsonWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    public JsonWriter hash() throws IOException {
        comma();
        writer.append("{\n");
        heirarchy.push(new H("\n}"));
        return this;
    }

    public JsonWriter array() throws IOException {
        comma();
        writer.append("[\n");
        heirarchy.push(new H("\n]"));
        return this;
    }

    public JsonWriter key(Object key) throws IOException {
        try {
            comma();
            quoteString(writer, key).append(": ");
            surpressComma = true;
            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JsonWriter value(Object value) throws IOException {
        comma();
        appendQuotedValue(value);

        return this;
    }

    public JsonWriter values(Object[] values) throws IOException {
        for (Object value : values) {
            comma();
            appendQuotedValue(value);
        }

        return this;
    }

    public JsonWriter values(Collection values) throws IOException {
        for (Object value : values) {
            comma();
            appendQuotedValue(value);
        }

        return this;
    }

    public JsonWriter rawValue(Object value) throws IOException {
        comma();
        appendUnquotedValue(value);

        return this;
    }

    public JsonWriter value(Object key, Object value) {
        try {
            comma();
            quoteString(writer, key).append(": ");
            appendQuotedValue(value);

            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JsonWriter values(Object key, Collection values) {
        try {
            comma();
            quoteString(writer, key).append(": ");
            surpressComma = true;
            array();
            for (Object value : values) {
                value(value);
            }
            end();
            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JsonWriter rawValue(Object key, Object value) {
        try {
            comma();
            quoteString(writer, key).append(": ");
            appendUnquotedValue(value);

            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JsonWriter end() throws IOException {
        writer.append(heirarchy.pop().ending);
        return this;
    }

    private JsonWriter appendQuotedValue(Object value) {
        try {
            if (value != null) {
                if (value instanceof Boolean) {
                    writer.append(value.toString().toLowerCase());
                } else if (value instanceof Number) {
                    writer.append(value.toString());
                } else {
                    quoteString(writer, value);
                }
            } else {
                writer.append("null");
            }
            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private JsonWriter appendUnquotedValue(Object value) throws IOException {
        if (value != null) {
            if (value instanceof Boolean) {
                writer.append(value.toString().toLowerCase());
            } else if (value instanceof Number) {
                writer.append(value.toString());
            } else {
                writer.append(value.toString());
            }
        } else {
            writer.append("null");
        }
        return this;
    }

    public static Appendable escapeString(Writer writer, String text) throws IOException {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c <= '~') {
                writer.append(ESCAPED_ASCII[c]);
            } else {
                writer.append("\\u");
                writer.append(HEX_DIGITS[(c & 0xF000) >>> 12]);
                writer.append(HEX_DIGITS[(c & 0x0F00) >>> 8]);
                writer.append(HEX_DIGITS[(c & 0x00F0) >>> 4]);
                writer.append(HEX_DIGITS[(c & 0x000F)]);
            }
        }
        return writer;
    }

    public static String escapeString(String text) throws IOException {
        return escapeString(new StringWriter(), text).toString();
    }

    public static Appendable quoteString(Writer writer, String text) throws IOException {
        writer.append("\"");
        escapeString(writer, text);
        writer.append("\"");
        return writer;
    }

    public static Appendable quoteString(Writer writer, Object text) throws IOException {
        writer.append("\"");
        escapeString(writer, text.toString());
        writer.append("\"");
        return writer;
    }

    public static String quoteString(String text) throws IOException {
        return quoteString(new StringWriter(), text).toString();
    }

    public static String escapeXml(String text) {
        // limited and mostly to help with Slack!
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
    
    private static char[] HEX_DIGITS = new char[]{
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'a',
        'b',
        'c',
        'd',
        'e',
        'f'
    };

    private static final String[] ESCAPED_ASCII = new String[]{
        "\\u0000",
        "\\u0001",
        "\\u0002",
        "\\u0003",
        "\\u0004",
        "\\u0005",
        "\\u0006",
        "\\u0007",
        "\\b",
        "\\t",
        "\\n",
        "\\u000b",
        "\\f",
        "\\r",
        "\\u000e",
        "\\u000f",
        "\\u0010",
        "\\u0011",
        "\\u0012",
        "\\u0013",
        "\\u0014",
        "\\u0015",
        "\\u0016",
        "\\u0017",
        "\\u0018",
        "\\u0019",
        "\\u001a",
        "\\u001b",
        "\\u001c",
        "\\u001d",
        "\\u001e",
        "\\u001f",
        " ",
        "!",
        "\\\"",
        "#",
        "$",
        "%",
        "&",
        "'",
        "(",
        ")",
        "*",
        "+",
        ",",
        "-",
        ".",
        "/",
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        ":",
        ";",
        "<",
        "=",
        ">",
        "?",
        "@",
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z",
        "[",
        "\\\\",
        "]",
        "^",
        "_",
        "`",
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z",
        "{",
        "|",
        "}",
        "~",
        "\\u007f"
    };

    private JsonWriter comma() throws IOException {
        if (!surpressComma && !heirarchy.isEmpty()) {
            H h = heirarchy.peek();
            if (h.count > 0) {
                writer.append(",\n "); // NOTE the trailing space!
            } else {
                writer.append(" ");
            }
            h.count += 1;
        }
        surpressComma = false;
        return this;
    }

    private static class H {

        int count;
        String ending;

        public H(int count, String ending) {
            this.count = count;
            this.ending = ending;
        }

        public H(String ending) {
            this(0, ending);
        }
    }

}

// END
