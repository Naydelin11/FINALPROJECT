import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SmartTutor {

    private Exam exam;
    private ChatGPTClient chatGPTClient;
    private int passingScore = 70;
    private Scanner scanner;

    public SmartTutor() {
        chatGPTClient = new ChatGPTClient();
        scanner = new Scanner(System.in);
        exam = generateInitialExam();
    }
    

    private Exam generateInitialExam() {
        List<POOTopic> topics = new ArrayList<>();
        topics.add(new POOTopic("Fundamentos de las POO"));
        topics.add(new POOTopic("Herencia en las POO"));
        topics.add(new POOTopic("Abstracción en las POO"));
        topics.add(new POOTopic("Genéricos en las POO"));
    
        for (POOTopic topic : topics) {
            try {
                CompletableFuture<String> questionFuture = ChatGPTClient.getChatGPTResponseAsync("Genera una pregunta sobre " + topic.getName());
                String questionText = questionFuture.get();
                CompletableFuture<String> answerFuture = ChatGPTClient.getChatGPTResponseAsync("Cuál es la respuesta para la pregunta: \"" + questionText + "\"?");
                String questionAnswer = answerFuture.get();
                topic.addQuestion(new Question(questionText, questionAnswer, topic.getName()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    
        return new Exam(topics);
    }
    

    public void start() throws InterruptedException, ExecutionException {
        do {
            System.out.println("Examen de Introducción a la programación orientada a objetos");
            List<String> userAnswers = getUserAnswers();
            List<QuestionResult> results = exam.evaluateAnswers(userAnswers);
            int score = 0;
    
            for (QuestionResult result : results) {
                if (result.isCorrect()) {
                    score++;
                }
            }
    
            System.out.println("Su puntuación es: " + score);
    
            if (score >= passingScore) {
                System.out.println("Examen aprobado!");
                break;
            } else {
                List<String> notPassedTopics = exam.getTopicsNotPassed(userAnswers, passingScore);
                if (!notPassedTopics.isEmpty()) {
                    String feedback = generateFeedback(notPassedTopics);
                    System.out.println(feedback);
                    exam = generateNewExam(notPassedTopics);
                } else {
                    System.out.println("No hay temas para generar feedback.");
                }
                System.out.println("¿Estás listo para intentar el examen nuevamente? (s/n)");
                String ready = scanner.nextLine();
                if (!"s".equalsIgnoreCase(ready)) {
                    System.out.println("El tutor está disponible cuando estés listo para continuar.");
                    break;
                }
            }
        } while (true);
    }
    
    

    private List<String> getUserAnswers() {
        return exam.getQuestions().stream()
            .map(question -> {
                System.out.println(question.getText());
                return scanner.nextLine();
            })
            .collect(Collectors.toList());  
    }

    private String generateFeedback(List<String> notPassedTopics) {
        StringBuilder feedback = new StringBuilder();
        for (String topic : notPassedTopics) {
            feedback.append("Tema: ").append(topic).append("\n");
            feedback.append("Recomendaciones:\n");
            try {
                CompletableFuture<String> recommendationsFuture = ChatGPTClient.getChatGPTResponseAsync("Dame recomendaciones de estudio para el tema: " + topic);
                String recommendations = recommendationsFuture.get();
                feedback.append(recommendations).append("\n\n");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return feedback.toString();
    }
    
    private Exam generateNewExam(List<String> notPassedTopics) {
        List<POOTopic> topics = new ArrayList<>();
        for (String topicName : notPassedTopics) {
            try {
                POOTopic topic = new POOTopic(topicName);
                CompletableFuture<String> questionFuture = ChatGPTClient.getChatGPTResponseAsync("Genera una pregunta sobre " + topicName);
                String questionText = questionFuture.get();
                CompletableFuture<String> answerFuture = ChatGPTClient.getChatGPTResponseAsync("Cuál es la respuesta para la pregunta: \"" + questionText + "\"?");
                String questionAnswer = answerFuture.get();
                topic.addQuestion(new Question(questionText, questionAnswer, topicName));
                topics.add(topic);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return new Exam(topics);
    }
    


}