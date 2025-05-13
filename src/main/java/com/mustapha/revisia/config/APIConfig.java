package com.mustapha.revisia.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIConfig {

    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    private static void loadProperties() {
        if (!loaded) {
            try (InputStream input = APIConfig.class.getClassLoader().getResourceAsStream("api.properties")) {
                if (input == null) {
                    System.err.println("Impossible de trouver api.properties");
                    return;
                }

                properties.load(input);
                loaded = true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static boolean isConfigValid() {
        loadProperties();
        String apiKey = getOpenAIApiKey();
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_openai_api_key_here");
    }
    public static boolean isOpenAIEnabled() {
        loadProperties();
        String enabled = properties.getProperty("openai.enabled", "false");
        return Boolean.parseBoolean(enabled);
    }
    public static String getOpenAIApiKey() {
        loadProperties();
        return properties.getProperty("openai.api.key");
    }
    /**
     * Force le rechargement des propriétés depuis le fichier
     */
    public static void reloadProperties() {
        loaded = false;
        loadProperties();
    }
}