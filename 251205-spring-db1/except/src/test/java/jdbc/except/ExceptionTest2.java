package jdbc.except;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

public class ExceptionTest2 {
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
    @Test
    void duplicatedKey() {
        MemberRepository1 memberRepository1 = new MemberRepository1(new DriverManagerDataSource(URL, USERNAME, PASSWORD));
        Member member = new Member("memId", 1_000);
        try {
            memberRepository1.delete(member.getMemberId());
            memberRepository1.save(member);
            memberRepository1.save(member);
        } catch (MyDBException e) {
            String sqlState = ((SQLException) e.getCause()).getSQLState();
            if (sqlState.equals("23505")) {
                System.out.println("throw new MyDBException2(e)");
                System.out.println(e);
            } else {
                System.out.println("throw new MyDBException(e)");
                System.out.println(e);
            }
        }
    }
}
//throw new MyDBException2(e)
//jdbc.except.MyDBException: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: "PUBLIC.CONSTRAINT_8 INDEX PUBLIC.PRIMARY_KEY_8 ON PUBLIC.MEMBER(MEMBER_ID) VALUES ( /* 36 */ 'memId' )"; SQL statement:
/*
insert into member (member_id, money) values (?, ?) [23505-240]
at org.h2.message.DbException.getJdbcSQLException(DbException.java:520)
at org.h2.message.DbException.getJdbcSQLException(DbException.java:489)
at org.h2.message.DbException.get(DbException.java:223)
at org.h2.message.DbException.get(DbException.java:199)
at org.h2.index.Index.getDuplicateKeyException(Index.java:535)
at org.h2.mvstore.db.MVSecondaryIndex.checkUnique(MVSecondaryIndex.java:223)
at org.h2.mvstore.db.MVSecondaryIndex.add(MVSecondaryIndex.java:184)
at org.h2.mvstore.db.MVTable.addRow(MVTable.java:520)
 */