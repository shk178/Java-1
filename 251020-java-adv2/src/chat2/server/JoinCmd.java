package chat2.server;

import java.io.IOException;

public class JoinCmd implements Command {
    private final SessionManager sessionManager;
    public JoinCmd(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public void execute(String[] args, Session session) throws IOException {
        String username = args[1];
        session.setUsername(username);
        sessionManager.sendAll(username + "님이 입장함");
    }
}
