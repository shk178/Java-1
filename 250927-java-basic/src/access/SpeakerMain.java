package access;

public class SpeakerMain {
    public static void main(String[] args) {
        Speaker speaker = new Speaker(90);
        speaker.volUp();
        speaker.volShow();
        speaker.volUp();
        speaker.volShow();
        speaker.volume = 200;
        speaker.volShow();
    }
}
