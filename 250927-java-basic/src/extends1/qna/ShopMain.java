package extends1.qna;

public class ShopMain {
    public static void main(String[] args) {
        Book book = new Book("책1", 1000, "작가1", 1111);
        Album album = new Album("앨범1", 2000, "아티스트1");
        book.print();
        album.print();
    }
}
