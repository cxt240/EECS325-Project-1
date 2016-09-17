import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * EECS 325 Project 1
 * @author Chris Tsuei (cxt240)
 */
public class proxyd {
	
	public static void main (String [] args) throws IOException{

		ArrayList<web_node> cache = new ArrayList<web_node>(200);
		//getting the port number from the user if any
		int port;
		try {
			port = Integer.parseInt(args[1]);
		}
		catch(Exception e) {
			System.out.println("valid port not specified, setting default port as 5036");
			port = 5036; //no valid input, port 5036 (I am student number 36)
		}
		
		//creating the serverSocket for the proxy
		ServerSocket new_proxy = null;
		try {
			new_proxy = new ServerSocket(port);
			System.out.println("Started server socket on port " + port);
		}
		catch(Exception e) {
			System.err.println("Can't listen to port");
			System.exit(-1);
		}
		
		//requests for connection threaded and finished here
		try {
			for(;;) {//infinite loop
				new Thread_proxy(new_proxy.accept()).start();
			//	new Thread_proxy_cache(new_proxy.accept(), cache).start();
			}
		} catch (IOException e) {
			System.out.println(e + "\n");
			System.out.println("invalid port number");
		}
	}
}
