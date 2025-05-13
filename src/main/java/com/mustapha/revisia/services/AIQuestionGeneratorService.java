package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Subject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIQuestionGeneratorService {

    private static final Random random = new Random();

    public List<Question> generateQuestionsFromDocument(Document document, int count) {
        List<Question> questions = new ArrayList<>();

        try {
            // Extract and preprocess text from PDF
            String content = extractAndPreprocessText(document.getFilePath());

            // Identify key sentences using AI techniques
            List<String> keySentences = identifyKeySentences(content, count * 3); // Extract more than needed

            // Create different types of questions
            for (int i = 0; i < Math.min(count, keySentences.size()); i++) {
                String sentence = keySentences.get(i);
                Question question = createSmartQuestion(sentence, document, i % 4); // Rotate through question types
                if (question != null) {
                    questions.add(question);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // If we couldn't generate enough questions, fill with basic ones
        while (questions.size() < count) {
            questions.add(createBasicQuestion(document));
        }

        return questions;
    }

    private String extractAndPreprocessText(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Preprocessing
            text = text.replaceAll("\\r\\n", " "); // Replace line breaks with spaces
            text = text.replaceAll("\\s+", " "); // Replace multiple spaces with a single space
            text = text.replaceAll("Page \\d+", ""); // Remove page numbers

            return text;
        }
    }

    private List<String> identifyKeySentences(String content, int numberOfSentences) {
        // Basic sentence splitting
        String[] sentences = content.split("[.!?]\\s+");

        // Filter sentences
        List<String> goodSentences = new ArrayList<>();
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (isGoodSentence(sentence)) {
                goodSentences.add(sentence);
            }
        }

        // Find sentences that contain keywords that indicate important information
        List<String> keySentences = new ArrayList<>();
        String[] keywords = {
                "définition", "concept", "important", "signifie", "est", "représente",
                "exemple", "caractéristique", "propriété", "méthode", "fonction", "formule",
                "théorème", "principe", "règle", "critère", "processus", "système"
        };

        for (String sentence : goodSentences) {
            for (String keyword : keywords) {
                if (sentence.toLowerCase().contains(keyword)) {
                    keySentences.add(sentence);
                    break;
                }
            }

            if (keySentences.size() >= numberOfSentences) {
                break;
            }
        }

        // If we don't have enough key sentences, add some good sentences
        if (keySentences.size() < numberOfSentences) {
            // Shuffle good sentences to get random ones
            Collections.shuffle(goodSentences);
            for (String sentence : goodSentences) {
                if (!keySentences.contains(sentence)) {
                    keySentences.add(sentence);
                }

                if (keySentences.size() >= numberOfSentences) {
                    break;
                }
            }
        }

        return keySentences;
    }

    private boolean isGoodSentence(String sentence) {
        // Length check - not too short, not too long
        if (sentence.length() < 30 || sentence.length() > 200) {
            return false;
        }

        // Should contain mostly alphanumeric characters and common punctuation
        if (!sentence.matches("^[a-zA-Z0-9àáâäæçèéêëìíîïñòóôöùúûüÿÀÁÂÄÆÇÈÉÊËÌÍÎÏÑÒÓÔÖÙÚÛÜŸ,.;:?!()'\"\\s-]+$")) {
            return false;
        }

        return true;
    }

    private Question createSmartQuestion(String sentence, Document document, int questionType) {
        switch (questionType) {
            case 0:
                return createCompletionQuestion(sentence, document);
            case 1:
                return createDefinitionQuestion(sentence, document);
            case 2:
                return createTrueFalseQuestion(sentence, document);
            case 3:
                return createMultipleChoiceQuestion(sentence, document);
            default:
                return createCompletionQuestion(sentence, document);
        }
    }

    private Question createCompletionQuestion(String sentence, Document document) {
        // Extract key terms (nouns, technical terms)
        List<String> keyTerms = extractKeyTerms(sentence);
        if (keyTerms.isEmpty()) return null;

        // Choose a random key term to remove
        String keyTerm = keyTerms.get(random.nextInt(keyTerms.size()));

        // Create question text by replacing the key term with a blank
        String questionText = "Complétez la phrase suivante : \"" + sentence.replace(keyTerm, "_______") + "\"";

        // Create options
        List<String> options = new ArrayList<>();
        options.add(keyTerm); // Correct answer

        // Generate distractors
        List<String> distractors = generateSmartDistractors(keyTerm, keyTerms, document);
        options.addAll(distractors.subList(0, Math.min(3, distractors.size())));

        // Shuffle options
        Collections.shuffle(options);
        int correctIndex = options.indexOf(keyTerm);

        // Create the question
        Question question = new Question();
        question.setSubject(document.getSubject());
        question.setDocument(document);
        question.setQuestionText(questionText);
        question.setOptions(options);
        question.setCorrectOptionIndex(correctIndex);
        question.setUserCreated(false);
        question.setDifficultyLevel(2); // Medium difficulty

        return question;
    }

    private Question createDefinitionQuestion(String sentence, Document document) {
        // Look for definition patterns
        Pattern definitionPattern = Pattern.compile("(?i)(\\w+)[\\s\\w]* est (?:défini|appelé|considéré)[\\s\\w]* comme[\\s\\w]*");
        Matcher matcher = definitionPattern.matcher(sentence);

        if (matcher.find()) {
            String term = matcher.group(1);
            String questionText = "Quelle est la définition correcte de \"" + term + "\" ?";

            List<String> options = new ArrayList<>();
            options.add(sentence); // Correct definition

            // Create false definitions
            for (int i = 0; i < 3; i++) {
                options.add(createFalseDefinition(term));
            }

            Collections.shuffle(options);
            int correctIndex = options.indexOf(sentence);

            Question question = new Question();
            question.setSubject(document.getSubject());
            question.setDocument(document);
            question.setQuestionText(questionText);
            question.setOptions(options);
            question.setCorrectOptionIndex(correctIndex);
            question.setUserCreated(false);
            question.setDifficultyLevel(3); // Higher difficulty

            return question;
        }

        return null;
    }

    private Question createTrueFalseQuestion(String sentence, Document document) {
        String questionText = "Indiquez si l'affirmation suivante est vraie ou fausse : \"" + sentence + "\"";

        List<String> options = new ArrayList<>();
        options.add("Vrai");
        options.add("Faux");

        Question question = new Question();
        question.setSubject(document.getSubject());
        question.setDocument(document);
        question.setQuestionText(questionText);
        question.setOptions(options);
        question.setCorrectOptionIndex(0); // "Vrai" is correct
        question.setUserCreated(false);
        question.setDifficultyLevel(1); // Easy difficulty

        return question;
    }

    private Question createMultipleChoiceQuestion(String sentence, Document document) {
        // Extract a concept from the sentence
        List<String> concepts = extractKeyTerms(sentence);
        if (concepts.isEmpty()) return null;

        String concept = concepts.get(0);
        String questionText = "Selon le document, quelle affirmation est correcte concernant \"" + concept + "\" ?";

        List<String> options = new ArrayList<>();
        options.add(sentence); // Correct statement

        // Create false statements
        for (int i = 0; i < 3; i++) {
            options.add(createFalseStatement(sentence));
        }

        Collections.shuffle(options);
        int correctIndex = options.indexOf(sentence);

        Question question = new Question();
        question.setSubject(document.getSubject());
        question.setDocument(document);
        question.setQuestionText(questionText);
        question.setOptions(options);
        question.setCorrectOptionIndex(correctIndex);
        question.setUserCreated(false);
        question.setDifficultyLevel(3); // Higher difficulty

        return question;
    }

    private List<String> extractKeyTerms(String sentence) {
        List<String> keyTerms = new ArrayList<>();

        // Look for capitalized terms (usually important in educational context)
        Pattern capitalizedPattern = Pattern.compile("\\b[A-Z][a-z]{3,}\\b");
        Matcher capitalizedMatcher = capitalizedPattern.matcher(sentence);
        while (capitalizedMatcher.find()) {
            keyTerms.add(capitalizedMatcher.group());
        }

        // Look for technical terms (longer words)
        Pattern technicalPattern = Pattern.compile("\\b[a-z]{7,}\\b");
        Matcher technicalMatcher = technicalPattern.matcher(sentence);
        while (technicalMatcher.find()) {
            keyTerms.add(technicalMatcher.group());
        }

        // If no key terms found, extract nouns
        if (keyTerms.isEmpty()) {
            // Simple noun extraction (words after "le", "la", "les", "un", "une", "des")
            Pattern nounPattern = Pattern.compile("\\b(le|la|les|un|une|des)\\s+([a-zéèêëàâäôöùûüïîç]{3,})\\b");
            Matcher nounMatcher = nounPattern.matcher(sentence.toLowerCase());
            while (nounMatcher.find()) {
                keyTerms.add(nounMatcher.group(2));
            }
        }

        return keyTerms;
    }

    private List<String> generateSmartDistractors(String correctAnswer, List<String> keyTerms, Document document) {
        List<String> distractors = new ArrayList<>();

        // Use other key terms as distractors
        for (String term : keyTerms) {
            if (!term.equals(correctAnswer) && !distractors.contains(term)) {
                distractors.add(term);
            }
        }

        // Create modified versions of the correct answer
        String[] modifications = {
                correctAnswer + "s",
                correctAnswer.substring(0, Math.max(1, correctAnswer.length() - 2)),
                correctAnswer.replaceFirst("e", "a"),
                "le " + correctAnswer,
                correctAnswer.replaceFirst("é", "è")
        };

        for (String modification : modifications) {
            if (!modification.equals(correctAnswer) && !distractors.contains(modification)) {
                distractors.add(modification);
            }
        }

        Collections.shuffle(distractors);
        return distractors;
    }

    private String createFalseDefinition(String term) {
        String[] templates = {
                term + " est un concept obsolète qui n'est plus utilisé dans les approches modernes.",
                term + " représente l'opposé de ce qui est généralement accepté dans ce domaine.",
                "Contrairement à l'idée reçue, " + term + " ne joue aucun rôle significatif dans ce contexte.",
                term + " est souvent confondu avec d'autres concepts, mais il désigne en réalité un phénomène très différent."
        };

        return templates[random.nextInt(templates.length)];
    }

    private String createFalseStatement(String correctStatement) {
        // Negate or modify the correct statement
        if (correctStatement.contains(" est ")) {
            return correctStatement.replace(" est ", " n'est pas ");
        } else if (correctStatement.contains(" sont ")) {
            return correctStatement.replace(" sont ", " ne sont pas ");
        } else if (correctStatement.contains(" peut ")) {
            return correctStatement.replace(" peut ", " ne peut pas ");
        } else {
            String[] modifiers = {
                    "Contrairement à ce qui est indiqué dans le cours, ",
                    "Il est erroné de penser que ",
                    "Une idée fausse courante est que ",
                    "Certains étudiants pensent à tort que "
            };

            return modifiers[random.nextInt(modifiers.length)] + correctStatement;
        }
    }

    private Question createBasicQuestion(Document document) {
        String[] templates = {
                "Quel est le concept principal présenté dans ce document?",
                "Quelle est l'idée fondamentale expliquée dans ce document?",
                "Quel principe est le plus important selon ce document?",
                "Quelle méthode est recommandée dans ce document?",
                "Quelle approche est privilégiée selon ce document?"
        };

        String questionText = templates[random.nextInt(templates.length)];

        List<String> options = new ArrayList<>();

        // Add one "correct" option
        options.add("La réponse correcte dépend du contenu spécifique du document.");

        // Add distractors
        options.add("Cette option n'est pas mentionnée dans le document.");
        options.add("Cette option est contraire aux principes présentés dans le document.");
        options.add("Cette option est incorrecte selon le document.");

        Question question = new Question();
        question.setSubject(document.getSubject());
        question.setDocument(document);
        question.setQuestionText(questionText);
        question.setOptions(options);
        question.setCorrectOptionIndex(0);
        question.setUserCreated(false);
        question.setDifficultyLevel(1);

        return question;
    }
}