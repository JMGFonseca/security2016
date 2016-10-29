package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
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
        private int port;
        private String command;
        private JButton jb;
        
        public volatile static boolean flag = true;
        public volatile static boolean sent = false;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        getMSG(int port, JButton jb){
        	this.port = port;
        	this.jb = jb;
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
        	try {
				c = new connection(new Socket("localhost", port));
			} catch (Exception e1) {
				System.err.println("Error in: " + this.getClass().getName() + " line " + 
						Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e1);
			}
        	JsonObject j = new JsonObject();
            while (flag) {
                try {
                	j = getResponse(c.i);
                	if(j == null){
                		System.err.println("Error in: " + this.getClass().getName() + " line " + 
								Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: null return");
                	}
                	else{
                	
                		sendACK();
                	}
                    Thread.sleep(1000);
                } catch (Exception e) {
                	System.err.println("Error in: " + this.getClass().getName() + " line " + 
							Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
                }
                Date date = new Date();
                setCommand(dateFormat.format(date) + "");
            }
            c.close();
        }
        
        private void sendACK(){
        	
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
        chat.append(runnable.getCommand() + "\n");
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
    

    public static void main(JTextArea jt, int port, Clients src, Clients dst, JButton jb) {
    	chat = jt;
        final getMSG runnable = new getMSG(port, jb);
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
