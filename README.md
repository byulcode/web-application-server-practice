# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* InputStream(1byte읽기)을 읽기 위해선 InputStreamReader(문자로 읽기 char)로 감싸고, 그것을 다시 BufferedReader(통째로 읽기 String)로 감싸 줘야 한다. 

  

### 요구사항 2 - get 방식으로 회원가입
* GET 방식을 사용할 때 사용자가 입력한 데이터가 다음과 같은 구조로 전달되는것을 알 수 있다.
  * **GET /user/create?userID=byulcode&password=password&name=byulcode&email=bybit%40gmail.com HTTP/1.1**

### 요구사항 3 - post 방식으로 회원가입
* POST 방식으로 데이터를 전달하기 위해 form.html의 method 속성을 get에서 post로 수정하면 요청 라인은 다음과 같다.
  * **POST /user/create HTTP/1.1**
* GET 방식으로 요청할 때 요청 URL에 포함되어 있던 쿼리 스트링이 없어졌다. 쿼리 스트링은 HTTP 요청의 본문(body)을 통해 전달된다. 
* 본문의 길이가 Content-Length라는 필드 이름으로 전달된다.</br>

### 요구사항 4 - redirect 방식으로 이동
* 302 상태 코드를 활용해 페이지를 이동할 경우 요청과 응답이 두번 발생한다. 이러한 방식은 리다이렉트 이동 방식으로 알려져 있다.
* HTTP는 서버에서 클라이언트로 응답을 보낼 때 상태 코

### 요구사항 5 - cookie
* HTTP는 무상태 프로토콜이기 때문에 각 요청 사이에 상태를 공유할 수 없다.
* HTTP는 서버가 클라이언트의 행위를 기억하기 위한 목적으로 쿠키를 지원한다.

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 