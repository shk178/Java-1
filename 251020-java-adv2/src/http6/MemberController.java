package http6;

import java.io.IOException;
import java.util.List;

public class MemberController {
    private final MemberRepository memberRepository;
    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    @Mapping("/")
    public void home(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>home</h1>").append("\n");
        sb.append("<ul>").append("\n");
        sb.append("<li><a href='/members'>멤버 리스트</a></li>").append("\n");
        sb.append("<li><a href='/add-member-form'>멤버 추가</a></li>").append("\n");
        sb.append("</ul>");
        String body = sb.toString();
        response.writeBody(body);
    }
    @Mapping("/members")
    public void members(HttpResponse response) {
        List<Member> members = MemberRepository.readAllMembers();
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>멤버 리스트</h1>");
        sb.append("<ul>").append("\n");
        for (Member member : members) {
            sb.append("<li>").append(member).append("</li>").append("\n");
        }
        sb.append("</ul>");
        String body = sb.toString();
        response.writeBody(body);
    }
    @Mapping("/add-member-form")
    public void addMemberForm(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>멤버 추가</h1>").append("\n");;
        sb.append("<form action='/add-member' method='POST'>");
        sb.append("<input type='text' name='id' placeholder='멤버 id'>");
        sb.append("<input type='text' name='name' placeholder='멤버 name'>");
        sb.append("<input type='text' name='age' placeholder='멤버 age'>");
        sb.append("<button type='submit'>추가</button>");
        sb.append("</form>");
        String body = sb.toString();
        response.writeBody(body);
    }
    @Mapping("/add-member")
    public void addMember(HttpRequest request, HttpResponse response) {
        String id = request.getQueryParam("id");
        String name = request.getQueryParam("name");
        int age = Integer.parseInt(request.getQueryParam("age"));
        Member member = new Member(id, name, age);
        try {
            MemberRepository.registerMember(member);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>멤버 추가 완료</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
}
