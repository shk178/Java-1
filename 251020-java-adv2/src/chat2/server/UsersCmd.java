package chat2.server;

import java.io.IOException;
import java.util.List;

public class UsersCmd implements Command {
    private final SessionManager sessionManager;
    public UsersCmd(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public void execute(String[] args, Session session) throws IOException {
        List<String> usernames = sessionManager.getAllUsername();
        session.send(usernames.toString());
    }
}
