package chat2.server;

import java.io.IOException;

public interface Command {
    void execute(String[] args, Session session) throws IOException;
}
