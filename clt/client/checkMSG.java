package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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
    	
    	private InputStream in;

        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        private String command;
        private JButton jb;
        
        public volatile static boolean flag = true;
        public volatile static boolean sent = false;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        getMSG(InputStream in, JButton jb){
        	this.in = in;
        	this.jb = jb;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void run() {
        	JsonObject j = new JsonObject();
            while (flag) {
                try {
                	//j = getResponse(in);
                	//if(j == null){
                		//System.err.println("Error receiving from server.");
                	//}
                	//else{
                	
                		//sendACK();
                	//}
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                }
                Date date = new Date();
                setCommand(dateFormat.format(date) + "");
            }
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
            // Received new command (outside EDT)
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Updating GUI inside EDT
                    executeCommand();
                }
            });
        }
    }
    
    public static JsonObject getResponse(InputStream i){
		try{
			JsonReader in = new JsonReader( new InputStreamReader ( i, "UTF-8") );
			JsonElement data = new JsonParser().parse( in );
			
			return data.getAsJsonObject();
		}catch(Exception e){
			System.err.print( "Cannot get server response: " + e );
		}
		return null;
	}

    public static void main(String[] args, JTextArea jt, InputStream in, JButton jb) {
    	chat = jt;
        final getMSG runnable = new getMSG(in, jb);
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
