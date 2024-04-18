public class QuestionResult {
    private Question question;
    private String userAnswer;
    private boolean isCorrect;
  
    public QuestionResult(Question question, String userAnswer, boolean isCorrect) {
      this.question = question;
      this.userAnswer = userAnswer;
      this.isCorrect = isCorrect;
    }
  
    public Question getQuestion() {
      return question;
    }
  
    public String getUserAnswer() {
      return userAnswer;
    }
  
    public String getCorrectAnswer() {
      return question.getAnswer();
    }
  
    public boolean isCorrect() {
      return isCorrect;
    }
  }
  