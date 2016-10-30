package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.awt.Color;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

public class chat extends JFrame{

	private JPanel contentPane;
	private JFrame frame = this;
	
	private static connection c;
	private static Clients src;
	private static Clients dst;
	private JTextField textField;
	
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
	
	private JsonObject send(String text){
		try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "client-com");
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
	
	private JsonObject decline(){
    	try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "client-disconnect");
			j.addProperty("src", src.id);
			j.addProperty("dst", dst.id);
			
			j.add("data", src.description.getAsJsonObject());
			
			return j;
		} catch (Exception e) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
		}
    	return null;
    }
	
	/**
	 * Create the frame.
	 */
	public chat(connection c, Clients src, Clients dst) {
		chat.c = c;
		chat.src = src;
		chat.dst = dst;
		
		setTitle("SCIM : " + src.id);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 102, 153));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		final JPanel panel = new JPanel();
		
		panel.setBackground(new Color(0, 102, 153));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
		);
		
		textField = new JTextField();
		textField.setColumns(10);
		
		final JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		
		JScrollPane sp = new JScrollPane(textArea);
		
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JsonObject send = messageServer(send(textField.getText()));
	        	String msg = send.toString() + "\n";
				try {
					chat.c.o.write (msg.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e1) {
					System.err.println("Error in: " + this.getClass().getName() + " line " + 
							Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e1);
				}
				textField.setText("");
			}
		});
		
		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JsonObject send = messageServer(decline());
	        	String msg = send.toString() + "\n";
				try {
					chat.c.o.write (msg.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e1) {
					System.err.println("Error in: " + this.getClass().getName() + " line " + 
							Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e1);
				}
				checkMSG.stop();
				frame.dispose();
				mainMenu mn = new mainMenu(chat.c, chat.src);
				mn.setVisible(true);
			}
		});
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap(168, Short.MAX_VALUE)
					.addComponent(disconnectButton)
					.addGap(157))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(40)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(sp, GroupLayout.PREFERRED_SIZE, 366, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 261, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(sendButton)))
					.addContainerGap(34, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(disconnectButton)
					.addGap(18)
					.addComponent(sp, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(sendButton))
					.addContainerGap(11, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		contentPane.setLayout(gl_contentPane);
		
		try {
			checkMSG.start();
			checkMSG.main(textArea, chat.c, src, dst, disconnectButton, this);
		} catch (Exception e1) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e1);
		}
	}
}
