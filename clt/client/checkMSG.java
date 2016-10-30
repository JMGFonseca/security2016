package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class checkMSG implements PropertyChangeListener {

    private static JTextArea chat;
    private getMSG runnable;
    
    public static class getMSG implements Runnable {

        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        private connection c;
        private String command;
        private JButton jb;
        private Clients src;
        private Clients dst;
        private JFrame frame;
        
        public volatile static boolean flag = true;
        public volatile static boolean sent = false;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        getMSG(connection c, JButton jb, Clients src, Clients dst, JFrame frame){
        	this.c = c;
        	this.jb = jb;
        	this.src = src;
        	this.dst = dst;
        	this.frame = frame;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        private JsonObject getResponse(InputStream i){
    		try{
    			JsonReader in = new JsonReader( new InputStreamReader ( i, "UTF-8") );
    			JsonElement data = new JsonParser().parse( in );
    			
    			return data.getAsJsonObject();
    		}catch(Exception e){
    			System.err.println("Error in: " + this.getClass().getName() + " line " + 
    					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
    		}
    		return null;
    	}

        @Override
        public void run() {
        	JsonObject j = new JsonObject();
            while (flag) {
                try {
                	j = getResponse(c.i);
                	if(j == null){
                		System.err.println("Error in: " + this.getClass().getName() + " line " + 
								Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: null return");
                	}
                	JsonObject payload = j.getAsJsonObject( "payload" );
                	JsonElement innerCmd = (payload == null) ? null : payload.get( "type" );
                	System.out.println("Received: " + innerCmd.getAsString().toString());
                	
                	if (innerCmd == null) {
       			     
                	}
                	else if (innerCmd.getAsString().equals( "ack" )) {
                		JsonElement id = payload.get( "dst" );
                		
    	   			    if (id == null) {
    	   			    	 
    	   			    }
    	   			    else if(id.getAsString().toString().equals(src.id)){
    	   			    	Date date = new Date();
    	   	                setCommand(dateFormat.format(date) + " -/ " + payload);
    	   			    }
                	}
                	else if (innerCmd.getAsString().equals( "client-com" )) {
                		JsonElement id = payload.get( "dst" );
    	   			    if (id == null) {
    	   			    	 
    	   			    }
    	   			    else if(id.getAsString().toString().equals(src.id)){
    	   			    	Date date = new Date();
    	   			    	sendACK(payload.get("data").getAsJsonObject().get("text").getAsString().toString());
    	   	                setCommand(dateFormat.format(date) + " -/ " + payload);
    	   			    }
                	}
                	else if (innerCmd.getAsString().equals( "client-disconnect" )) {
                		JsonElement id = payload.get( "dst" );
                		
    	   			    if (id == null) {
    	   			    	 
    	   			    }
    	   			    else if(id.getAsString().toString().equals(src.id)){
    	   			    	JOptionPane.showMessageDialog(frame,
                    			    "Other client disconnected.",
                    			    "Server Message",
                    			    JOptionPane.WARNING_MESSAGE);
                    		jb.doClick();
    	   			    }
                	}
                    Thread.sleep(1000);
                } catch (Exception e) {
                	System.err.println("Error in: " + this.getClass().getName() + " line " + 
							Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
                }
            }
        }
        
        public JsonObject messageServer(JsonObject jo){
    		try {
    			JsonObject j = new JsonObject();
    			
    			j.addProperty("type", "secure");
    			j.addProperty("sa-data", "mac");
    			j.add("payload", jo);
    			
    			return j;
    		} catch (Exception e) {
    			System.err.println("Error in: " + this.getClass().getName() + " line " + 
    					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
    		}
    		return null;
    	}
        
        private JsonObject accept(String text){
        	try {
    			JsonObject j = new JsonObject();
    			
    			j.addProperty("type", "ack");
    			j.addProperty("src", src.id);
    			j.addProperty("dst", dst.id);
    			
    			JsonObject temp = src.description.getAsJsonObject();
    			temp.addProperty("text", text);
    			j.add("data", temp);
    			
    			return j;
    		} catch (Exception e) {
    			System.err.println("Error in: " + this.getClass().getName() + " line " + 
    					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
    		}
        	return null;
        }
        
        private void sendACK(String text){
        	JsonObject send = messageServer(accept(text));
        	String msg = send.toString() + "\n";
			try {
				c.o.write (msg.getBytes(StandardCharsets.UTF_8));
			} catch (Exception e1) {
				System.err.println("Error in: " + this.getClass().getName() + " line " + 
						Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e1);
			}
        }

        public String getCommand() {
            return command;
        }

        private void setCommand(String command) {
            String old = this.command;
            this.command = command;
            pcs.firePropertyChange("command", old, command);
        }
    }

    protected void initUI(getMSG runnable) {
        this.runnable = runnable;
        runnable.addPropertyChangeListener(this);
    }
    
    public static void stop(){
    	getMSG.flag = false;
    }
    
    public static void start(){
    	getMSG.flag = true;
    }

    private void executeCommand() {
    	String temp = runnable.getCommand().split(" -/ ")[0];
    	String name = null;
    	JsonObject payload = new JsonParser().parse(runnable.getCommand().split(" -/ ")[1]).getAsJsonObject();
    	if(payload == null)
    		return ;
    	if(payload.get("type").getAsString().toString().equals("client-com"))
    		name = payload.get("src").getAsString().toString();
    	else if(payload.get("type").getAsString().toString().equals("ack")){
    		name = payload.get("dst").getAsString().toString();
    	}
    	JsonObject data = payload.get( "data" ).getAsJsonObject();
        chat.append(name + " [" + temp + "] : " + data.get("text").getAsString().toString() + "\n");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("command")) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    executeCommand();
                }
            });
        }
    }
    

    public static void main(JTextArea jt, connection c, Clients src, Clients dst, JButton jb, JFrame frame) {
    	chat = jt;
        final getMSG runnable = new getMSG(c, jb, src, dst, frame);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkMSG testThreadingAndGUI = new checkMSG();
                testThreadingAndGUI.initUI(runnable);
            }
        });
        new Thread(runnable).start();
    }

}
