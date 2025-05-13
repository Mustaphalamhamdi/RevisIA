package com.mustapha.revisia.services;

import com.mustapha.revisia.config.APIConfig;
import com.mustapha.revisia.dao.QuestionDAO;
import com.mustapha.revisia.dao.QuestionDAOImpl;
import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuestionServiceImpl implements QuestionService {

    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final AIQuestionGeneratorService aiQuestionGenerator = new AIQuestionGeneratorService();
    private OpenAIQuestionGenerator openAIGenerator;

    // Constructeur qui initialise le générateur OpenAI
    public QuestionServiceImpl() {
        try {
            if (APIConfig.isConfigValid() && APIConfig.isOpenAIEnabled()) {
                String apiKey = APIConfig.getOpenAIApiKey();
                openAIGenerator = new OpenAIQuestionGenerator(apiKey);
                System.out.println("OpenAI intégré avec succès au service de génération de questions");
            } else {
                System.out.println("OpenAI non activé ou configuration invalide - utilisation du générateur de base");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du générateur OpenAI: " + e.getMessage());
        }
    }

    @Override
    public void saveQuestion(Question question) {
        questionDAO.saveQuestion(question);
    }

    @Override
    public Question getQuestionById(Long id) {
        return questionDAO.getQuestionById(id);
    }

    @Override
    public List<Question> getQuestionsBySubject(Subject subject) {
        return questionDAO.getQuestionsBySubject(subject);
    }

    @Override
    public List<Question> getQuestionsByDocument(Document document) {
        return questionDAO.getQuestionsByDocument(document);
    }

    @Override
    public List<Question> getQuestionsBySubjectAndDocument(Subject subject, Document document) {
        return questionDAO.getQuestionsBySubjectAndDocument(subject, document);
    }

    @Override
    public List<Question> getRandomQuestionsBySubject(Subject subject, int limit) {
        return questionDAO.getRandomQuestionsBySubject(subject, limit);
    }

    @Override
    public List<Question> getRandomQuestionsByDocument(Document document, int limit) {
        return questionDAO.getRandomQuestionsByDocument(document, limit);
    }

    @Override
    public List<Question> generateQuestionsFromDocument(Document document, int count) {
        try {
            // Vérifier si OpenAI est disponible et activé
            if (openAIGenerator != null && APIConfig.isOpenAIEnabled()) {
                System.out.println("Génération de questions avec OpenAI pour le document: " + document.getTitle());

                // Extraire le texte du PDF
                String text = extractTextFromPDF(document.getFilePath());

                // Générer des questions avec OpenAI
                List<Question> openAIQuestions = openAIGenerator.generateQuestionsFromText(
                        text, count, document.getSubject(), document);

                // Valider les questions générées
                List<Question> validatedQuestions = validateQuestions(openAIQuestions);

                // Si des questions valides ont été générées, les utiliser
                if (validatedQuestions != null && !validatedQuestions.isEmpty()) {
                    System.out.println("OpenAI a généré " + validatedQuestions.size() + " questions valides");

                    // Sauvegarder les questions dans la base de données
                    for (Question question : validatedQuestions) {
                        questionDAO.saveQuestion(question);
                    }

                    return validatedQuestions;
                } else {
                    System.out.println("OpenAI n'a pas généré de questions valides, utilisation du générateur de base");
                }
            }

            // Si OpenAI n'est pas disponible ou n'a pas généré de questions valides,
            // utiliser le générateur par défaut
            List<Question> fallbackQuestions = aiQuestionGenerator.generateQuestionsFromDocument(document, count);

            // Sauvegarder les questions dans la base de données
            for (Question question : fallbackQuestions) {
                questionDAO.saveQuestion(question);
            }

            return fallbackQuestions;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la génération de questions: " + e.getMessage());

            // En cas d'erreur, utiliser le générateur par défaut
            return aiQuestionGenerator.generateQuestionsFromDocument(document, count);
        }
    }

    /**
     * Valide les questions générées par OpenAI
     */
    private List<Question> validateQuestions(List<Question> questions) {
        List<Question> validQuestions = new ArrayList<>();

        if (questions == null) return validQuestions;

        for (Question question : questions) {
            // Vérifier que la question a un texte valide
            if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
                continue;
            }

            // Vérifier que la question a des options
            if (question.getOptions() == null || question.getOptions().isEmpty()) {
                continue;
            }

            // Vérifier qu'il y a au moins 2 options
            if (question.getOptions().size() < 2) {
                // Ajouter des options par défaut si nécessaire
                if (question.getOptions().size() == 1) {
                    String correctOption = question.getOptions().get(0);
                    question.getOptions().add("Ce n'est pas correct");
                    question.getOptions().add("Aucune de ces réponses");
                    question.setCorrectOptionIndex(0);  // La première option est correcte
                } else {
                    continue;
                }
            }

            // Vérifier que l'index de la réponse correcte est valide
            if (question.getCorrectOptionIndex() == null ||
                    question.getCorrectOptionIndex() < 0 ||
                    question.getCorrectOptionIndex() >= question.getOptions().size()) {
                question.setCorrectOptionIndex(0);  // Définir la première option comme correcte par défaut
            }

            // La question est valide
            validQuestions.add(question);
        }

        return validQuestions;
    }

    /**
     * Extrait le texte d'un fichier PDF
     */
    private String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    @Override
    public void updateQuestion(Question question) {
        questionDAO.updateQuestion(question);
    }

    @Override
    public void deleteQuestion(Question question) {
        questionDAO.deleteQuestion(question);
    }

    /**
     * Analyze a question to determine its difficulty level
     * @param question The question to analyze
     * @return Difficulty level (1-5, where 5 is most difficult)
     */
    public int analyzeDifficulty(Question question) {
        int difficulty = 3; // Default medium difficulty

        // Analyze question length - longer questions tend to be more complex
        String questionText = question.getQuestionText();
        if (questionText.length() > 150) {
            difficulty++;
        } else if (questionText.length() < 50) {
            difficulty--;
        }

        // Analyze number of options - more options = harder to choose
        List<String> options = question.getOptions();
        if (options.size() > 4) {
            difficulty++;
        }

        // Look for keywords that indicate complexity
        String[] complexityWords = {"analyse", "évalue", "compare", "explique", "critique", "justifie"};
        for (String word : complexityWords) {
            if (questionText.toLowerCase().contains(word)) {
                difficulty++;
                break; // Only increment once for complexity words
            }
        }

        // Look for keywords that indicate simplicity
        String[] simplicityWords = {"liste", "nomme", "identifie", "décris", "définis"};
        for (String word : simplicityWords) {
            if (questionText.toLowerCase().contains(word)) {
                difficulty--;
                break; // Only decrement once for simplicity words
            }
        }

        // Ensure difficulty stays within bounds
        difficulty = Math.max(1, Math.min(5, difficulty));

        return difficulty;
    }

    /**
     * Generate questions with a specific difficulty level
     * @param document The document to generate questions from
     * @param count Number of questions to generate
     * @param targetDifficulty Target difficulty level (1-5)
     * @return List of questions with the target difficulty
     */
    public List<Question> generateQuestionsWithDifficulty(Document document, int count, int targetDifficulty) {
        try {
            List<Question> allQuestions;

            // Utiliser OpenAI si disponible
            if (openAIGenerator != null && APIConfig.isOpenAIEnabled()) {
                String text = extractTextFromPDF(document.getFilePath());
                allQuestions = openAIGenerator.generateQuestionsFromText(
                        text, count * 2, document.getSubject(), document);

                if (allQuestions == null || allQuestions.isEmpty()) {
                    // Fallback au générateur par défaut
                    allQuestions = aiQuestionGenerator.generateQuestionsFromDocument(document, count * 2);
                }
            } else {
                // Utiliser le générateur par défaut
                allQuestions = aiQuestionGenerator.generateQuestionsFromDocument(document, count * 2);
            }

            // Analyser et filtrer les questions pour correspondre à la difficulté cible
            // Au lieu de ceci:
            List<Question> filteredQuestions = allQuestions.stream()
                    .peek(q -> q.setDifficultyLevel(analyzeDifficulty(q)))
                    .filter(q -> q.getDifficultyLevel() == targetDifficulty)
                    .limit(count)
                    .toList();

            if (filteredQuestions.size() < count) {
                int remaining = count - filteredQuestions.size();
                List<Question> additionalQuestions = allQuestions.stream()
                        .filter(q -> !filteredQuestions.contains(q))
                        .limit(remaining)
                        .toList();

                filteredQuestions.addAll(additionalQuestions); // Erreur ici
            }

// Utilisez plutôt ceci:
            List<Question> initialFilteredQuestions = allQuestions.stream()
                    .peek(q -> q.setDifficultyLevel(analyzeDifficulty(q)))
                    .filter(q -> q.getDifficultyLevel() == targetDifficulty)
                    .limit(count)
                    .toList();

            List<Question> finalQuestions = new ArrayList<>(initialFilteredQuestions);

            if (initialFilteredQuestions.size() < count) {
                int remaining = count - initialFilteredQuestions.size();
                List<Question> additionalQuestions = allQuestions.stream()
                        .filter(q -> !initialFilteredQuestions.contains(q))
                        .limit(remaining)
                        .toList();

                finalQuestions.addAll(additionalQuestions);
            }

// Sauvegarder les questions dans la base de données
            for (Question question : finalQuestions) {
                questionDAO.saveQuestion(question);
            }

            return finalQuestions;
        } catch (Exception e) {
            e.printStackTrace();
            return aiQuestionGenerator.generateQuestionsFromDocument(document, count);
        }
    }

    /**
     * Generate adaptive questions based on user performance
     * @param document The document to generate questions from
     * @param count Number of questions to generate
     * @param user The user to generate questions for
     * @return List of questions adapted to the user's level
     */
    public List<Question> generateAdaptiveQuestions(Document document, int count, User user) {
        // This would be implemented by:
        // 1. Analyzing user's past quiz performances
        // 2. Identifying their strengths and weaknesses
        // 3. Generating questions that target areas they need to improve

        // Simplified implementation for now - just generates questions with appropriate difficulty
        int userPerformanceLevel = estimateUserLevel(user, document.getSubject());

        // Map 1-5 performance level to a difficulty level
        // If user is struggling (level 1-2), give easier questions (2-3)
        // If user is doing well (level 4-5), give harder questions (4-5)
        int targetDifficulty = Math.min(5, Math.max(1, userPerformanceLevel + 1));

        return generateQuestionsWithDifficulty(document, count, targetDifficulty);
    }

    /**
     * Estimate a user's knowledge level in a subject
     * @param user The user to evaluate
     * @param subject The subject to evaluate
     * @return Estimated level (1-5, where 5 is highest)
     */
    private int estimateUserLevel(User user, Subject subject) {
        // In a real implementation, this would analyze:
        // - Quiz scores
        // - Study session confidence ratings
        // - Time spent studying

        // Default to middle level for now
        return 3;
    }
}