//import java.io.IOException;
//import java.net.SocketException;
//import org.apache.commons.net.WhoisClient;
//
///**
// * @author Crunchify.com
// *
// */
//
//public class Yesto {
//
//	public static void main(String[] args) {
//
//		Yesto obj = new Yesto();
//		System.out.println(obj.crunchifyWhois("filotrack.com"));
//		System.out.println("\nTest Finished..");
//
//	}
//
//	public String crunchifyWhois(String domainName) {
//
//		StringBuilder whoisResult = new StringBuilder("");
//
//		WhoisClient crunchifyWhois = new WhoisClient();
//		try {
//			// The WhoisClient class implements the client side of the Internet
//			// Whois Protocol defined in RFC 954. To query a host you create a
//			// WhoisClient instance, connect to the host, query the host, and
//			// finally disconnect from the host. If the whois service you want
//			// to query is on a non-standard port, connect to the host at that
//			// port.
//			crunchifyWhois.connect(WhoisClient.DEFAULT_HOST);
//			String whoisData = crunchifyWhois.query("=" + domainName);
//			whoisResult.append(whoisData);
//			crunchifyWhois.disconnect();
//
//		} catch (SocketException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return whoisResult.toString();
//	}
//}
//
//////Demonstrate Sockets.
////import java.net.*;
////import java.io.*;
////class Yesto {
////	public static void main(String args[]) throws Exception {
////		int c;
////		Socket s = new Socket("whois.internic.net", 43);
////		System.out.println("socket connessa");
////		InputStream in = s.getInputStream();
////		OutputStream out = s.getOutputStream();
////		String str = (args.length == 0 ? "starwave-dom" : args[0]) +
////				"\\n";
////		byte buf[] = str.getBytes();
////		out.write(buf);
////		while ((c = in.read()) != -1) {
////			System.out.print((char) c);
////		}
////		s.close();
////	}
////}
//
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;

public class Yesto {

  public final static int DEFAULT_PORT = 43;

  public final static String DEFAULT_HOST = "whois.internic.net";

  public static void main(String[] args) throws Exception {

    String serverName = System.getProperty("WHOIS_SERVER", DEFAULT_HOST);

    //InetAddress server = InetAddress.getByName("google.com");
    //InetAddress server = null;
    InetAddress server = InetAddress.getByName(serverName);
    @SuppressWarnings("resource")
	Socket theSocket = new Socket(server, DEFAULT_PORT);
    Writer out = new OutputStreamWriter(theSocket.getOutputStream(), "8859_1");
    out.write("\r\n");
    out.flush();
    @SuppressWarnings("unused")
	InputStream raw = theSocket.getInputStream();
    InputStream in = new BufferedInputStream(theSocket.getInputStream());
    int c;
    while ((c = in.read()) != -1)
      System.out.write(c);
  }
}