package class1.ex;

public class Ex1 {
    public static void main(String[] args) {
        MovieReview inception = new MovieReview();
        MovieReview aboutTime = new MovieReview();
        inception.title = "인셉션";
        inception.review = "인생은 무한 루프";
        aboutTime.title = "어바웃 타임";
        aboutTime.review = "인생 시간 영화";
        MovieReview[] mrs = new MovieReview[]{inception, aboutTime};
        for (MovieReview i : mrs) {
            System.out.println("title = " + i.title);
            System.out.println("review = " + i.review);
        }
    }
}
