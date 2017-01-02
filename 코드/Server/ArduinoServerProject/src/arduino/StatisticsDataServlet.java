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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/requestStatisticsData")
public class StatisticsDataServlet extends HttpServlet {
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
			rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='studydb'");
			int tableCnt=0;
			if(rs.next()) {
				tableCnt=rs.getInt(1);
			}
			tableCnt--; // 게시판에 사용되는 테이블 개수 미포함
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
			HashMap <String, ArrayList<Date>> saveDataMap = new HashMap <String, ArrayList<Date>>();
			Statement stms=conn.createStatement();
			for(int j=0; j<i; j++) {
				String tableName = "m_"+(cal.get(Calendar.MONTH)+1-j) +"_table";

				System.out.println(tableName+"?");
				ArrayList<Date> list = new ArrayList<Date>();
				rs= stmt.executeQuery("SHOW TABLES LIKE " + "'" + tableName+"'");
				if(rs.next()==false){
					System.out.println(tableName+"ㅇㅇㅇ");
					continue;
				}
				System.out.println(tableName+"!");
				stms=conn.createStatement();
				rs= stms.executeQuery("SELECT * FROM "+tableName);
				int n=0;
				while(rs.next()){
						list.add(rs.getDate("fdate"));
				}
				saveDataMap.put((cal.get(Calendar.MONTH)+1-j)+"month", list);
			}

			// ① 파일명 가져오기
			//String fileName = request.getParameter("fileName");
			
			// ② 경로 가져오기
			//String saveDir = this.getServletContext().getRealPath("/ex0820/");
			/*String saveDir = "C:/test";
			File file = new File(saveDir + "/" + fileName);
			System.out.println("파일명 : " + fileName);*/
			
			/*
			// ③ MIMETYPE 설정하기
			String mimeType = getServletContext().getMimeType(file.toString());*//*
			if(mimeType == null)
			{*/
				response.setContentType("application/octet-stream");
			//}
			
			// ④ 다운로드용 파일명을 설정
			String downName = null;
			if(request.getHeader("user-agent").indexOf("MSIE") == -1)
			{
				downName = new String(fileName.getBytes("UTF-8"), "8859_1");
			}
			else
			{
				downName = new String(fileName.getBytes("EUC-KR"), "8859_1");
			}
			
			// ⑤ 무조건 다운로드하도록 설정
			response.setHeader("Content-Disposition","attachment;filename=\"" + downName + "\";");
			
			// ⑥ 요청된 파일을 읽어서 클라이언트쪽으로 저장한다.
			//FileInputStream fileInputStream = new FileInputStream(file);
			ServletOutputStream servletOutputStream = response.getOutputStream();
			ObjectOutputStream oos =new ObjectOutputStream(servletOutputStream);
			oos.flush();
			System.out.println(saveDataMap.size());
			oos.writeObject(saveDataMap);
			/*byte b [] = new byte[1024];
			int data = 0;
			
			while((data=(fileInputStream.read(b, 0, b.length))) != -1)
			{
				servletOutputStream.write(b, 0, data);
			}
	*/
			oos.flush();
			oos.close();
			servletOutputStream.flush();
			System.out.println("보냄");
			//fileInputStream.close();
			
			} catch (Exception e) {
				throw new ServletException(e);
				
			} finally {
				try {if (rs != null) rs.close();} catch(Exception e) {}
				try {if (stmt != null) stmt.close();} catch(Exception e) {}
				try {if (conn != null) conn.close();} catch(Exception e) {}
			}
		}
}
