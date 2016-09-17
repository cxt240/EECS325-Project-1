import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Threading for the normal proxy
 * @author Chris Tsuei(cxt240)
 * EECS 325
 */
public class Thread_proxy_cache extends Thread{
	
	//socket for the proxy to listen to the client
	private Socket proxy_connect;
	private ArrayList<web_node> proxy_cache;
	/**
	 * constructor for the threads
	 * @param new_proxy the port the client is communicating from
	 */
	public Thread_proxy_cache(Socket new_proxy, ArrayList<web_node> cache) {
		super("Thread_proxy");
		this.proxy_connect = new_proxy;
		this.proxy_cache = cache;
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
			
			//cache time
			String web_address = null;
			boolean found = false;
			int search = 0;
			Date now = new Date();
			long time_found = 0;
			
			while(search < proxy_cache.size() && !found) {//looks through the cache for the web address
				if(proxy_cache.get(search).web_request.equals(object_url)) { //found it
					web_address = proxy_cache.get(search).web_address; 
					found = true;
				}
				search++; //increments up (even if not found), if found will exit at beginning of next loop
			}
			
			if(!found) {//website ip address wasn't found
				//finding the relative URL and making a request to it from port 80
				String website_address = (new URL(object_url)).getHost(); //output for proxy socket
				proxy_cache.add(new web_node(object_url, website_address)); //adds  to cache
				time_found = now.getTime();
			}
			Socket transmit = new Socket(web_address, 80); // 80 is the standard port for web servers
			
			transmit.getOutputStream().write(header.toByteArray());//sending header to the servers
			InputStream receive = transmit.getInputStream();
			
			//the request's response
			byte[] foward = new byte[4096];
			int reading = 0;
			while((reading = receive.read(foward))!= -1) {
				proxy_out.write(foward, 0, reading);
			}
			
			if(found) {
				while(now.getTime() - time_found != 500) {} // do nothing until 500 ms have passed
				
				// 500 seconds have elapsed, delete node with address
				boolean delete_found = false;
				int search2 = 0;
				while(search2 < proxy_cache.size() && !delete_found) {//looks through the cache for the web address
					if(proxy_cache.get(search).web_request.equals(object_url)) { //found it
						proxy_cache.remove(search2);
						delete_found = true;
					}
					search2++; //increments up (even if not found), if found will exit at beginning of next loop
				}
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
