package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JList;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

public class mainMenu extends JFrame {

	private JPanel contentPane;
	private static Socket c;
	private static OutputStream o;
	private static InputStream i;
	private static String MAC;
	private static String name;
	
	private static int port = 1025;
	
	public JsonObject secureConnection(){
		try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "secure");
			j.addProperty("sa-data", "mac");
			
			JsonObject temp = new JsonObject();
			temp.addProperty("type", "list");
			j.add("payload", temp);
			
			return j;
		} catch (Exception e) {
			System.err.print( "Cannot create secure connection: " + e );
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
	
	public JsonObject connectToClient(){
		
		return null;
	}
	
	private void close(){
		try {
			i.close();
			o.close();
			c.close();
		} catch (Exception e) {
			System.err.print("Error closing socket: " + e);
		}
	}

	public mainMenu(Socket c, OutputStream o, InputStream i, String MAC, String name, int port) {
		mainMenu.c = c;
		mainMenu.o = o;
		mainMenu.i = i;
		mainMenu.MAC = MAC;
		mainMenu.name = name;
		mainMenu.port = port;
		
		setTitle("SCIM");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 102, 153));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		DefaultListModel<Clients> model = new DefaultListModel<Clients>();
		try{
			JsonObject j = new JsonObject();
			
			j = secureConnection();
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

			JsonArray jsonA = (j.get("payload")).getAsJsonObject().get("data").getAsJsonArray();
			if(jsonA.size() > 0){
				for(int temp = 0; temp < jsonA.size(); temp++){
					JsonObject jotemp = jsonA.get(temp).getAsJsonObject();
					String temp3 = jotemp.get("data").getAsJsonObject().get("Mac").toString().replaceAll("\"", "");
					if(!temp3.equals(MAC)){
						Clients client = new Clients(jotemp.get("id").toString(), jotemp);
						model.addElement(client);
					}
				}
			}
			System.out.println(j.toString());
		}catch(Exception e){
			System.err.println("Error in list request: " + e);
		}
		
		JList<Clients> list = new JList<Clients>(model);
		
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		JLabel lblNewLabel = new JLabel("Users Online");
		lblNewLabel.setForeground(new Color(255, 255, 255));
		
		JButton settingsButton = new JButton("Settings");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(17)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(93)
							.addComponent(lblNewLabel))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(list, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
							.addGap(28)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(settingsButton)
								.addComponent(connectButton))))
					.addContainerGap(17, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(19)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(connectButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(settingsButton))
						.addComponent(list, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(20, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
}
