package chat2.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CmdManager1 implements CommandManager {
    private static final String DELIMITER = "\\|"; // 정규 표현식이 아님 - \\ 추가
    private final SessionManager sessionManager;
    private final Map<String, Command> commands = new HashMap<>();
    public CmdManager1(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        commands.put("/join", new JoinCmd(sessionManager));
        commands.put("/message", new MsgCmd(sessionManager));
        commands.put("/change", new ChangeCmd(sessionManager));
        commands.put("/users", new UsersCmd(sessionManager));
        commands.put("/exit", new ExitCmd(sessionManager));
    }
    @Override
    public void execute(String totalMessage, Session session) throws IOException {
        String[] args = totalMessage.split(DELIMITER);
        String key = args[0];
        Command cmd = commands.getOrDefault(key, new DefaultCmd(sessionManager)); // null을 객체처럼 다룬다.
        cmd.execute(args, session);
    }
}
