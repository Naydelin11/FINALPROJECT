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
                
                // Verificar si la respuesta de la API es válida
                if (questionText != null && !questionText.isEmpty()) {
                    CompletableFuture<String> answerFuture = ChatGPTClient.getChatGPTResponseAsync("Cuál es la respuesta para la pregunta: \"" + questionText + "\"?");
                    String questionAnswer = answerFuture.get();
                    
                    // Verificar si la respuesta de la API es válida
                    if (questionAnswer != null && !questionAnswer.isEmpty()) {
                        topic.addQuestion(new Question(questionText, questionAnswer, topic.getName()));
                    } else {
                        System.out.println("La respuesta para la pregunta sobre el tema " + topic.getName() + " es inválida. Saltando este tema.");
                    }
                } else {
                    System.out.println("No se pudo generar una pregunta para el tema " + topic.getName() + ". Saltando este tema.");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    
        return new Exam(topics);
    }
    
    
    public void start() throws InterruptedException, ExecutionException {
        do {
            // Realizar el examen y obtener las respuestas del usuario
            List<String> userAnswers = getUserAnswers();
            List<QuestionResult> results = exam.evaluateAnswers(userAnswers);
            int score = 0;
    
            // Calificar el examen y obtener el puntaje
            for (QuestionResult result : results) {
                if (result.isCorrect()) {
                    score++;
                }
            }
    
            // Mostrar el puntaje al usuario
            System.out.println("Su puntaje es: " + score);
    
            // Proporcionar retroalimentación con recomendaciones de estudio para los temas no aprobados
            List<String> notPassedTopics = exam.getTopicsNotPassed(userAnswers, passingScore);
            String feedback = generateFeedback(notPassedTopics);
            System.out.println(feedback);
    
            // Permitir que el usuario haga preguntas adicionales a ChatGPT
            System.out.println("¿Tienes alguna pregunta adicional sobre los temas del examen? (s/n)");
            String askQuestion = scanner.nextLine();
            if ("s".equalsIgnoreCase(askQuestion)) {
                askAdditionalQuestions();
            }
    
            // Generar un nuevo examen solo con los temas no aprobados
            exam = generateNewExam(notPassedTopics);
    
            // Preguntar al usuario si desea intentar el examen nuevamente
            System.out.println("¿Estás listo para intentar el examen nuevamente? (s/n)");
            String ready = scanner.nextLine();
            if (!"s".equalsIgnoreCase(ready)) {
                System.out.println("El tutor está disponible cuando estés listo para continuar.");
                break;
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
    
            // Aquí puedes agregar enlaces de repaso relacionados con el tema
            switch (topic) {
                case "Fundamentos de las POO":
                    feedback.append("Repasa los conceptos básicos de la programación orientada a objetos en el siguiente enlace: [Fundamentos de POO](https://genius.com/Kali-uchis-pensamientos-intrusivos-lyrics)\n");
                    break;
                case "Herencia en las POO":
                    feedback.append("Refuerza tus conocimientos sobre herencia en las clases de POO aquí: [Herencia en POO](https://genius.com/Genius-traducciones-al-espanol-blackpink-as-if-its-your-last-traduccion-al-espanol-lyrics)\n");
                    break;
                case "Abstracción en las POO":
                    feedback.append("Consulta ejemplos y explicaciones sobre abstracción en POO en este enlace: [Abstracción en POO](https://www.letras.com/kali-uchis/no-hay-ley/)\n");
                    break;
                case "Genéricos en las POO":
                    feedback.append("Aprende más sobre el uso de genéricos en programación orientada a objetos aquí: [Genéricos en POO](https://www.letras.com/blackpink/dont-know-what-to-do/)\n");
                    break;
                default:
                    feedback.append("No se encontraron enlaces de repaso para este tema.\n");
                    break;
            }
    
            feedback.append("\n");
        }
        return feedback.toString();
    }    

    public void askAdditionalQuestions() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("¿Tienes alguna pregunta adicional relacionada con los temas del examen? (s/n)");
            String response = scanner.nextLine();
            if ("s".equalsIgnoreCase(response)) {
                System.out.println("Por favor, introduce tu pregunta:");
                String userQuestion = scanner.nextLine();
                CompletableFuture<String> answerFuture = ChatGPTClient.getChatGPTResponseAsync(userQuestion);
                try {
                    String answer = answerFuture.get();
                    System.out.println("Respuesta: " + answer);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Hubo un error al obtener la respuesta. Inténtalo de nuevo más tarde.");
                }
            } else if ("n".equalsIgnoreCase(response)) {
                System.out.println("Entendido. Si tienes más preguntas, no dudes en preguntar más tarde.");
                break;
            } else {
                System.out.println("Respuesta no válida. Por favor, responde con 's' o 'n'.");
            }
        }
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
