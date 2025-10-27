package annotation;

public class TestController {
    @SimpleMapping(value = "/")
    public void home() {
        System.out.println("TestController.home 호출");
    }
    @SimpleMapping(value = "/site1")
    public void page1() {
        System.out.println("TestController.page1 호출");
    }
    @SimpleMapping(value = "/site2")
    public void page2() {
        System.out.println("TestController.page2 호출");
    }
}
