import java.time.LocalDate;

public class Review {
    //Attributes
    private String reviewID;
    private String reviewerID;
    private String reviewTitle;
    private String reviewBody;
    private float score;
    private LocalDate reviewDate;


    //Methods
    //Getters
    public String getReviewID() {
        return reviewID;
    }
    public String getReviewerID() {
        return reviewerID;
    }
    public String getReviewTitle() {
        return reviewTitle;
    }
    public String getReviewBody() {
        return reviewBody;
    }
    public float getScore() {
        return score;
    }
    public LocalDate getReviewDate() {
        return reviewDate;
    }
    //Setters
    public void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }
    public void setReviewerID(String reviewerID) {
        this.reviewerID = reviewerID;
    }
    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }
    public void setReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
    }
    public void setScore(float score) {
        this.score = score;
    }
    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }
}