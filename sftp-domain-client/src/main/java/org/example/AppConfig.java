package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final String[] REQUIRED_KEYS = {
            "sftp.remote.path",
            "local.download.dir",
            "local.hosts.filename"
    };

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Не найден файл конфигурации " + CONFIG_FILE + " в classpath");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось загрузить " + CONFIG_FILE, ex);
        }

        for (String key : REQUIRED_KEYS) {
            String value = properties.getProperty(key);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalStateException("В " + CONFIG_FILE + " отсутствует обязательный параметр: " + key);
            }
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}