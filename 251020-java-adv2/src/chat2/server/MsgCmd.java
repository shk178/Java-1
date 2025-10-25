package chat2.server;

import java.io.IOException;

public class MsgCmd implements Command {
    private final SessionManager sessionManager;
    public MsgCmd(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public void execute(String[] args, Session session) throws IOException {
        String message = args[1];
        sessionManager.sendAll("[" + session.getUsername() + "] " + message);
    }
}
