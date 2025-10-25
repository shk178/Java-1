package chat.server;

import java.io.IOException;

public class ServerMain {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        SessionManager sessionManager = new SessionManager();
        CommandManager commandManager = new CmdManager1(sessionManager);
        Server server = new Server(PORT, commandManager, sessionManager);
        server.start();
    }
}
/*
19:52:01.981 [     main] 서버 시작
19:52:11.283 [ Thread-1] 서버 세션 실행
java.io.IOException: exit
	at chat.server.CmdManager1.execute(CmdManager1.java:13)
	at chat.server.Session.run(Session.java:43)
	at java.base/java.lang.Thread.run(Thread.java:1583)
19:52:25.248 [ Thread-1] 서버 세션 종료
19:52:44.870 [ Thread-0] shutdownHook 실행
19:52:44.871 [     main] 서버 종료
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endAccept(NioSocketImpl.java:682)
	at java.base/sun.nio.ch.NioSocketImpl.accept(NioSocketImpl.java:755)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:698)
	at java.base/java.net.ServerSocket.platformImplAccept(ServerSocket.java:663)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:639)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:585)
	at java.base/java.net.ServerSocket.accept(ServerSocket.java:543)
	at chat.server.Server.running(Server.java:28)
	at chat.server.Server.start(Server.java:23)
	at chat.server.ServerMain.main(ServerMain.java:11)
 */
/*
20:05:42.095 [     main] 서버 시작
20:05:45.394 [ Thread-1] 서버 세션 실행
java.io.IOException: exit
	at chat.server.CmdManager1.execute(CmdManager1.java:24)
	at chat.server.Session.run(Session.java:43)
	at java.base/java.lang.Thread.run(Thread.java:1583)
20:05:59.913 [ Thread-1] 서버 세션 종료
20:06:16.781 [ Thread-0] shutdownHook 실행
20:06:16.781 [     main] 서버 종료
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endAccept(NioSocketImpl.java:682)
	at java.base/sun.nio.ch.NioSocketImpl.accept(NioSocketImpl.java:755)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:698)
	at java.base/java.net.ServerSocket.platformImplAccept(ServerSocket.java:663)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:639)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:585)
	at java.base/java.net.ServerSocket.accept(ServerSocket.java:543)
	at chat.server.Server.running(Server.java:28)
	at chat.server.Server.start(Server.java:23)
	at chat.server.ServerMain.main(ServerMain.java:11)
 */
/*
20:07:28.062 [     main] 서버 시작
20:07:31.212 [ Thread-1] 서버 세션 실행
20:07:34.196 [ Thread-2] 서버 세션 실행
java.io.IOException: exit
	at chat.server.CmdManager1.execute(CmdManager1.java:24)
	at chat.server.Session.run(Session.java:43)
	at java.base/java.lang.Thread.run(Thread.java:1583)
20:08:15.571 [ Thread-1] 서버 세션 종료
java.io.IOException: exit
	at chat.server.CmdManager1.execute(CmdManager1.java:24)
	at chat.server.Session.run(Session.java:43)
	at java.base/java.lang.Thread.run(Thread.java:1583)
20:08:22.525 [ Thread-2] 서버 세션 종료
20:08:29.607 [ Thread-0] shutdownHook 실행
20:08:29.607 [     main] 서버 종료
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endAccept(NioSocketImpl.java:682)
	at java.base/sun.nio.ch.NioSocketImpl.accept(NioSocketImpl.java:755)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:698)
	at java.base/java.net.ServerSocket.platformImplAccept(ServerSocket.java:663)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:639)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:585)
	at java.base/java.net.ServerSocket.accept(ServerSocket.java:543)
	at chat.server.Server.running(Server.java:28)
	at chat.server.Server.start(Server.java:23)
	at chat.server.ServerMain.main(ServerMain.java:11)
 */