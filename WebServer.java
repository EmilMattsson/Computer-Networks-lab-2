
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebServer {
	public static final int BUFSIZE= 1024;
	public static final int MYPORT= 4951;
	public static int counter = 0;

	public static void main(String[] args) throws IOException {
		byte[] buf= new byte[BUFSIZE];
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(MYPORT);
		Socket clientConnection = null;

		System.out.println("Server online");
		while (true) {
			try {
				/* Server accepts a new client connection */
				clientConnection = server.accept();
			} catch (Exception e) {
				e.printStackTrace();
			}
			/* For each new client connection the server hands the client to a new thread */
			Runnable WebServerThread = new WebServerThread(clientConnection, counter++, buf);
			new Thread(WebServerThread).start();
		}
	}
}

class WebServerThread implements Runnable {
	protected Socket clientConnection;
	private final int id;
	private byte[] buffer;
	File file = new File("dir2\\subdir2\\TCP Client with small buffer size.png");
	String image1Path = file.getAbsolutePath();

	public WebServerThread(Socket s, int i, byte[] b){
		this.clientConnection = s;
		this.id = i;
		this.buffer = b;
	}

	@Override
	public void run() {
		DataInputStream in = null;
		DataOutputStream binaryOut = null;
		FileInputStream fis = null;
		String request;
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		Date date = new Date();
		Path p;

		try {
			in = new DataInputStream(clientConnection.getInputStream());
			binaryOut = new DataOutputStream(clientConnection.getOutputStream());
			String webServerAddress = clientConnection.getInetAddress().toString();
			System.out.println("New Connection:" + webServerAddress);
			String html = new File("dir1\\subdir1\\index.html").getAbsolutePath();
			System.out.println(html + " <-- path");
			
			//byte[] buffer = new byte[buffer.length];
			in.read(buffer);
			request = new String(buffer).trim();
			String[] parts = request.split("\n");
			request = parts[0];
			System.out.println("--- Client request: " + request);
			
			if (request.contains("GET")){
				String[] parts2 = request.split("\\ ");
				String extension = parts2[1];
				System.out.println(extension);

				if(extension.contains(image1Path) || extension.contains("/dir2/subdir2")){
					fis = new FileInputStream(file);
					byte[] data = new byte[(int) file.length()];
					fis.read(data);
					fis.close();

					binaryOut = new DataOutputStream(clientConnection.getOutputStream());
					binaryOut.writeBytes("HTTP/1.0 200 OK\r\n");
					binaryOut.writeBytes("Content-Type: image/png\r\n");
					binaryOut.writeBytes("Server-name: Myserver\r\n");
					binaryOut.writeBytes("Date: " + df.format(date) + "\r\n");
					binaryOut.writeBytes("Content-Length: " + data.length);
					binaryOut.writeBytes("\r\n\r\n");
					binaryOut.write(data);
				}
				else if (extension.contains(html) || extension.contains("/dir1/subdir1")){
					binaryOut.writeBytes("HTTP/1.0 200 OK\r\n");
					binaryOut.writeBytes("Content-type: text/html\r\n");
					binaryOut.writeBytes("Server-name: Myserver\r\n");
					binaryOut.writeBytes("Date: " + df.format(date) + "\r\n");

					p = Paths.get(html);
					Files.readAllBytes(p);
					byte[] b = new byte[Files.readAllBytes(p).length];
					b = Files.readAllBytes(p);
					binaryOut.writeBytes("Content-length: " + b.length);
					binaryOut.writeBytes("\r\n\r\n");
					binaryOut.write(b);
				}
			}	else {
				
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		finally {
			try {
				/* Close socket and terminate thread etc. */
				in.close();
				binaryOut.close();
				clientConnection.close();
				//System.out.println("Server Thread_" + id + ": has served a client");
				Thread.currentThread().interrupt();
				return;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
