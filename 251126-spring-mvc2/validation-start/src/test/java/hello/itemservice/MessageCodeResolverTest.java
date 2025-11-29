package hello.itemservice;

import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import java.util.Arrays;

public class MessageCodeResolverTest {
    static void object() {
        MessageCodesResolver mcr = new DefaultMessageCodesResolver();
        String[] messageCodes = mcr.resolveMessageCodes("required", "item");
        System.out.println(Arrays.toString(messageCodes)); // [required.item, required]
    }
    static void field() {
        MessageCodesResolver mcr = new DefaultMessageCodesResolver();
        String[] messageCodes = mcr.resolveMessageCodes("required", "item", "itemName", String.class);
        System.out.println(Arrays.toString(messageCodes)); // [required.item.itemName, required.itemName, required.java.lang.String, required]
    }
    public static void main(String[] args) {
        object();
        System.out.println();
        field();
    }
}
