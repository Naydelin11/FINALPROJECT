import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class POOTopic {
    private String name;
    private List<Question> questions;

    public POOTopic(String name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public boolean isPassed(List<String> userAnswers, int passingScore) throws InterruptedException, ExecutionException {
        int topicScore = 0;
        for (Question question : questions) {
            int questionIndex = questions.indexOf(question);
            if (questionIndex != -1 && question.evaluateAnswer(userAnswers.get(questionIndex), question.getAnswer())) {
                topicScore++;
            }
        }
        double percentageScore = (double) topicScore / questions.size() * 100;
        return percentageScore >= passingScore;
    }
    

    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}