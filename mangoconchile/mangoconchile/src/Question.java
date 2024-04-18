import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Question {
    private String text;
    private String answer;
    private String topic;

    public Question(String text, String answer, String topic) {
        this.text = text;
        this.answer = answer;
        this.topic = topic;
    }

    public boolean evaluateAnswer(String userAnswer, String correctAnswer) {
        int distance = calculateLevenshteinDistance(userAnswer, correctAnswer);
        int maxLength = Math.max(userAnswer.length(), correctAnswer.length());
        if (maxLength == 0) {
            return false;
        }
        int similarity = 100 - (distance * 100 / maxLength);
    
        return similarity >= 70;
    }
    
    public boolean evaluateAnswerSimilarity(String userAnswer, String correctAnswer) {
        double similarity = ChatGPTClient.calculateStringSimilarity(userAnswer, correctAnswer);
        return similarity >= 0.7;
      }    

    public static int calculateLevenshteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int newValue = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int replacementCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                newValue = Math.min(Math.min(newValue + 1, costs[j] + 1), costs[j - 1] + replacementCost);
                costs[j - 1] = costs[0];
                costs[0] = newValue;
            }
        }
        return costs[b.length()];
    }


    public String getText() {
        return text;
    }

    public String getAnswer() {
        return answer;
    }

    public String getTopic() {
        return topic;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}