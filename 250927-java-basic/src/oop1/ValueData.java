package oop1;

public class ValueData {
    int value = 0;
    void add() {
        value++;
    }
    void add(int diff) {
        value += diff;
    }
    void print() {
        System.out.println("value = " + value);
    }
}
