package hello.jdbc.transaction2;

import hello.jdbc.Member;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MemberRepository3 {
    private final DataSource dataSource;
    public MemberRepository3(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public Member dataLogic(Member member) throws SQLException {
        Connection con = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        ResultSet rs3 = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            String sql1 = "delete from member where member_id = ?";
            pstmt1 = con.prepareStatement(sql1);
            pstmt1.setString(1, member.getMemberId());
            pstmt1.executeUpdate();
            String sql2 = "insert into member (member_id, money) values (?, ?)";
            pstmt2 = con.prepareStatement(sql2);
            pstmt2.setString(1, member.getMemberId());
            pstmt2.setInt(2, member.getMoney());
            pstmt2.executeUpdate();
            String sql3 = "select * from member where member_id = ?";
            pstmt3 = con.prepareStatement(sql3);
            pstmt3.setString(1, member.getMemberId());
            rs3 = pstmt3.executeQuery();
            Member resultMember = null;
            if (rs3.next()) {
                resultMember = new Member();
                resultMember.setMemberId(rs3.getString("member_id"));
                resultMember.setMoney(rs3.getInt("money"));
            }
            return resultMember;
        } catch (SQLException e) {
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs3);
            JdbcUtils.closeStatement(pstmt3);
            JdbcUtils.closeStatement(pstmt2);
            JdbcUtils.closeStatement(pstmt1);
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
