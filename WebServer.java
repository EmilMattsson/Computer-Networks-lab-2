
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
	private DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	private Date date = new Date();
	private DataInputStream in = null;
	private DataOutputStream binaryOut = null;
	private FileInputStream fis = null;
	private Path p;

	public WebServerThread(Socket s, int i, byte[] b){
		this.clientConnection = s;
		this.id = i;
		this.buffer = b;
	}

	private void getFile(String path) {
		File dir = new File(path);
		File[] dirContent = dir.listFiles();
		for (File e : dirContent){
			System.out.println(e.getPath() + " hej");
		}
		File f = dirContent[0];
		if (f.getPath().contains(".html")) {
			htmlResponse(f.getPath());
			System.out.println(f.getPath());
		}
	}

	private void htmlResponse(String path) {
		p = Paths.get(path);
		try {
			Files.readAllBytes(p);
			byte[] b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);

			binaryOut.writeBytes("HTTP/1.0 200 OK\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			binaryOut.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void pngResponse() {

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

			buffer = new byte[buffer.length];
			in.read(buffer);
			request = new String(buffer).trim();
			String[] parts = request.split("\n");
			request = parts[0];
			System.out.println("--- Client request: " + request);

			if (request.contains("GET")){
				String[] parts2 = request.split("\\ ");
				String extension = parts2[1];
				//System.out.println(extension);
				extension = extension.replaceFirst("/", "");
				//System.out.println(extension);
				if (extension.contains("%20")) {
					extension = extension.replaceAll("%20", " ");
				}
				//System.out.println(extension);
				//getFile(extension);

				File requestedFile = new File(extension);
				if (requestedFile.isDirectory()) {
					File[] dirContent = requestedFile.listFiles();
					requestedFile = dirContent[0];
				}
				if (requestedFile.exists()) {
					String contentType = "";
					if (requestedFile.getPath().contains(".html")) {
						//System.out.println(requestedFile.getPath());
						contentType = "text/html";
					}
					else if (requestedFile.getPath().contains(".png")) {
						//System.out.println(requestedFile.getPath());
						contentType = "image/png";
					}
					p = Paths.get(requestedFile.getPath());
					Files.readAllBytes(p);
					byte[] b = new byte[Files.readAllBytes(p).length];
					b = Files.readAllBytes(p);

					binaryOut.writeBytes("HTTP/1.0 200 OK\r\n"
							+ "Content-type: " + contentType + "\r\n"
							+ "Server-name: Myserver\r\n"
							+ "Date: " + df.format(date) + "\r\n"
							+ "Content-length: " + b.length
							+ "\r\n\r\n");
					binaryOut.write(b);
				}	
				else if (requestedFile.exists() == false){
					p = Paths.get("dir3/subdir3/404.png");
					Files.readAllBytes(p);
					byte[] b = new byte[Files.readAllBytes(p).length];
					b = Files.readAllBytes(p);
					binaryOut.writeBytes("HTTP/1.0 404 NOT FOUND\r\n"
							+ "Content-type: image/png\r\n"
							+ "Server-name: Myserver\r\n"
							+ "Date: " + df.format(date) + "\r\n"
							+ "Content-length: " + b.length
							+ "\r\n\r\n");
					binaryOut.write(b);
				}

				//				if(extension.contains(image1Path) || extension.contains("dir2/subdir2")){
				//					fis = new FileInputStream(file);
				//					byte[] data = new byte[(int) file.length()];
				//					fis.read(data);
				//					fis.close();
				//
				//					binaryOut.writeBytes("HTTP/1.0 200 OK\r\n");
				//					binaryOut.writeBytes("Content-Type: image/png\r\n");
				//					binaryOut.writeBytes("Server-name: Myserver\r\n");
				//					binaryOut.writeBytes("Date: " + df.format(date) + "\r\n");
				//					binaryOut.writeBytes("Content-Length: " + data.length);
				//					binaryOut.writeBytes("\r\n\r\n");
				//					binaryOut.write(data);
				//				}
				//				else if (extension.contains("index.html") || extension.contains("dir1/subdir1")){
				//					p = Paths.get(html);
				//					Files.readAllBytes(p);
				//					byte[] b = new byte[Files.readAllBytes(p).length];
				//					b = Files.readAllBytes(p);
				//					
				//					binaryOut.writeBytes("HTTP/1.0 200 OK\r\n"
				//							+ "Content-type: text/html\r\n"
				//							+ "Server-name: Myserver\r\n"
				//							+ "Date: " + df.format(date) + "\r\n"
				//							+ "Content-length: " + b.length
				//							+ "\r\n\r\n");
				//					binaryOut.write(b);
				//				}
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
