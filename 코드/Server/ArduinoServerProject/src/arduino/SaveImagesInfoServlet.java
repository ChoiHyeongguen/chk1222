package arduino;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

@WebServlet("/arduino/savedb")
public class SaveImagesInfoServlet extends GenericServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void service(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		String fileName =(String) request.getAttribute("filename");
		String filePath = (String) request.getAttribute("filepath");
		Date saveDate = (Date) request.getAttribute("date");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Calendar cal = Calendar.getInstance();
		String tableName = "m_"+(cal.get(Calendar.MONTH) +1) +"_table";
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
						"(no INT NOT NULL AUTO_INCREMENT, fname VARCHAR(50), "
						+ "fpath VARCHAR(50), fdate DATE,PRIMARY KEY no(no));");
				//stmt.executeUpdate("ALTER TABLE " + tableName +" AUTO_INCREMENT = 1");
				
				System.out.println(tableName+" 테이블이 생성되었습니다. ");
						
			}
			
			String sql = "INSERT INTO " + tableName +"(fname, fpath, fdate) VALUES(?, ?, ?)";
			PreparedStatement psmt = conn.prepareStatement(sql);
			psmt.setString(1, fileName);
			psmt.setString(2, filePath);
		  	psmt.setDate(3, saveDate);
			psmt.executeUpdate();
			
			
			
			rs = stmt.executeQuery(
					"SELECT no, fname, fpath, fdate" + 
					" FROM " + tableName+
					" ORDER BY no ASC");
			
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<html><head><title>회원목록</title></head>");
			out.println("<body><h1>회원목록</h1>");
			while(rs.next()) {
				out.println(
					rs.getInt("no") + "," +
					rs.getString("fname") + "," +
					rs.getString("fpath") + "," + 
					rs.getDate("fdate") + "<br>"
				);
			}
			out.println("</body></html>");
		} catch (Exception e) {
			throw new ServletException(e);
			
		} finally {
			try {if (rs != null) rs.close();} catch(Exception e) {}
			try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}

	}
}
