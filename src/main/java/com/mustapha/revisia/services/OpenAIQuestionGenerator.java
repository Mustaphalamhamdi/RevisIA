package com.mustapha.revisia.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Subject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class OpenAIQuestionGenerator {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAIQuestionGenerator(String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Génère des questions à choix multiples à partir du texte fourni
     *
     * @param text Le texte source pour générer des questions
     * @param count Nombre de questions à générer
     * @param subject La matière associée
     * @param document Le document associé
     * @return Liste de questions générées
     */
    public List<Question> generateQuestionsFromText(String text, int count, Subject subject, Document document) {
        List<Question> questions = new ArrayList<>();

        try {
            // Préparer le texte d'entrée (limiter la taille pour rester dans les limites de l'API)
            String inputText = text.length() > 4000 ? text.substring(0, 4000) : text;

            // Créer la requête pour l'API OpenAI
            String prompt = createQuestionGenerationPrompt(inputText, count);
            String jsonResponse = callOpenAIApi(prompt);

            // Parser la réponse JSON
            JsonNode responseNode = objectMapper.readTree(jsonResponse);
            String content = responseNode.path("choices").path(0).path("message").path("content").asText();

            // Parser le contenu JSON des questions générées
            JsonNode questionsArray = objectMapper.readTree(content);

            // Créer des objets Question à partir des données JSON
            for (JsonNode questionNode : questionsArray) {
                String questionText = questionNode.path("question").asText();
                List<String> options = new ArrayList<>();

                // Ajouter les options
                JsonNode optionsNode = questionNode.path("options");
                for (JsonNode optionNode : optionsNode) {
                    options.add(optionNode.asText());
                }

                int correctIndex = questionNode.path("correctIndex").asInt();

                // Créer l'objet Question
                Question question = new Question();
                question.setSubject(subject);
                question.setDocument(document);
                question.setQuestionText(questionText);
                question.setOptions(options);
                question.setCorrectOptionIndex(correctIndex);
                question.setUserCreated(false);

                // Définir la difficulté si disponible
                if (questionNode.has("difficulty")) {
                    question.setDifficultyLevel(questionNode.path("difficulty").asInt(3));
                } else {
                    question.setDifficultyLevel(3); // Difficulté moyenne par défaut
                }

                questions.add(question);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la génération de questions avec OpenAI: " + e.getMessage());
        }

        return questions;
    }

    /**
     * Crée le prompt pour générer des questions à choix multiples
     */
    private String createQuestionGenerationPrompt(String text, int count) {
        return "Génère " + count + " questions à choix multiples en français pour un quiz éducatif basé sur le texte suivant. "
                + "Chaque question doit avoir 4 options, dont une seule correcte. "
                + "Le format de réponse doit être un tableau JSON avec la structure: "
                + "[{\"question\": \"...\", \"options\": [\"...\", \"...\", \"...\", \"...\"], \"correctIndex\": 0-3, \"difficulty\": 1-5}].\n\n"
                + "Texte: " + text;
    }

    /**
     * Appelle l'API OpenAI avec le prompt donné
     */
    private String callOpenAIApi(String prompt) throws IOException, InterruptedException {
        // Créer le corps de la requête
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-3.5-turbo");

        ArrayNode messagesArray = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messagesArray.add(message);

        requestBody.set("messages", messagesArray);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        // Convertir le corps en JSON
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        // Créer la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        // Envoyer la requête et récupérer la réponse
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}