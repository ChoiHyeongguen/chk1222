
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.lang.*" %> 
<%@ page import="java.sql.*" %> 
<%@ page import="java.sql.Date" %> 
<%@ page import="java.util.Calendar" %> 
<%@ page import="java.io.*,java.net.URLEncoder"%>
<%@ page import="org.json.simple.*"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%--
	이 jsp는 안드로이드 어플리케이션으로 부터 게시판 번호를 받아서
	데이터베이스로 조회 하고 게시판 내용을 다시 안드로이드 어플리케이션으로 보내준다.
 --%>
   <%	
   	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	Calendar cal = Calendar.getInstance();
	String tableName = "bulletinboard_table";		// 게시판의 정보가 있는 데이터 베이스 테이블 이름
	/*
		스트링으로 받은 게시판 번호를 숫자타입으로 변환합니다.(데이터 베이스 조회를 위해서..)
	*/
	int getAndroidData = Integer.parseInt(new String(request.getParameter("no").getBytes("8859_1"),"KSC5601"));

	try {
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		conn = DriverManager.getConnection(
				"jdbc:mysql://localhost/studydb", //JDBC URL
				"root"
				+ ""
				+ "",	// DBMS 사용자 아이디
				"1234");	// DBMS 사용자 암호
   	request.setCharacterEncoding("UTF-8");
	response.setCharacterEncoding("UTF-8");
	System.out.println("하하하");
	System.out.println(getAndroidData+"의 데이터를 주세요");
	
	response.setContentType("text/html; charset=UTF-8");
	/*
		문자열만 주고 받기 때문에 JSON 파싱을 이용해서 
		정보를 주고 받습니다.
	*/
	JSONObject jsonMain = new JSONObject();
	JSONArray jArray = new JSONArray(); 
	JSONObject jObject = new JSONObject(); 
	int i=0;
	stmt = conn.createStatement();			// 이게 문제였음 ㅅㅂ
	/*
		게시판번호를 이용해 데이터베이스에 조회
	*/
	rs = stmt.executeQuery(
			"SELECT * FROM " + tableName+ " WHERE no = " + (getAndroidData+1)+" ");
	if(rs!=null){
		System.out.println("여기들어옴?");
	while(rs.next()) {		// 받아온 데이터를 한번만 읽어서
			if(i==1)
				break;
			//데이터를 jObject에 담는다.
			/*
				json은 크게 jmain
				이안에 jarray 여러개
				이안에 jobject여러개 를 넣어서 데이터를 구성하고
				jmain단위로 넘겨줍니다.
			*/
			jObject = new JSONObject(); // JSON내용을 담을 객체.
			jObject.put("content", rs.getString("b_content"));		// 데이터베이스에서 꺼내서 jobject에 삽입
			jArray.add(i, jObject);	
			i++;
		}	
	
		jsonMain.put("dataSend", jArray); 	// 최종 보낼 데이터 구성 
		out.println(jsonMain);	// 데이터 전송
		out.flush();
		}
	}
	 catch (Exception e) {
			throw new ServletException(e);
			
		} finally {
			try {if (rs != null) rs.close();} catch(Exception e) {}
			try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}
%> 
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Insert title here</title>
</head>
<body>

</body>
</html>