package arduino;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AndroidServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Calendar cal = Calendar.getInstance();

		String bulletinBoardtableName = "bulletinboard_table";
		try {
			String fileName="";
			String filePath="";
			Date date = null;
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			conn = DriverManager.getConnection(
				"jdbc:mysql://localhost/studydb", //JDBC URL
				"root"
				+ ""
				+ "",	// DBMS 사용자 아이디
				"1234");	// DBMS 사용자 암호

		stmt = conn.createStatement();
		/*
		 	게시판 테이블은 무조건 처음에 만들어져 있어야 하는 걸로 만들었습니다.
		 	그래서 테이블 개수 계산시에 이 테이블을 제외하고 계산해서 
		 	사진들의 정보가 저장된 테이블들의 정보만 가지고 서버는 사진들을 보냅니다.
		 */
		rs= stmt.executeQuery("SHOW TABLES LIKE " + "'" + bulletinBoardtableName+"'");
		if(rs.next() == false) {		// 테이블이 있는지 조회, 없다면 새로 생성합니다.
			stmt.executeUpdate("CREATE TABLE " + bulletinBoardtableName + 
						"(no INT NOT NULL AUTO_INCREMENT, b_importance VARCHAR(30), "
						+ "b_title VARCHAR(40), b_date DATE, b_content VARCHAR(100),PRIMARY KEY no(no));");
						System.out.println(bulletinBoardtableName+" 테이블이 생성되었습니다. ");
						
		}	
		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='studydb'");
		int tableCnt=0;		
		if(rs.next()) {
			tableCnt+=rs.getInt(1);
		}
		tableCnt--; // 기본적으로 게시판 테이블이 존재합니다.
		System.out.println("테이블 갯수 "+ tableCnt);
		int i=0;
		while(true) {
			String tableName = "m_"+(cal.get(Calendar.MONTH)+1-i) +"_table";
			rs= stmt.executeQuery("SHOW TABLES LIKE " + "'" + tableName+"'");
			System.out.println("tableCnt = " +tableCnt);
			i++;
			if(rs.next() != false ) {
				tableCnt--;
			}
			else
				continue;
			
			if(tableCnt==0) 
				break;

			System.out.println("test");
		}
		System.out.println("test " + i);
		HashMap <String, ArrayList<byte[]>> saveDataMap = new HashMap <String, ArrayList<byte[]>>();
		HashMap <String, ArrayList<String>> saveFileNameDataMap = new HashMap <String, ArrayList<String>>();
		Statement stms=conn.createStatement();
		for(int j=0; j<i; j++) {
			String tableName = "m_"+(cal.get(Calendar.MONTH)+1-j) +"_table";
			
			ArrayList<byte[]> list = new ArrayList<byte[]>();
			ArrayList<String> nameList = new ArrayList<String>();
			rs= stmt.executeQuery("SHOW TABLES LIKE " + "'" + tableName+"'");
			if(rs.next()==false){
				System.out.println(tableName+"ㅇㅇㅇ");
				continue;
			}

			stms=conn.createStatement();
			rs= stms.executeQuery("SELECT * FROM "+tableName);
			
			while(rs.next()){
					fileName = rs.getString("fname");
					filePath = rs.getString("fpath");
					date = rs.getDate("fdate");
					nameList.add(fileName);
					File imgFile= new File(filePath+fileName);
					byte[] bytes = new byte[(int) imgFile.length()];
					DataInputStream in = new DataInputStream(new FileInputStream(imgFile));
					in.readFully(bytes);
					in.close();
					list.add(bytes);
			}
			saveDataMap.put((cal.get(Calendar.MONTH)+1-j)+"month", list);
			saveFileNameDataMap.put((cal.get(Calendar.MONTH)+1-j)+"month_name", nameList);
		}

		System.out.println("파일명 : " + fileName);
		
			response.setContentType("application/octet-stream");
			
		String downName = null;
		if(request.getHeader("user-agent").indexOf("MSIE") == -1)
		{
			downName = new String(fileName.getBytes("UTF-8"), "8859_1");
		}
		else
		{
			downName = new String(fileName.getBytes("EUC-KR"), "8859_1");
		}
		
		// 무조건 다운로드하도록 설정
		response.setHeader("Content-Disposition","attachment;filename=\"" + downName + "\";");
		/*
		  Object로 한번에 보내서 데이터의 손실을 최소화 합니다.
		 */
		ServletOutputStream servletOutputStream = response.getOutputStream();
		ObjectOutputStream oos =new ObjectOutputStream(servletOutputStream);
		oos.writeObject(saveFileNameDataMap);
		oos.flush();
		oos.writeObject(saveDataMap);

		oos.flush();
		oos.close();
		servletOutputStream.flush();
		System.out.println("보냄");
		
		} catch (Exception e) {
			throw new ServletException(e);
			
		} finally {
			try {if (rs != null) rs.close();} catch(Exception e) {}
			try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

}
