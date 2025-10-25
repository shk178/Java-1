package chat.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CmdManager1 implements CommandManager {
    private static final String DELIMITER = "\\|"; // 정규 표현식이 아님 - \\ 추가
    private final SessionManager sessionManager;
    public CmdManager1(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    @Override
    public void execute(String totalMessage, Session session) throws IOException {
        if (totalMessage.startsWith("/join")) {
            String[] split = totalMessage.split(DELIMITER);
            String username = split[1];
            session.setUsername(username);
            sessionManager.sendAll(username + "님이 입장함");
        } else if (totalMessage.startsWith("/message")) {
            String[] split = totalMessage.split(DELIMITER);
            String message = split[1];
            sessionManager.sendAll("[" + session.getUsername() + "] " + message);
        } else if (totalMessage.startsWith("/change")) {
            String[] split = totalMessage.split(DELIMITER);
            String username2 = split[1];
            sessionManager.sendAll(session.getUsername() + "님이 이름 변경함: " + username2);
            session.setUsername(username2);
        } else if (totalMessage.startsWith("/users")) {
            List<String> usernames = sessionManager.getAllUsername();
            session.send(usernames.toString());
        } else if (totalMessage.startsWith("/exit")) {
            throw new IOException("exit");
        } else {
            session.send("처리할 수 없음");
        }
    }
}
