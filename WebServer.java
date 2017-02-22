
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
	private byte[] b;
	private DataInputStream in = null;
	private DataOutputStream binaryOut = null;
	private String request;
	private String contentType;
	private DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	private Date date = new Date();
	private Path p;
	private File requestedItem;

	public WebServerThread(Socket s, int i, byte[] b){
		this.clientConnection = s;
		this.id = i;
		this.buffer = b;
	}

	@Override
	public void run() {
		try {
			in = new DataInputStream(clientConnection.getInputStream());
			binaryOut = new DataOutputStream(clientConnection.getOutputStream());
			String webServerAddress = clientConnection.getInetAddress().toString();
			System.out.println("New Connection:" + webServerAddress);
			buffer = new byte[buffer.length];

			/* The thread gets a request string from a browser, usually GET-request */
			in.read(buffer);
			request = new String(buffer).trim();
			String[] parts = request.split("\n");
			request = parts[0];
			System.out.println("--- Client request: " + request);

			/* If the request is a GET-request, split it and get the part that specifies what is asked for 
			 * e.g. /dir1/subdir1/index.html
			 */
			if (request.contains("GET")){
				String[] parts2 = request.split("\\ ");
				request = parts2[1];
				request = request.replaceFirst("/", "");
				System.out.println(request);
				requestedItem = new File(request);

				/* If the requested item exist, check what kind of item it is */
				if (requestedItem.exists()) {

					if (requestedItem.isDirectory()) {
						File[] dirContent = requestedItem.listFiles();

						if (dirContent.length > 0) {
							requestedItem = dirContent[0];
						}
						System.out.println(requestedItem.getPath());
					}

					if (requestedItem.getPath().contains(".html")) {
						contentType = "text/html";
					}
					//					else if (requestedFile.getPath().contains(".htm")) {
					//						contentType = "text/htm";
					//					}
					else if (requestedItem.getPath().contains(".png")) {
						contentType = "image/png";
					}
					if (requestedItem.getPath().contains("dir3")) {
						response403();
					}
					/* The else statement below should be "else if(requestedItem.isFile())"
					 * but to generate an error in server to get 500-error it is not
					 */
					else {
						/* send the existing requested item to the browser in a 200-OK response*/
						response200(contentType);
					}
					//					else if (requestedItem.isDirectory()) {
					//						response404();
					//					}
				}
				else if (requestedItem.exists() == false){
					/* If item doesn't exist, generate a 404-error response */
					response404();
				}
			}	else {

			}
		} catch (IOException e){
			e.printStackTrace();
			/* If there is an server error, send a 500-error response */
			response500();
		}
		finally {
			try {
				/* Close socket and terminate thread etc. */
				in.close();
				binaryOut.close();
				clientConnection.close();
				System.out.println("Server Thread_" + id + ": has served a client");
				Thread.currentThread().interrupt();
				return;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/* Method for 200-response sending back an existing requested item */
	private void response200(String s) {
		String contentType = s;
		p = Paths.get(requestedItem.getPath());
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			System.out.println(requestedItem.getPath());
			binaryOut.writeBytes("HTTP/1.1 200 OK\r\n"
					+ "Content-type: " + contentType + "\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			binaryOut.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
			response500();
		}
	}

	/* Method for 403-response, user requesting an forbidden item */
	private void response403() {
		p = Paths.get("dir3/subdir3/403.html");
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			binaryOut.writeBytes("HTTP/1.1 403 FORBIDDEN\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			binaryOut.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
			response500();
		}
	}

	/* Method for 404-response, user requesting an item that doesn't exist */
	private void response404() {
		p = Paths.get("dir3/subdir3/404.html");
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			binaryOut.writeBytes("HTTP/1.1 404 NOT FOUND\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			binaryOut.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
			response500();
		}
	}

	/* Method for 500-response, an internal server error */
	private void response500() {
		p = Paths.get("dir3/subdir3/500.html");
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			binaryOut.writeBytes("HTTP/1.1 500 Internal Server Error\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			binaryOut.write(b);

		}	catch (IOException e){
			e.printStackTrace();
		}
	}
}
