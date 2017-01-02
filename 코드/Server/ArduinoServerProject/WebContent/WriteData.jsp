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
   <%	
   
  	request.setCharacterEncoding("UTF-8");
	response.setCharacterEncoding("UTF-8"); 
	System.out.println("일단접속은 되요");	
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	Calendar cal = Calendar.getInstance();
	String tableName = "bulletinboard_table";
	try {
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		conn = DriverManager.getConnection(
				"jdbc:mysql://localhost/studydb", //JDBC URL
				"root"
				+ ""
				+ "",	// DBMS 사용자 아이디
				"1234");	// DBMS 사용자 암호
		stmt = conn.createStatement();
		
		rs= stmt.executeQuery("SHOW TABLES LIKE " + "'" + tableName+"'");
		if(rs.next() == false) {
			stmt.executeUpdate("CREATE TABLE " + tableName + 
					"(no INT NOT NULL AUTO_INCREMENT, b_importance VARCHAR(30), "
					+ "b_title VARCHAR(40), b_date DATE, b_content VARCHAR(100),PRIMARY KEY no(no));");
			//stmt.executeUpdate("ALTER TABLE " + tableName +" AUTO_INCREMENT = 1");
			
			System.out.println(tableName+" 테이블이 생성되었습니다. ");
					
		}
		
		
	String getAndroidDataTitle = new String(request.getParameter("title").getBytes("UTF-8"));
	String getAndroidDataContent = new String(request.getParameter("content").getBytes("UTF-8"));
	String getAndroidDataImportance = new String(request.getParameter("importance").getBytes("UTF-8"));
	System.out.println("보낸 콘텐트"+getAndroidDataImportance);
	Date getAndroidDataDate =new Date( new SimpleDateFormat("yyyy-mm-dd").parse(
			new String(request.getParameter("date").getBytes("UTF-8"))).getTime());

	String sql = "INSERT INTO " + tableName +"(b_importance, b_title, b_date, b_content) VALUES(?, ?, ?, ?)";
	PreparedStatement psmt = conn.prepareStatement(sql);
	psmt.setString(1, getAndroidDataImportance);
	psmt.setString(2, getAndroidDataTitle);
  	psmt.setDate(3, getAndroidDataDate);
  	psmt.setString(4, getAndroidDataContent);
	psmt.executeUpdate();		/* 
	for(int i=0; i<getAndroidData.length; i++) {
		System.out.println(getAndroidData[i]); 
		}
	} */
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