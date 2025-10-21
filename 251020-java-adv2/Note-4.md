## XML, JSON, 데이터베이스
- 회원 객체와 같이 구조화된 데이터를 통신할 때 사용하는 데이터 형식
- 1. 객체 직렬화의 한계
- 클래스 구조가 변경되면 이전에 직렬화된 객체와의 호환성 문제 발생
- 자바 직렬화는 자바 플랫폼에 종속적이어서 다른 언어나 시스템과의 상호 운용성이 떨어진다.
- 직렬화/역직렬화 과정이 상대적으로 느리고 리소스를 많이 사용한다.
- 직렬화된 형식을 커스터마이즈하기 어렵다.
- 직렬화된 데이터의 크기가 상대적으로 크다.
- 2. XML
```xml
<member>
    <id>id1</id>
    <name>name1</name>
    <age>20</age>
</member>
```
- 플랫폼 종속성 문제가 해결되었다.
- 유연하지만 복잡하고 무거웠다.
- 태그를 포함한 XML 문서는 크기가 커서 네트워크 전송 비용이 컸다.
- 3. JSON
```json
{ "member": { "id": "id1", "name": "name1", "age": 20 } }
```
- 가볍고 자바스크립트와 호환된다.
- 텍스트 기반 포맷이어서 디버깅과 개발이 쉽다.
- 웹 환경에서 표준 데이터 교환 포맷이 되었다.
- 4. Protobuf, Avro
- JSON보다 작은 용량으로 더 빠르게 통신할 수 있다.
- JSON보다 호환성은 덜하다. 사람이 읽기 어렵다.
- byte 기반에 용량과 성능 최적화가 되어 있다.
- 5. 데이터베이스
- 어떤 형식이든 데이터를 저장할 때 파일에 저장하는 방식은 한계가 있다.
- 데이터의 무결성 보장이 어렵다. 동시 접근 등 일관성 유지 어렵다.
- 데이터 검색과 관리가 비효율적이다.
- 보안 문제가 있다. (접근 제어, 암호화 등)
- 대규모 데이터의 효율적인 백업 및 복구가 필요하다.
- 이런 문제들을 해결하는 서버 프로그램이 데이터베이스다.
# 6. File, Files
- 파일 또는 디렉토리를 다룰 때 File, Files, Path 클래스를 사용한다.
- 251020-java-adv2/src/files/FileMain.java // File
- 251020-java-adv2/src/files/File1Main.java // Path + Files
- 251020-java-adv2/src/files/File2Main.java // File
- 251020-java-adv2/src/files/File3Main.java // Path + Files
- Files로 문자로 된 파일 읽기: File4Main.java
- Files로 문자로 된 파일 라인 단위로 읽기: File5Main.java
- 파일 복사 최적화: File6Main.java, File7Main.java, File8Main.java
- File5Main.java: Files.readAllLines(path) 대신 Files.lines(path)를 쓸 수도 있다.