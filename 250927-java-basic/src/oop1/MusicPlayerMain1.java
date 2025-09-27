package oop1;

public class MusicPlayerMain1 {
    public static void main(String[] args) {
        MusicPlayerData data = new MusicPlayerData();
        dataOn(data);
        dataUp(data);
        dataUp(data);
        dataDown(data);
        dataOff(data);
    }
    static void dataOn(MusicPlayerData data) {
        data.isOn = true;
    }
    static void dataOff(MusicPlayerData data) {
        data.isOn = false;
    }
    static void dataUp(MusicPlayerData data) {
        data.volume++;
    }
    static void dataDown(MusicPlayerData data) {
        data.volume--;
    }
}
