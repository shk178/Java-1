package aop.demo.model;

import aop.demo.aspect.Loggable;

public class Triangle {
    private String name;
    public String getName() {
        return name;
    }
    @Loggable
    public void printName() {
        System.out.println("printName: " + name);
    }
    public void setName(String name) {
        this.name = name;
    }
    public String setNameandReturn(String name2) {
        this.name = name2;
        return name;
    }
}
