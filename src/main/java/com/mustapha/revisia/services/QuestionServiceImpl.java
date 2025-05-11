package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.QuestionDAO;
import com.mustapha.revisia.dao.QuestionDAOImpl;
import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.util.List;

public class QuestionServiceImpl implements QuestionService {

    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final AIQuestionGeneratorService aiQuestionGenerator = new AIQuestionGeneratorService();

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
        // Use the AI-powered question generator instead of the previous basic implementation
        List<Question> generatedQuestions = aiQuestionGenerator.generateQuestionsFromDocument(document, count);

        // Save generated questions to database
        for (Question question : generatedQuestions) {
            questionDAO.saveQuestion(question);
        }

        return generatedQuestions;
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
        // Generate double the number of questions requested
        List<Question> allQuestions = aiQuestionGenerator.generateQuestionsFromDocument(document, count * 2);

        // Analyze and filter questions to match the target difficulty
        List<Question> filteredQuestions = allQuestions.stream()
                .peek(q -> q.setDifficultyLevel(analyzeDifficulty(q)))
                .filter(q -> q.getDifficultyLevel() == targetDifficulty)
                .limit(count)
                .toList();

        // If we don't have enough questions at the target difficulty, fill with other questions
        if (filteredQuestions.size() < count) {
            int remaining = count - filteredQuestions.size();
            List<Question> additionalQuestions = allQuestions.stream()
                    .filter(q -> !filteredQuestions.contains(q))
                    .limit(remaining)
                    .toList();

            filteredQuestions.addAll(additionalQuestions);
        }

        // Save questions to database
        for (Question question : filteredQuestions) {
            questionDAO.saveQuestion(question);
        }

        return filteredQuestions;
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