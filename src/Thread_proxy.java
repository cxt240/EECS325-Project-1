import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

/**
 * Threading for the normal proxy
 * @author Chris Tsuei(cxt240)
 * EECS 325
 */
public class Thread_proxy extends Thread{
	
	//socket for the proxy to listen to the client
	private Socket proxy_connect;
	
	/**
	 * constructor for the threads
	 * @param new_proxy the port the client is communicating from
	 */
	public Thread_proxy(Socket new_proxy) {
		super("Thread_proxy");
		this.proxy_connect = new_proxy;
	}

	/**
	 * overriding the run method of thread to run the main proxy methods for receiving requests
	 * and making requests as well as dealing with  resposnses to said requests
	 */
	public void run() {
		
		try{ 
			//listeners from the client
			InputStreamReader proxy_in = new InputStreamReader(proxy_connect.getInputStream());
			BufferedReader proxy_reader = new BufferedReader(proxy_in);
			
			OutputStream proxy_out = proxy_connect.getOutputStream();
			
			//Creating the request header (to be used twice)
			ByteArrayOutputStream header = new ByteArrayOutputStream();
			String output;
			Boolean firstLine = true;
			String object_url = null;
			
			//going through the HTTP request, picking it apart and ensuring the connection is closed
			while((output = proxy_reader.readLine()) != null) {
				if(output.length() == 0) {//last command for the request so kill the loop
					break;
				}
				if(firstLine) {
					object_url = output.substring((output.indexOf(" ") + 1), (output.lastIndexOf(" HTTP")));
					firstLine = false;
					System.out.println(object_url);
				}
				else if(output.contains("onnection: ")) {//Proxy-connection or Connection, share onnection (I guess you could do toLowercase())
						output.replace("keep-alive", "close");
					}
				
				System.out.println("writing command: " + output);
				if(output.length() != 0) {
					header.write((output + "\r\n").getBytes());
				}
			}
			header.write(("\r\n").getBytes());
			
			//finding the relative URL and making a request to it from port 80
			String website_address = (new URL(object_url)).getHost(); //output for proxy socket
			Socket transmit = new Socket(website_address, 80); // 80 is the standard port for web servers
			
			transmit.getOutputStream().write(header.toByteArray());//sending header to the servers
			InputStream receive = transmit.getInputStream();
			
			//the request's response
			byte[] foward = new byte[4096];
			int reading = 0;
			while((reading = receive.read(foward))!= -1) {
				proxy_out.write(foward, 0, reading);
			}
			
			//closing all streams
			transmit.close();
			header.close();
			receive.close();
			proxy_out.close();
			proxy_in.close();
			proxy_reader.close();
		}
		catch(Exception e) {
			System.out.println("something is wrong: " + e);
		}
	}
}
