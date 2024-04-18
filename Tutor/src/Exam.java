import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Exam {
    private List<Question> questions;
    private List<POOTopic> topics;

    public Exam(List<POOTopic> topics) {
        this.topics = topics;
        this.questions = new ArrayList<>();
        generateQuestions();
    }

    public void generateQuestions() {
        for (POOTopic topic : topics) {
            CompletableFuture<String> questionFuture = ChatGPTClient.getChatGPTResponseAsync("Genera una pregunta sobre " + topic.getName());
            CompletableFuture<String> answerFuture = questionFuture.thenCompose(questionText ->
                ChatGPTClient.getChatGPTResponseAsync("Cuál es la respuesta para la pregunta: \"" + questionText + "\"?")
            );
            try {
                String questionText = questionFuture.get();
                String questionAnswer = answerFuture.get();
                questions.add(new Question(questionText, questionAnswer, topic.getName()));
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Error al generar pregunta para el tema: " + topic.getName());
                continue;
            }
        }
    }

    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public List<QuestionResult> evaluateAnswers(List<String> userAnswers) throws InterruptedException, ExecutionException {
        List<QuestionResult> questionResults = new ArrayList<>();
        int score = 0;
      
        for (int i = 0; i < questions.size(); i++) {
          Question question = questions.get(i);
          String userAnswer = userAnswers.get(i);
          boolean isCorrect = question.evaluateAnswerSimilarity(userAnswer, question.getAnswer());
      
          if (isCorrect) {
            score++;
          }
      
          questionResults.add(new QuestionResult(question, userAnswer, isCorrect));
        }
      
        return questionResults;
      }
      

      public List<String> getTopicsNotPassed(List<String> userAnswers, int passingScore) throws InterruptedException, ExecutionException {
        List<QuestionResult> questionResults = evaluateAnswers(userAnswers);
        List<String> notPassedTopics = new ArrayList<>();
      
        for (POOTopic topic : topics) {
          int topicScore = 0;
      
          for (QuestionResult questionResult : questionResults) {
            if (questionResult.getQuestion().getTopic().equals(topic.getName())) {
              topicScore += questionResult.isCorrect() ? 1 : 0;
            }
          }
      
          double percentageScore = (double) topicScore / topic.getQuestions().size() * 100;
      
          if (percentageScore < passingScore) {
            notPassedTopics.add(topic.getName());
          }
        }
      
        return notPassedTopics;
      }

    public List<QuestionResult> getCorrectAnswers() {
        List<QuestionResult> correctAnswers = new ArrayList<>();
      
        for (int i = 0; i < questions.size(); i++) {
          Question question = questions.get(i);
          String correctAnswer = question.getAnswer();
          boolean isCorrect = question.evaluateAnswerSimilarity(correctAnswer, correctAnswer);
          correctAnswers.add(new QuestionResult(question, correctAnswer, isCorrect));
        }
      
        return correctAnswers;
      }
      
}