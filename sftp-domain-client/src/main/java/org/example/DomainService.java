package org.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class DomainService {

    private final JsonParser jsonParser;
    private final String jsonPath;
    private final String jsonFilename;

    public DomainService() {
        this.jsonParser = new JsonParser();
        this.jsonPath = AppConfig.get("local.download.dir");
        this.jsonFilename = AppConfig.get("local.hosts.filename");
    }

    private String filePath() {
        return Paths.get(jsonPath, jsonFilename).toString();
    }

    public Map<String, String> getAllMapping() throws IOException {
        return new TreeMap<>(jsonParser.parse(filePath()));
    }

    public Optional<String> getIp(String domain) throws IOException {
        String key = normalizeDomain(domain);
        return Optional.ofNullable(jsonParser.parse(filePath()).get(key));
    }

    public Optional<String> getDomain(String ip) throws IOException {
        String value = ip == null ? "" : ip.trim();
        Map<String, String> map = jsonParser.parse(filePath());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(value)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public void addPair(String domainRaw, String ipRaw) throws IOException {
        String domain = normalizeDomain(domainRaw);
        String ip = ipRaw == null ? "" : ipRaw.trim();

        if (domain.isEmpty()) {
            throw new IllegalArgumentException("домен не может быть пустым");
        }
        if (!IpValidator.isValidIPv4(ip)) {
            throw new IllegalArgumentException("некорректный IPv4-адрес: " + ipRaw);
        }

        Map<String, String> map = jsonParser.parse(filePath());

        if (map.containsKey(domain)) {
            throw new IllegalArgumentException("такой домен уже существует: " + domain);
        }
        if (map.containsValue(ip)) {
            throw new IllegalArgumentException("такой IP-адрес уже используется: " + ip);
        }

        map.put(domain, ip);
        jsonParser.buildJson(map, filePath());
    }

    /** @return true, если пара была найдена и удалена. */
    public boolean removePair(String identifierRaw) throws IOException {
        if (identifierRaw == null || identifierRaw.trim().isEmpty()) {
            return false;
        }

        String identifier = identifierRaw.trim();
        String domainKey = normalizeDomain(identifier);

        Map<String, String> map = jsonParser.parse(filePath());

        if (map.containsKey(domainKey)) {
            map.remove(domainKey);
            jsonParser.buildJson(map, filePath());
            return true;
        }

        String foundDomain = null;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(identifier)) {
                foundDomain = entry.getKey();
                break;
            }
        }

        if (foundDomain == null) {
            return false;
        }

        map.remove(foundDomain);
        jsonParser.buildJson(map, filePath());
        return true;
    }

    private String normalizeDomain(String domain) {
        return domain == null ? "" : domain.trim().toLowerCase();
    }
}