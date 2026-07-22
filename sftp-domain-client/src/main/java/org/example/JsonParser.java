package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {

    // Каждый элемент массива "addresses" — простой блок { ... } без вложенных объектов
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{([^{}]*)}");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("\"domain\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern IP_PATTERN = Pattern.compile("\"ip\"\\s*:\\s*\"([^\"]*)\"");

    public Map<String, String> parse(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);

        if (!Files.exists(path)) {
            return new LinkedHashMap<>();
        }

        String jsonContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        Map<String, String> result = new LinkedHashMap<>();

        Matcher objectMatcher = OBJECT_PATTERN.matcher(jsonContent);
        while (objectMatcher.find()) {
            String block = objectMatcher.group(1);

            Matcher domainMatcher = DOMAIN_PATTERN.matcher(block);
            Matcher ipMatcher = IP_PATTERN.matcher(block);

            if (domainMatcher.find() && ipMatcher.find()) {
                String domain = domainMatcher.group(1).trim().toLowerCase();
                String ip = ipMatcher.group(1).trim();

                if (domain.isEmpty()) {
                    System.err.println("Пропущена запись без домена: " + block.trim());
                    continue;
                }
                if (!IpValidator.isValidIPv4(ip)) {
                    System.err.println("Пропущена запись с некорректным IP: " + block.trim());
                    continue;
                }
                result.put(domain, ip);
            }
        }

        return result;
    }

    public void buildJson(Map<String, String> map, String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"addresses\": [\n");

        int size = map.size();
        int count = 0;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            count++;
            sb.append("    {\n");
            sb.append("      \"domain\": \"").append(entry.getKey()).append("\",\n");
            sb.append("      \"ip\": \"").append(entry.getValue()).append("\"\n");
            sb.append("    }");
            if (count < size) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");

        Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}