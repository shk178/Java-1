package hello.jdbc;

import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

@Repository
public class MemberRepository2 {
    public Member save(Connection con, Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values (?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //JdbcUtils.closeConnection(con);
    }
    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";
        //Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            //con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException(memberId);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }
    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";
        //Connection con = null;
        PreparedStatement pstmt = null;
        try {
            //con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            System.out.println(resultSize);
        } catch (SQLException e) {
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
    public void delete(Connection con, String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";
        //Connection con = null;
        PreparedStatement pstmt = null;
        try {
            //con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
}
