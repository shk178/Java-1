package hello.jdbc.transaction;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.Member;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RunMain {
    public static void main(String[] args) throws SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:tcp://localhost/~/test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("pool1");
        Connection con = dataSource.getConnection();
        Member member = new Member("id-1", 1000);
        System.out.println("member = " + member); // member = Member(memberId=id-1, money=1000)
        con.setAutoCommit(true);
        String sql = "delete from member where member_id = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, member.getMemberId());
        pstmt.executeUpdate();
        con.setAutoCommit(false);
        String sql1 = "insert into member (member_id, money) values (?, ?)";
        PreparedStatement pstmt1 = con.prepareStatement(sql1);
        pstmt1.setString(1, member.getMemberId());
        pstmt1.setInt(2, member.getMoney());
        pstmt1.executeUpdate();
        String sql2 = "select * from member where member_id = ?";
        PreparedStatement pstmt2 = con.prepareStatement(sql2);
        pstmt2.setString(1, member.getMemberId());
        ResultSet rs = pstmt2.executeQuery();
        if (rs.next()) {
            Member resultMember = new Member();
            resultMember.setMemberId(rs.getString("member_id"));
            resultMember.setMoney(rs.getInt("money"));
            System.out.println("resultMember = " + resultMember); // resultMember = Member(memberId=id-1, money=1000)
        }
        con.commit();
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(pstmt);
        JdbcUtils.closeStatement(pstmt1);
        JdbcUtils.closeStatement(pstmt2);
        JdbcUtils.closeConnection(con);
    }
}
