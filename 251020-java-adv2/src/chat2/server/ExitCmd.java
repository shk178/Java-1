package chat2.server;

import java.io.IOException;

public class ExitCmd implements Command {
    private final SessionManager sessionManager;
    public ExitCmd(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public void execute(String[] args, Session session) throws IOException {
        throw new IOException("exit");
    }
}
