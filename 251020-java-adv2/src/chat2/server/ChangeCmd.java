package chat2.server;

import java.io.IOException;

public class ChangeCmd implements Command {
    private final SessionManager sessionManager;
    public ChangeCmd(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public void execute(String[] args, Session session) throws IOException {
        String username2 = args[1];
        sessionManager.sendAll(session.getUsername() + "님이 이름 변경함: " + username2);
        session.setUsername(username2);
    }
}
