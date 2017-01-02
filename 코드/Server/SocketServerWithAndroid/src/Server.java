import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Server {
	private String filePath = "C:/test/";
	private String fileName = "complete.txt";
	private String finalPath = "";		// 최종경로
	HashMap<String, DataOutputStream> clients;
	private ServerSocket ServerSocket = null;
	private static InetAddress address=null;
	public static void main(String[] args) {
		new Server().start();
	}

	public Server() {
		clients = new HashMap<String, DataOutputStream>();
		Collections.synchronizedMap(clients);
	}

	private void start() {
		int port = 5001;
		Socket socket = null;

		try {
			ServerSocket = new ServerSocket(port);
			System.out.println("wait");
			while (true) {	
				System.out.println("test");
				socket = ServerSocket.accept();	//	무한 대기
				
				InetAddress ip = socket.getInetAddress();		/*// 요청받으면 ip 저장
				if(address!=null && address.equals(ip)){
					socket.close();
					continue;
				}
				address = ip;*/
				System.out.println(ip + "  connected");
				new MultiThread(socket).start();	// 쓰레드로 통신을 하기위해 socket을 인자로 넘겨준다.
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	/*
	 	파일확장자를 통해
	 	파일을 필터링하기위한 클래스
	 */
	class ExtensionFilter implements FilenameFilter {
		private String extend;

		public ExtensionFilter(String extend) {
			this.extend = extend;
		}

		@Override
		public boolean accept(File dir, String name) {
			// TODO Auto-generated method stub
			return name.endsWith(extend);
		}
	}
	class MultiThread extends Thread {
		Socket socket = null;
		DataInputStream input;
		DataOutputStream output;
			
		public MultiThread(Socket socket) {
			this.socket = socket;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
			}
		}
		public String getRealFileName(String str) {
			StringTokenizer st = new StringTokenizer(str);
			while(st.hasMoreElements()){
				String result = st.nextToken("/");
				if(result.contains(".jpg"))
					return result;
				
			}
			return "";
		}
		public void run() {
			System.out.println("Test1");
			/*
			 	사고가 발생하여 클라이언트에게 넘겨줄 정보가 있을 때 까지 계속 대기를 합니다.
			 	
			 */
			while (true) {		
				if(socket.isClosed()){
					System.out.println("종료되었습니다.");
					return;
					
				}
				File temp = new File("C:/test/");
				String[] filelist1 = temp.list(new ExtensionFilter(".tmp"));
				String[] filelist2 = temp.list(new ExtensionFilter(".txt"));
				if (filelist1.length > 0) {
					System.out.println(filelist1);
					File tmp = new File(filePath + filelist1[0]);
					tmp.delete();

					try {
						BufferedReader in = new BufferedReader(new FileReader(filePath + fileName));
						String fileAllPath;
						while ((fileAllPath = in.readLine()) != null) {
							File sendImgFile = new File(fileAllPath);		
				            finalPath = fileAllPath;
							output.flush();
							String fName =getRealFileName(fileAllPath);
							output.writeUTF(fName);
							System.out.println("test = " + fName);
							System.out.println(finalPath);
							break;
						}
						in.close();
					} catch (IOException e) {
						System.err.println(e + "!!!");
						System.exit(1);
					}

					System.out.println("complete!!");
					break;
				}
			}
			try {

				if(socket.isClosed()){
					System.out.println("종료되었습니다.");
					return;
					
				}
				File imgFile= new File(finalPath);
				byte[] bytes = new byte[(int) imgFile.length()];
				DataInputStream in = new DataInputStream(new FileInputStream(imgFile));
				in.readFully(bytes);
				in.close();
				OutputStream os = socket.getOutputStream();
				os.flush();
				ObjectOutputStream oos =new ObjectOutputStream(os);
				oos.writeObject(bytes);
				oos.flush();
				oos.close();
				os.close();
				socket.close();
				System.out.println("End");
			} catch (IOException e) {
				System.out.println(e + "@@");
			}

			File f = new File(filePath + fileName);
			f.delete();
			System.out.println("file delete");
			
			return; 
		}
	}	
}
