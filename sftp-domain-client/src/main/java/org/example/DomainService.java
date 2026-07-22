package org.example;

import java.io.IOException;
import java.util.Map;

public class DomainService {
    private final JsonParser jsonParser;
    private final String jsonPath = AppConfig.get("local.download.dir");
    private final String jsonFilename = AppConfig.get("local.hosts.filename");

    public DomainService() {
        jsonParser = new JsonParser();
    }

    public Map<String, String> getAllMapping() throws IOException {
        return jsonParser.parse(jsonPath + jsonFilename);
    }

    public String getIp(String domain) throws IOException {
        return jsonParser.parse(jsonPath + jsonFilename).get(domain);
    }

    public String getDomain(String ip) throws IOException {
        Map<String, String> mapJson = jsonParser.parse(jsonPath + jsonFilename);
        for (Map.Entry<String, String> entry : mapJson.entrySet()) {
            if (entry.getValue().equals(ip)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
