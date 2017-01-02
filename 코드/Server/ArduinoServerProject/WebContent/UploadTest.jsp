<- 
	아두이노의 역할을 대신하는 jsp파일 
	이걸 실행하면 사진을 업로드 할 수 있는데 업로드 하면 아두이노가 서버로 사진을 보내는 서블릿으로
	전송 및 실행하게 된다. 아두이노를 계속 키기보다는 이걸 사용해서 테스트 하는 것이 효과적이라 미리 정의 해놓은 것입니다.
->
<%@ page language="java" contentType="text/html; charset=EUC-KR" pageEncoding="EUC-KR"%>

<html>

<head>

 <meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

 <title>파일 업로드를 심플하게 해보아요 :-)</title>

</head>

<body>

          <!-- enctype="multipart/form-data" 파일이나 대용량 데이터 보낼때 데이터 전송 방식 --><form method="post" enctype="multipart/form-data" action="upload"> 

 작성자 : <input type="text" name="name"><br>

 제목 : <input type="text" name="subject"><br>

 파일명 : <input type="file" name="uploadFile"><br>

 

 <input type="submit" value="파일올리기"><br>

</form>

</body>

</html>
