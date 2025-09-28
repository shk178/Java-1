package access;

public class Speaker2 {
    private int volume;
    Speaker2(int volume) {
        this.volume = volume;
    }
    void volUp() {
        if (volume >= 100) {
            System.out.println("최대 음량");
        } else {
            volume += 10;
        }
    }
    void volDown() {
        if (volume <= 0) {
            System.out.println("최소 음량");
        } else {
            volume -= 10;
        }
    }
    void volShow() {
        System.out.println("volume = " + volume);
    }
}
