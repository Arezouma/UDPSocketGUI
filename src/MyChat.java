
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
 
/** UDP Socket with GUI. 
 * Author: Arezouma Solly N. 
 * GUI allows connection between different computer in the LAN to communicate
 * Each connected computer has a window in which all its message are appended
 * There is a table that keeps track of all created windows. 
 * */
public class MyChat extends JFrame implements ActionListener {
    
	/**
     * Create the GUI for the connection chat. the window has two text fields: IP and Port,
     *  a chat button to start communication, an exit button to close it and a clear button to reset the fields.
     *  The constructor takes a socket as parameter 
     *   
     
     */
	private static Socket mySocket; // Create socket object 
	private JFrame frame;
	private JTextField ipTextField; // The ip number place holder
	private JTextField portTextField; // The port number place holder
    private final JLabel ipLabel; 
	private final JLabel portLabel;
	// the start, exit and clear buttons
	private final JButton chatButton; 
	private final JButton exitButton;
	private final JButton clearButton;
	private InetAddress address;
	private int port;
	//Initialize hashmap to store connection window
	private static HashMap<String, WinFrame> winTable = new HashMap<>();
	
	
	public MyChat(Socket socket) {
        //Create and set up the window.
        super("MyChat");
		mySocket = socket;
		setLayout(new FlowLayout());
		// construct  ip TextField
		ipTextField = new JTextField(20); 
		ipLabel = new JLabel("IP Number        : "); // label for ip
		ipLabel.setHorizontalAlignment(JLabel.LEFT);
		add(ipLabel);
		add(ipTextField); // add ip TextField to JFrame
		
		portTextField = new JTextField(20); 
		portLabel = new JLabel(" PORT Number: "); // label for port
		portLabel.setHorizontalAlignment(JLabel.LEFT);
		add(portLabel);
		add(portTextField);
		
		// create new Buttons and ButtonHandler for button event handling 
		
		chatButton = new JButton("Chat");
		exitButton = new JButton("Exit");
		clearButton = new JButton("Clear");
		
		chatButton.addActionListener(this);
		exitButton.addActionListener(this);
		clearButton.addActionListener(this);
		
		add(clearButton);
		add(exitButton);
	    add(chatButton);
		
		
		
		
	}
	 
	// handle button event
	@Override
	public void actionPerformed(ActionEvent event)
    {   
		// Get inputs from JTextField
		String ipAddress = ipTextField.getText();
    	String strPort = portTextField.getText();
    	String key = ipAddress + ":" + strPort;
    	
	 	if (event.getActionCommand()=="Chat")
	 	{	
	 	// Check if the entered ip address and port exist in the table 
	 		if (!ipAddress.isEmpty() && !strPort.isEmpty() && !winTable.containsKey(key))  
	 		{	 
	 			
	 			WinFrame chat = null;
	 			try {
	 				chat = new WinFrame(mySocket, ipAddress, Integer.parseInt(strPort));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
		 	
	            chat.win();
		        winTable.put(key, chat);
			   		 
			 }	
	 	 }
	 	 if(event.getActionCommand()=="Exit") {
	 		 
	 		   exitButton.addActionListener(new CloseListener());
		}

	 	  
	 	if (event.getActionCommand() == "Clear") 
	 	{
	 		ipTextField.setText(" ");
	 	    portTextField.setText(" ");
	 	}
	 	  
		
	}
	// Getters
	
	// get ip from ipTextfield as a String 
	public String getIp(){
		return ipTextField.getText();
	}
		
	// get port number from portTextfield as a String
	public String getPort(){
		return portTextField.getText();
	}
	
	public static Socket getSocket() {
		return mySocket;
	}
	
	public static void recievePackets() throws UnknownHostException {
		DatagramPacket inPacket = null;
		
		do {
			try {
				inPacket = mySocket.receive();
				// Get message, ip, and port to set hash key	
					if (inPacket != null) {
						String message = new String(inPacket.getData());
						String address = inPacket.getAddress().getHostAddress();
						int port = inPacket.getPort();
						String key = address + ":" + port;
					
						WinFrame chat = null;
					// Check if key in wintable, then create WinFrame and append message
						if (winTable.containsKey(key)) {
							chat = winTable.get(key);
							chat.getTextarea().append(address + ":" + message + "\n");
							chat.win();
							
							System.out.println("first win is created");
						// Otherwise create WinFrame and add to the wintable
					    } else {
						    chat = new WinFrame(mySocket, address, port);
						    winTable.put(key, chat);
						    chat.getTextarea().append(address + ":" + " " + message + "\n");
						    chat.win();
						
						    System.out.println("second win is created");
					    }
					}
			   } catch (NullPointerException ne) {
						// Nothing to do
			   }
			} while (true); 
		} 
    
    
	public void mainWin()
	{ 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(350, 100);
		setVisible(true);
	
	}
	
	// Inner class to close window when exit button clicked
	private class CloseListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent event) {
	    	if (event.getSource() == exitButton) {
	    		
	    		MyChat.getSocket().close();
				 System.exit(0);
	    	}
	              
	    }
	}
	
}
