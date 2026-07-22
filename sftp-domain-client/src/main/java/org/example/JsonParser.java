package org.example;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "\"domain\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"ip\"\\s*:\\s*\"([^\"]+)\""
    );

    public Map<String, String> parse(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);

        String jsonContent = new String(Files.readAllBytes(path));

        Map<String, String> parseJson = new HashMap<>();

        Matcher matcher = ADDRESS_PATTERN.matcher(jsonContent);

        while (matcher.find()) {
            String domain = matcher.group(1);
            String ip = matcher.group(2);

            parseJson.put(domain, ip);
        }

        return parseJson;
    }
}
