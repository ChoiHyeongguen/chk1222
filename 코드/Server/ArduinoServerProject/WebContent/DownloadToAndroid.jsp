<%@ page language="java" contentType="text/html; charset=euc-kr"
	pageEncoding="euc-kr"%>
<%@ page import="java.io.*,java.net.URLEncoder"%>
<%
	ServletContext sc = this.getServletContext();
	String fileName = "", state = "", androidState=(String)request.getParameter("android");
	//int fileLength = 0;
	
	fileName = (String) sc.getAttribute("FNAME");
	//fileLength =(String) sc.getAttribute("FLENGTH")!=null ? Integer.parseInt((String) sc.getAttribute("FLENGTH")) : 0;
	state = (String) sc.getAttribute("STATE");
	System.out.println(fileName + " " +state+ " " + androidState);
	//String fileName = new String("cam1.jpg");
	if (fileName!=null   && state!=null && state.equals("TRUE")  
			&& androidState!=null ) {
		String fileName2 = new String(fileName.getBytes("euc-kr"), "8859_1");
		response.setContentType("image/jpg");
		File file = new File("C:/test/" + fileName);
		//byte[] bytestream = new byte[fileLength];
		byte[] bytestream = new byte[(int)file.length()]; 

		response.setHeader("Content-Disposition", "attachment;filename=" + fileName2 + ";");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setContentLength((int) file.length());
		FileInputStream filestream = new FileInputStream(file);
		int i = 0, j = 0;

		while ((i = filestream.read()) != -1) {
			bytestream[j] = (byte) i;
			j++;
		}
		out.clear();
		out=pageContext.pushBody(); 
		OutputStream outStream = response.getOutputStream();
		/* 
		out.clear();
		response.flushBuffer(); */
		outStream.write(bytestream);
		//outStream.flush();
		System.out.println(bytestream.length);
		outStream.close();
		sc.setAttribute("STATE", "TRUE");
	}
	else {
		response.setContentLength(0);
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=euc-kr">
<title>≈∏¿Ã∆≤</title>
</head>
<body>

</body>
</html>
