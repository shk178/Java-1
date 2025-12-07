package hello.jdbc.transaction2;

import hello.jdbc.Member;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.*;

import static hello.jdbc.ConnectionConst.*;

@Component
public class MemberRepository3 {
    private final DataSource dataSource;
    public MemberRepository3() {
        this.dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    }
    public Member saveFind(Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values (?, ?)";
        String sql2 = "select * from member where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            pstmt = con.prepareStatement(sql2);
            pstmt.setString(1, member.getMemberId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member1 = new Member();
                member1.setMemberId(rs.getString("member_id"));
                member1.setMoney(rs.getInt("money"));
                System.out.println();
                System.out.println("member1.getMemberId() = " + member1.getMemberId());
                System.out.println("member1.getMoney() = " + member1.getMoney());
                System.out.println();
                return member;
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
        return null;
    }
    private Connection getConnection() throws SQLException {
        Connection con = DataSourceUtils.getConnection(dataSource);
        return con;
    }
    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        DataSourceUtils.releaseConnection(con, dataSource);
    }
}
