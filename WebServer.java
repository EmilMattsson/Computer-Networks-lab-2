
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
	private DataInputStream in = null;
	private DataOutputStream binaryOut = null;
	private String request;
	private String contentType;
	private File requestedItem;
	httpResponse httpResponse;

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
			httpResponse = new httpResponse(binaryOut);

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
					else if (requestedItem.getPath().contains(".png")) {
						contentType = "image/png";
					}
					if (requestedItem.getPath().contains("dir3") || requestedItem.getAbsolutePath().contains("Shared") == false) {
						httpResponse.response403();
					}
					/* The else statement below should be "else if(requestedItem.isFile())"
					 * but to generate an error in server to get 500-error it is not
					 */
					else {
						/* send the existing requested item to the browser in a 200-OK response*/
						httpResponse.response200(contentType, requestedItem);
					}
					//					else if (requestedItem.isDirectory()) {
					//						response404();
					//					}
				}
				else if (requestedItem.exists() == false){
					/* If item doesn't exist, generate a 404-error response */
					httpResponse.response404();
				}
			}	else {
				/* Insert POST method here */
			}
		} catch (IOException e){
			e.printStackTrace();
			/* If there is an server error, send a 500-error response */
			httpResponse.response500();
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
}
