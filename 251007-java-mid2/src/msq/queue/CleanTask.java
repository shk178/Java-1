package msq.queue;

public class CleanTask implements Task {
    @Override
    public void execute() {
        System.out.println("자원 정리");
    }
}
