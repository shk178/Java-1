package extends1.qna;

public class Book extends Item {
    public String author;
    public int isbn;
    Book(String name, int price, String author, int isbn) {
        super(name, price);
        this.author = author;
        this.isbn = isbn;
    }
    @Override
    public void print() {
        super.print();
        System.out.println("author = " + author);
        System.out.println("isbn = " + isbn);
    }
}
