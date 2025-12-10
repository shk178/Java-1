package jdbc.except;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SpringTranslator {
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
    @Test
    void duplicatedKey2() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        Member member = new Member("memId", 1_000);
        String sql = "insert into member (member_id, money) values (?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            pstmt.executeUpdate();
        } catch (SQLException e) {
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException dae = set.translate("msg", sql, e);
            System.out.println("throw dae");
            System.out.println(dae);
            System.out.println();
            System.out.println(e);
        } finally {
            JdbcUtils.closeStatement(pstmt);
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
// throw dae
// org.springframework.dao.DuplicateKeyException: msg; SQL [insert into member (member_id, money) values (?, ?)]; Unique index or primary key violation: "PUBLIC.CONSTRAINT_8 INDEX PUBLIC.PRIMARY_KEY_8 ON PUBLIC.MEMBER(MEMBER_ID) VALUES ( /* 36 */ 'memId' )"; SQL statement:
// insert into member (member_id, money) values (?, ?) [23505-232]

// org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: "PUBLIC.CONSTRAINT_8 INDEX PUBLIC.PRIMARY_KEY_8 ON PUBLIC.MEMBER(MEMBER_ID) VALUES ( /* 36 */ 'memId' )"; SQL statement:
// insert into member (member_id, money) values (?, ?) [23505-240]