package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import javax.swing.JPanel;

public class ClientGUI {

	private JFrame frmScim;
	private JTextField usernameField;
	private static Socket c;
	private static OutputStream o;
	private static InputStream i;
	private static String MAC;
	private static String name;

	/**
	 * Launch the application.
	 */
	private static int port = 1025;
	
	public JsonObject connectToServer(){
		try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "connect");
			j.addProperty("phase", 1);
			j.addProperty("name", name);
			j.addProperty("id", 123);
			j.addProperty("ciphers", "DES");
			
			JsonObject temp = new JsonObject();
			temp.addProperty("Mac", MAC);
			j.add("data", temp);
			
			return j;
		} catch (Exception e) {
			System.err.print( "Cannot create a connection: " + e );
		}
		return null;
	}
	
	public JsonObject getResponse(InputStream i){
		try{
			JsonReader in = new JsonReader( new InputStreamReader ( i, "UTF-8") );
			JsonElement data = new JsonParser().parse( in );
			
			return data.getAsJsonObject();
		}catch(Exception e){
			System.err.print( "Cannot get server response: " + e );
		}
		return null;
	}
	
	
	public String getMyMAC(){
		try{
			InetAddress ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			
			byte[] mac = network.getHardwareAddress();
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			
			return sb.toString();
		}catch(Exception e){
			System.err.print( "Cannot get this machine MAC Address: " + e );
		}
		return null;
	}
	
	private void close(){
		try {
			i.close();
			o.close();
			c.close();
		} catch (IOException e) {
			System.err.print("Error closing socket: " + e);
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
					window.frmScim.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmScim = new JFrame();
		frmScim.setTitle("SCIM");
		frmScim.getContentPane().setBackground(new Color(0, 102, 153));
		frmScim.setBounds(100, 100, 450, 300);
		frmScim.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		final JPanel panelLogin = new JPanel();
		panelLogin.setBackground(new Color(0, 102, 153));
		GroupLayout groupLayout = new GroupLayout(frmScim.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(panelLogin, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(panelLogin, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
		);
		usernameField = new JTextField();
		usernameField.setColumns(10);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setForeground(new Color(255, 255, 255));
		
		JLabel lblSecureInstantMessaging = new JLabel("Secure Instant Messaging System");
		lblSecureInstantMessaging.setForeground(new Color(255, 255, 255));
		
		JButton btnLogin = new JButton("Login");
		GroupLayout gl_panelLogin = new GroupLayout(panelLogin);
		gl_panelLogin.setHorizontalGroup(
			gl_panelLogin.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panelLogin.createSequentialGroup()
					.addContainerGap(212, Short.MAX_VALUE)
					.addComponent(lblSecureInstantMessaging)
					.addGap(116))
				.addGroup(Alignment.TRAILING, gl_panelLogin.createSequentialGroup()
					.addContainerGap(249, Short.MAX_VALUE)
					.addGroup(gl_panelLogin.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panelLogin.createSequentialGroup()
							.addComponent(btnLogin)
							.addGap(181))
						.addGroup(gl_panelLogin.createSequentialGroup()
							.addComponent(usernameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(154))
						.addGroup(gl_panelLogin.createSequentialGroup()
							.addComponent(lblUsername)
							.addGap(192))))
		);
		gl_panelLogin.setVerticalGroup(
			gl_panelLogin.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLogin.createSequentialGroup()
					.addGap(51)
					.addComponent(lblSecureInstantMessaging)
					.addGap(29)
					.addComponent(lblUsername)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(usernameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLogin)
					.addGap(57))
		);
		panelLogin.setLayout(gl_panelLogin);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(usernameField.getText().equals(""))
						return;
					c = new Socket("localhost", port);
					MAC = getMyMAC();
					o = c.getOutputStream();
					i = c.getInputStream();
					name = usernameField.getText();
					
					JsonObject j = new JsonObject();
					j = connectToServer();
					if(j == null){
						close();
						System.exit(0);
					}
					String msg = j.toString() + "\n";
					o.write (msg.getBytes(StandardCharsets.UTF_8));
					
					j = getResponse(i);
					if(j == null){
						close();
						System.exit(0);
					}
					msg = j.get("data").toString();
					msg = msg.replaceAll("\"", "");
					if(!msg.equals("ok")){
						System.err.println("Error in data sent from client.");
						close();
						System.exit(0);
					}
					
					frmScim.dispose();
					mainMenu mn = new mainMenu(c, o, i, MAC, name, port);
					mn.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		frmScim.getContentPane().setLayout(groupLayout);
	}
}
