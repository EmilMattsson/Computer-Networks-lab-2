import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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

	public WebServerThread(Socket s, int i, byte[] b){
		this.clientConnection = s;
		this.id = i;
		this.buffer = b;
	}

	private static String getText(File file) throws FileNotFoundException{		//Method for reading the text from the selected file
		Scanner scan = new Scanner(file);
		String text = "";
		while (scan.hasNextLine()){
			text += scan.nextLine();
		}
		scan.close();
		return text;
	}

	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		DataOutputStream binaryOut = null;
		FileInputStream fis = null;
		String request;

		try {
			String webServerAddress = clientConnection.getInetAddress().toString();
			System.out.println("New Connection:" + webServerAddress);
			in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));

			request = in.readLine();
			System.out.println("--- Client request: " + request);



			String[] parts = request.split("\\ ");
			String extension = parts[1];
			System.out.println(extension);

			if(extension.contains(".png"))
			{
				File file = new File("TCP Client with small buffer size.png");
				fis = new FileInputStream(file);
				byte[] data = new byte[(int) file.length()];
				fis.read(data);
				fis.close();

				binaryOut = new DataOutputStream(clientConnection.getOutputStream());
				binaryOut.writeBytes("HTTP/1.0 200 OK\r\n");
				binaryOut.writeBytes("Content-Type: image/png\r\n");
				binaryOut.writeBytes("Content-Length: " + data.length);
				binaryOut.writeBytes("\r\n\r\n");
				binaryOut.write(data);

				binaryOut.close();
			}
			else if (extension.contains(".html")){
				out = new PrintWriter(clientConnection.getOutputStream(), true);
				out.println("HTTP/1.0 200");
				out.println("Content-type: text/html");
				out.println("Server-name: myserver");

				Path p = Paths.get("C:\\Users\\Emil\\Java_kurser\\1DV701_assign2\\src\\dir1\\subdir1\\Hello world.html");
				Files.readAllBytes(p);
				byte[] b = new byte[Files.readAllBytes(p).length];
				b = Files.readAllBytes(p);
				binaryOut.writeBytes("Content-length: " + b.length);
				binaryOut.writeBytes("");
				binaryOut.write(b);
				binaryOut.flush();
				binaryOut.close();
				
				/*File html1 = new File("Hello world.html");
				String response = getText(html1);
				out.println("Content-length: " + response.length());
				out.println("");
				out.println(response);
				out.flush();
				out.close();
				clientConnection.close();*/
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		finally {
			try {
				/* Close socket and terminate thread etc. */
				in.close();
				out.close();
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
