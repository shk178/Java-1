<%@ page import="java.util.List" %>
<%@ page import="lecture.demo.domain.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원 목록</title>
</head>
<body>
<table>
    <thead>
        <th>id</th>
        <th>username</th>
        <th>age</th>
    </thead>
    <tbody>
<%
    List<Member> members = (List<Member>) request.getAttribute("members");
    for (Member member : members) {
%>
        <tr>
            <td><%= member.getId() %></td>
            <td><%= member.getUsername() %></td>
            <td><%= member.getAge() %></td>
        </tr>
<%
    }
%>
    </tbody>
</table>
</body>
</html>