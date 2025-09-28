package extends1.qna;

public class Album extends Item {
    public String artist;
    Album(String name, int price, String artist) {
        super(name, price);
        this.artist = artist;
    }
    @Override
    public void print(){
        super.print();
        System.out.println("artist = " + artist);
    }
}
