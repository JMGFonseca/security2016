package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JList;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;

public class mainMenu extends JFrame {

	private JFrame frame = this;
	private JPanel contentPane;
	private static connection c;
	private static Clients client;
	private static boolean flag = false;
	
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
	
	
	public JsonObject connectToClient(String id){
		try {
			JsonObject j = new JsonObject();
			
			j.addProperty("type", "client-connect");
			j.addProperty("src", client.id);
			j.addProperty("dst", id);
			j.addProperty("phase", 1);
			j.addProperty("ciphers", "DES");
			
			JsonObject temp = client.description.getAsJsonObject();
			temp.addProperty("name", client.name);
			j.add("data", temp);
			
			return j;
		} catch (Exception e) {
			System.err.println("Error in: " + this.getClass().getName() + " line " + 
					Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e);
		}
		return null;
	}

	public mainMenu(connection c, Clients client) {
		mainMenu.c = c;
		mainMenu.client = client;
		
		setTitle("SCIM : " + client.id);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 102, 153));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		DefaultListModel<Clients> model = new DefaultListModel<Clients>();;
		
		final JList<Clients> list = new JList<Clients>(model);
		
		itemListener l = new itemListener(client);
		final JRadioButton btnDES = new JRadioButton("DES");
		btnDES.addItemListener(l);
		final JRadioButton btnAES = new JRadioButton("AES");
		btnAES.addItemListener(l);
		btnDES.setVisible(flag);
		btnAES.setVisible(flag);
		if(client.ciphers.equals("DES")){
			btnDES.setSelected(true);
			btnAES.setSelected(false);
		}
		else if (client.ciphers.equals("AES")){
			btnDES.setSelected(false);
			btnAES.setSelected(true);
		}
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(btnDES);
		bg.add(btnAES);
		
		final JButton ciphersButton = new JButton("Ciphers");
		
		ciphersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				flag = !flag;
				btnDES.setVisible(flag);
				btnAES.setVisible(flag);
			}
		});

		final JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					if(list.getSelectedValue() == null){
						return ;
					}
					JsonObject j = new JsonObject();
					connectButton.setEnabled(false);
					ciphersButton.setEnabled(false);
					checkIncoming.stop();
					j = messageServer(connectToClient(list.getSelectedValue().id));
					if(j == null){
						System.err.println("Error in: " + this.getClass().getName() + " line " + 
								Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: null return");
					}
					
					String msg = j.toString() + "\n";
					mainMenu.c.o.write (msg.getBytes(StandardCharsets.UTF_8));
				}catch(Exception e1){
					System.err.println("Error in: " + this.getClass().getName() + " line " + 
							Thread.currentThread().getStackTrace()[1].getLineNumber() + "\nError: " + e1);
				}
				
			}
		});
		
		JLabel lblNewLabel = new JLabel("Users Online");
		lblNewLabel.setForeground(new Color(255, 255, 255));
		
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
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(28)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(ciphersButton)
										.addComponent(connectButton)))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(btnAES)
										.addComponent(btnDES))))))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
							.addComponent(ciphersButton)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnDES)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnAES))
						.addComponent(list, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(20, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);

		checkIncoming.main(frame, model, c, client);
		checkIncoming.start();
	}
}

class itemListener implements ItemListener{
	
	private Clients client;
	
	itemListener(Clients client){
		this.client = client;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED){
	    	String str = e.getItemSelectable().toString().split("text=")[1];
	        client.ciphers = str.substring(0,str.length()-1);
	    }
	    else if (e.getStateChange() == ItemEvent.DESELECTED) {
	    	
	    }
	}
}
