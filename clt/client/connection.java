package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class connection {
	
	int port;
	Socket s;
	InputStream i;
	OutputStream o;
	
	connection(Socket s){
		this.s = s;
		this.port = s.getPort();
		try {
			i = s.getInputStream();
			o = s.getOutputStream();
		} catch (Exception e) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
		}
		
	}
	
	public void close(){
		try {
			i.close();
			o.close();
			s.close();
		} catch (IOException e) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
		}
	}
}
