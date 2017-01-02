package arduino;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
@WebServlet("/requestInitInfo")

/*
  이 서블릿은 게시판의 리스트들의 정보들을 데이터베이스에 전부 받아서
  안드로이드 어플리케이션으로 보내주는 역할을 합니다.
  보내는 방식은 JSON 파싱 방법을 이용해 데이터를 보냅니다.
*/
public class AndroidBulletinBoardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{ 
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		System.out.println("출력됨");
		Date saveDate = new Date(Calendar.getInstance().getTimeInMillis());	// 작성일자는 서버의 실시간으로 받는다.
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
			if(rs.next() == false) {		// 테이블이 있는지 조회, 없다면 새로 생성합니다.
				stmt.executeUpdate("CREATE TABLE " + tableName + 
						"(no INT NOT NULL AUTO_INCREMENT, b_importance VARCHAR(30), "
						+ "b_title VARCHAR(40), b_date DATE, b_content VARCHAR(100),PRIMARY KEY no(no));");
						System.out.println(tableName+" 테이블이 생성되었습니다. ");
						
			}
			/*
			  두개의 게시판 글 게시
			 *//*
			String sql = "INSERT INTO " + tableName +"(b_importance, b_title, b_date, b_content) VALUES(?, ?, ?, ?)";
			PreparedStatement psmt = conn.prepareStatement(sql);
			psmt.setString(1, "good");
			psmt.setString(2, "Today is Good Day!");
		  	psmt.setDate(3, saveDate);
		  	psmt.setString(4, "content 1");
			psmt.executeUpdate();
			sql = "INSERT INTO " + tableName +"(b_importance, b_title, b_date, b_content) VALUES(?, ?, ?, ?)";
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, "");
			psmt.setString(2, " Today is !!!");
		  	psmt.setDate(3, saveDate);
		  	psmt.setString(4, "content 2");
			psmt.executeUpdate();		*/
			// 게시판 검색
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
					"SELECT no, b_importance, b_title, b_date, b_content" + 
					" FROM " + tableName+
					" ORDER BY no DESC");
			
			response.setContentType("text/html; charset=UTF-8");
			

			PrintWriter out = response.getWriter();
			JSONObject jsonMain = new JSONObject(); // 객체
			JSONArray jArray = new JSONArray(); // 배열
			JSONObject jObject = null;
			int i=0;
			while(rs.next()) {
				jObject = new JSONObject(); // JSON내용을 담을 객체.
				jObject.put("importance", rs.getString("b_importance"));
				jObject.put("title", rs.getString("b_title"));
				jObject.put("date", rs.getDate("b_date").toString());
				jObject.put("content", rs.getString("b_content"));
				jArray.add(i, jObject);	
				i++;
			}	
			if(i!=0){
			jsonMain.put("dataSend", jArray); // JSON의 제목 지정
			out.println(jsonMain);
			out.flush();
			}
		} catch (Exception e) {
			throw new ServletException(e);
			
		} finally {
			try {if (rs != null) rs.close();} catch(Exception e) {}
			try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

}
