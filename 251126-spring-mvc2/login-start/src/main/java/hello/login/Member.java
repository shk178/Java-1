package hello.login;

import lombok.Data;

@Data
public class Member {
    private Long sequenceId;
    private String loginId;
    private String loginPwd;
}
