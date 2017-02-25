import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class httpResponse {

	private Path p;
	private DataOutputStream out;
	private File requestedItem;
	private byte[] b;
	private DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	private Date date = new Date();
	
	public httpResponse(DataOutputStream dos) {
		out = dos;
	}
	
	/* Method for 200-response sending back an existing requested item */
	public void response200(String s, File f) {
		String contentType = s;
		requestedItem = f;
		p = Paths.get(requestedItem.getPath());
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			System.out.println(requestedItem.getPath());
			out.writeBytes("HTTP/1.1 200 OK\r\n"
					+ "Content-type: " + contentType + "\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			out.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
			response500();
		}
	}

	/* Method for 403-response, user requesting an forbidden item */
	public void response403() {
		p = Paths.get("dir3/subdir3/403.html");
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			out.writeBytes("HTTP/1.1 403 FORBIDDEN\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			out.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
			response500();
		}
	}

	/* Method for 404-response, user requesting an item that doesn't exist */
	public void response404() {
		p = Paths.get("dir3/subdir3/404.html");
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			out.writeBytes("HTTP/1.1 404 NOT FOUND\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			out.write(b);
		}	catch (IOException e) {
			e.printStackTrace();
			response500();
		}
	}

	/* Method for 500-response, an internal server error */
	public void response500() {
		p = Paths.get("dir3/subdir3/500.html");
		try {
			Files.readAllBytes(p);
			b = new byte[Files.readAllBytes(p).length];
			b = Files.readAllBytes(p);
			out.writeBytes("HTTP/1.1 500 Internal Server Error\r\n"
					+ "Content-type: text/html\r\n"
					+ "Server-name: Myserver\r\n"
					+ "Date: " + df.format(date) + "\r\n"
					+ "Content-length: " + b.length
					+ "\r\n\r\n");
			out.write(b);

		}	catch (IOException e){
			e.printStackTrace();
		}
	}
}
