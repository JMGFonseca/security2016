package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class checkIncoming implements PropertyChangeListener {
	
	private getInc runnable;
	private static connection c;
	private static Clients src;
    private static JFrame j;
    
    public static class getInc extends Thread {

        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        private connection c;
        private String command;
        private Clients src;
        private JFrame frame;
        
        public static Thread one;
        
        private DefaultListModel<Clients> model;
        
        public volatile static boolean flag = true;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        getInc(connection c, Clients src, JFrame frame, DefaultListModel<Clients> model){
        	this.c = c;
        	this.src = src;
        	this.frame = frame;
        	this.model = model;
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
        	one = new Thread() {
        	    public void run() {
        	    	while(flag){
        	    		try {
                			JsonObject j1 = new JsonObject();
                			
                			j1 = messageServer(getClientList());
                			if(j1 == null){
                				System.err.println("Error in: " + this.getClass().getName() + " line " + 
                						Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: null return");
                			}
                			String msg = j1.toString() + "\n";
                			c.o.write (msg.getBytes(StandardCharsets.UTF_8));

	        	            Thread.sleep(5000);
	        	        } catch(Exception e) {
	        	        	System.err.println("Error in: " + this.getClass().getName() + " line " + 
	            					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
	        	        }
        	    	}
        	    }  
        	};
        	
        	one.start();
        	JsonObject j = new JsonObject();
            while (flag) {
            	j = getResponse(c.i);
            	if(j == null){
            		System.err.println("Error in: " + this.getClass().getName() + " line " + 
							Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: null return");
            	}
            	JsonObject payload = j.getAsJsonObject( "payload" );
            	JsonElement innerCmd = (payload == null) ? null : payload.get( "type" );
            	
            	if (innerCmd == null) {
   			     
            	}
            	else if (innerCmd.getAsString().equals( "list" )) {
            		DefaultListModel<Clients> temp1 = new DefaultListModel<Clients>();
            		JsonArray jsonA = (j.get("payload")).getAsJsonObject().get("data").getAsJsonArray();
        			if(jsonA.size() > 0){
        				for(int temp = 0; temp < jsonA.size(); temp++){
        					JsonObject jotemp = jsonA.get(temp).getAsJsonObject();
        					String temp3 = jotemp.get("id").getAsString().toString();
        					if(!temp3.equals(src.id)){
        						Clients clients = new Clients(jotemp.get("id").getAsString().toString(), 
        									jotemp.get("name").getAsString().toString(), 
        									jotemp.get("ciphers").getAsString().toString(), 
        									jotemp.get("data").getAsJsonObject());
        						temp1.addElement(clients);
        					}
        				}//Add Connected
        				for(int i = 0; i < temp1.size(); i++){
        					Clients tmp = temp1.get(i);
        					if(!model.contains(tmp)){
        						model.addElement(tmp);
        					}
        				}//Remove disconnected
        				for(int i = 0; i < model.size(); i++){
        					Clients tmp = model.get(i);
        					if(!temp1.contains(tmp)){
        						model.remove(i);
        					}
        				}
        			}
            	}
            	else if (innerCmd.getAsString().equals( "client-connect" )) {
            		JsonElement id = payload.get( "dst" );
            		
	   			    if (id == null) {
	   			    	 
	   			    }
	   			    else if(id.getAsString().toString().equals(src.id)){
	   			    	Date date = new Date();
	   	                setCommand(dateFormat.format(date) + " -/ " + payload);
	   			    }
            	}
            	else if (innerCmd.getAsString().equals( "client-disconnect" )) {
            		JsonElement id = payload.get( "dst" );
            		
	   			    if (id == null) {
	   			    	 
	   			    }
	   			    else if(id.getAsString().toString().equals(src.id)){
	   			    	one.stop();
	   			    	frame.dispose();
	            		mainMenu mn = new mainMenu(c, src);
	            		mn.setVisible(true);
	   			    }
            	}
            	else if(innerCmd.getAsString().equals( "ack" )){
            		JsonElement id = payload.get( "dst" );
            		
	   			    if (id == null) {
	   			    	 
	   			    }
	   			    else if(id.getAsString().toString().equals(src.id)){
	   			    	JsonObject data = payload.get( "data" ).getAsJsonObject();
	                	String name = data.get("name").getAsString().toString();
	                	checkIncoming.stop();
						frame.dispose();
						chat chat = new chat(c, src, new Clients(payload.get( "src" ).getAsString(), 
			        			name,
			        			src.ciphers,
			        			data));
						chat.setVisible(true);
	   			    }
            	}
                try {
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

        public String getCommand() {
            return command;
        }
        
    	public JsonObject getClientList(){
    		try {
    			JsonObject j = new JsonObject();
    			j.addProperty("type", "list");
    			
    			return j;
    		} catch (Exception e) {
    			System.err.println("Error in: " + this.getClass().getName() + " line " + 
    					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
    		}
    		return null;
    	}

        private void setCommand(String command) {
            String old = this.command;
            this.command = command;
            pcs.firePropertyChange("command", old, command);
        }
    }

    protected void initUI(getInc runnable) {
        this.runnable = runnable;
        runnable.addPropertyChangeListener(this);
    }
    
    public static void stop(){
    	getInc.one.stop();
    	getInc.flag = false;
    }
    
    public static void start(){
    	getInc.flag = true;
    }
    
    private JsonObject decline(JsonObject payload){
    	try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "client-disconnect");
			j.addProperty("src", payload.get("dst").getAsString());
			j.addProperty("dst", payload.get("src").getAsString());
			
			j.add("data", payload.get("data").getAsJsonObject());
			
			return j;
		} catch (Exception e) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
		}
    	return null;
    }
    
    private JsonObject accept(JsonObject payload){
    	try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "ack");
			j.addProperty("src", payload.get("dst").getAsString());
			j.addProperty("dst", payload.get("src").getAsString());
			
			j.add("data", payload.get("data").getAsJsonObject());
			
			return j;
		} catch (Exception e) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
		}
    	return null;
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

    private void executeCommand() {
    	String temp = runnable.getCommand().split(" -/ ")[0];
    	JsonObject payload = new JsonParser().parse(runnable.getCommand().split(" -/ ")[1]).getAsJsonObject();
    	if(payload == null)
    		return ;
    	JsonObject data = payload.get( "data" ).getAsJsonObject();
    	String name = data.get("name").getAsString().toString();
    	Object[] options = {"Yes",
                "No"};
		int n = JOptionPane.showOptionDialog(j,
				temp + " Incoming connection from " +
						payload.get( "dst" ).getAsString().toString(),
				"Server Message",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
        if(n == 0){
        	j.dispose();
        	stop();
        	JsonObject send = messageServer(accept(payload));
        	String msg = send.toString() + "\n";
			try {
				checkIncoming.c.o.write (msg.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				System.err.println("Error in: " + this.getClass().getName() + " line " + 
						Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
			}
        	chat chat = new chat(c, src, new Clients(payload.get( "src" ).getAsString(), 
        			name,
        			payload.get( "ciphers" ).getAsString(),
        			data));
			chat.setVisible(true);
        }else{
        	JsonObject send = messageServer(decline(payload));
        	String msg = send.toString() + "\n";
        	try {
				checkIncoming.c.o.write (msg.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				System.err.println("Error in: " + this.getClass().getName() + " line " + 
						Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
			}
        }
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
    
    public JsonObject getResponse(InputStream i){
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
    

    public static void main(JFrame j, DefaultListModel<Clients> list, connection c, Clients src) {
    	checkIncoming.src = src;
    	checkIncoming.c = c;
    	checkIncoming.j = j;
        final getInc runnable = new getInc(c, src, j, list);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkIncoming testThreadingAndGUI = new checkIncoming();
                testThreadingAndGUI.initUI(runnable);
            }
        });
        new Thread(runnable).start();
    }
}
