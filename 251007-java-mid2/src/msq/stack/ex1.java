package msq.stack;
import java.util.*;

public class ex1 {
    public static void main(String[] args) {
        NetHistory browser = new NetHistory();
        browser.visitPage("youtube.com");
        browser.visitPage("google.com");
        browser.visitPage("facebook.com");
        browser.goBack();
        browser.goBack();
        browser.goBack();
        browser.visitPage("github.com");
        browser.goBack();
    }
}
/*
visitPage to: youtube.com
visitPage to: google.com
visitPage to: facebook.com
goBack to: google.com
goBack to: youtube.com
goBack to: null
visitPage to: github.com
goBack to: null
 */