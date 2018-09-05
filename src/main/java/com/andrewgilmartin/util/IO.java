package com.andrewgilmartin.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IO {

    private static final Logger logger = Logger.getLogger(IO.class);

    public static String slurp(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            for (int l = r.read(buffer); l != -1; l = r.read(buffer)) {
                sb.append(buffer, 0, l);
            }
        }
        return sb.toString();
    }

    public static int findLastNumberedFile(File directory, String baseName) {
        int last = 0;
        if (directory.isDirectory()) {
            Pattern p = Pattern.compile(baseName + "\\.(\\d+)");
            for (String name : directory.list()) {
                Matcher m = p.matcher(name);
                if (m.matches()) {
                    int i = Integer.parseInt(m.group(1));
                    if (i > last) {
                        last = i;
                    }
                }
            }
        }
        return last;
    }
}

// END

