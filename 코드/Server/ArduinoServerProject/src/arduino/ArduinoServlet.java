package arduino;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.Enumeration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

public class ArduinoServlet  extends HttpServlet{

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        RequestDispatcher rd = null;
        String fileName = "";
        File file = null;
        String savePath = "C:/test/"; //<- 요기를 바꿔주면 다운받는 경로가 바뀝니다.
        Calendar cal=Calendar.getInstance();
        String monthFolderName = (cal.get(cal.MONTH) +1) +"월";		// 월별 생성 폴더
        /*
         	folderName은 위에 월별폴더를 기준으로 생성된 실질적으로
         	저장되는 폴더이름이다.
         */
        String folderName= monthFolderName+"/"
        	+	(cal.get(cal.MONTH) +1) +"월 " + cal.get(cal.DAY_OF_MONTH) + "일/";
        
        Enumeration files = null;
        System.out.println(folderName);
        File targetMonthDir = new File(savePath + monthFolderName);
        File targetDir = new File(savePath+folderName);  
        if(!targetMonthDir.exists()) {
            targetMonthDir.mkdirs();
        }
        	
        if(!targetDir.exists()) {    //디렉토리 없으면 생성.
         targetDir.mkdirs();
        }
        int maxSize = 5 * 1024 * 1024; // 최대 업로드 파일 크기 5MB(메가)로 제한
        try {

         MultipartRequest multi = new MultipartRequest(request,
        		 savePath+folderName, maxSize,  "euc-kr",  new DefaultFileRenamePolicy());

         fileName = multi.getFilesystemName("uploadFile"); // 파일의 이름 얻기

         files = multi.getFileNames();
         String name = (String)files.nextElement();         
         file = multi.getFile(name);
         if (fileName == null) { // 파일이 업로드 되지 않았을때
          System.out.print("파일 업로드 되지 않았음");
         } else { // 파일이 업로드 되었을때
             System.out.println("File Name  : " + fileName);
         }
        } catch (Exception e) {
            System.out.print("예외 발생 : " + e);
        	}
        try {
        	System.out.println("테스트 fileName" + fileName);
            BufferedWriter out = new BufferedWriter(new FileWriter("C:/test/complete.txt"));
            out.write(savePath+folderName+fileName); 
            out.newLine();
            out.flush();
            out.close();
            /*
             	아두이노의 사진이 전송이 되면 곧바로 complete.txt라는 파일을 생성하게 된다.
             	이는 사진이 전송됬다는 의미이며 이 파일안에는 단순히 파일이름을 저장하게 된다.
             	그리고 곧바로 tmp파일이 생성되고 소켓서버는 이를 감지해 사진을 안드로이드 어플리케이션으로 
             	사진을 전송하게 된다.
             */
            
            File temp = File.createTempFile("temp", ".tmp", new File("C:/test"));  
            ////////////////////////////////////////////////////////////////
          } catch (IOException e) {
              System.err.println(e); // 에러가 있다면 메시지 출력
              System.exit(1);
          }        
        request.setAttribute("filename", fileName);
        request.setAttribute("filepath", savePath+folderName);
        request.setAttribute("date",  new Date(cal.getTimeInMillis()));/*
        	ServletContext sc = this.getServletContext();
        	sc.setAttribute("FNAME", fileName);
        	sc.setAttribute("FLENGTH", file.length());
        	sc.setAttribute("STATE", "TRUE");*/
        rd = getServletContext().getRequestDispatcher("/arduino/savedb");
        rd.forward(request, response);

    }
    public void doPost(HttpServletRequest request, HttpServletResponse response)

            throws IOException, ServletException {

        doGet(request, response);

    }
}
