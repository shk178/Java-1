package chat2.server;

import java.io.IOException;

public class DefaultCmd implements Command {
    private final SessionManager sessionManager;
    public DefaultCmd(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public void execute(String[] args, Session session) throws IOException {
        session.send("잘못된 명령어");
    }
}
