package hello.jdbc;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.ConnectionConst.*;

public class DataSourceConnect {
    static void one() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        System.out.println("con1 = " + con1);
        System.out.println("con1.getClass() = " + con1.getClass());
        System.out.println("con2 = " + con2);
        System.out.println("con2.getClass() = " + con2.getClass());
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        Connection con3 = dataSource.getConnection();
        Connection con4 = dataSource.getConnection();
        System.out.println("con3 = " + con3);
        System.out.println("con3.getClass() = " + con3.getClass());
        System.out.println("con4 = " + con4);
        System.out.println("con4.getClass() = " + con4.getClass());
    }
    public static void main(String[] args) throws SQLException {
        one();
        /*
        con1 = conn0: url=jdbc:h2:tcp://localhost/~/test user=SA
        con1.getClass() = class org.h2.jdbc.JdbcConnection
        con2 = conn1: url=jdbc:h2:tcp://localhost/~/test user=SA
        con2.getClass() = class org.h2.jdbc.JdbcConnection
        con3 = conn2: url=jdbc:h2:tcp://localhost/~/test user=SA
        con3.getClass() = class org.h2.jdbc.JdbcConnection
        con4 = conn3: url=jdbc:h2:tcp://localhost/~/test user=SA
        con4.getClass() = class org.h2.jdbc.JdbcConnection
         */
        two();
        /*
        15:48:05.768 [main] INFO com.zaxxer.hikari.HikariDataSource -- two - Starting...
        15:48:05.794 [main] INFO com.zaxxer.hikari.pool.HikariPool -- two - Added connection conn4: url=jdbc:h2:tcp://localhost/~/test user=SA
        15:48:05.794 [main] INFO com.zaxxer.hikari.HikariDataSource -- two - Start completed.
        con5 = HikariProxyConnection@1897115967 wrapping conn4: url=jdbc:h2:tcp://localhost/~/test user=SA
        con5.getClass() = class com.zaxxer.hikari.pool.HikariProxyConnection
        con6 = HikariProxyConnection@1188753216 wrapping conn5: url=jdbc:h2:tcp://localhost/~/test user=SA
        con6.getClass() = class com.zaxxer.hikari.pool.HikariProxyConnection
         */
    }
    static void two() throws SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("two");
        Connection con5 = dataSource.getConnection();
        Connection con6 = dataSource.getConnection();
        System.out.println("con5 = " + con5);
        System.out.println("con5.getClass() = " + con5.getClass());
        System.out.println("con6 = " + con6);
        System.out.println("con6.getClass() = " + con6.getClass());
    }
}
