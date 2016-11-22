package GChat;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;



import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;


public class GChat extends JFrame implements ActionListener, Runnable, KeyListener{
    
	private static final long serialVersionUID = 7943495196364601512L;
	JLabel lCon;
    JEditorPane tpane = new JEditorPane("text/html","");
    JTextPane textPane = new JTextPane();
    JTextArea txt;
    JTextField tfd;
    JButton b1;
    DataOutputStream os;
    InputStreamReader is;
    BufferedReader br;
    Socket socket;
    Thread threadRead;
    Thread threadCount;
    static String username;
    JScrollPane scroll;
    public static void main(String[] args) 
    {
        GChat fr = new GChat();
        fr.setSize(450,405);
        fr.setTitle("gChat");
        fr.setDefaultCloseOperation(EXIT_ON_CLOSE);
        fr.setLocationRelativeTo(null);
        fr.setResizable(false);
        fr.setVisible(true);
    }
    
    public GChat()
    {
    	try 
    	{
    	    UIManager.setLookAndFeel(
    	        UIManager.getSystemLookAndFeelClassName());
    	} 
    	catch (Exception ex) 
    	{
    	  System.out.println("Unable to load native look and feel");
    	}
    	
    	do
        {
        	username = (String) JOptionPane.showInputDialog(this, "Username", "Sign In", 3, null, null, null);
        }
        while(username.isEmpty());
        lCon = new JLabel("Connected People: ");
        //txt = new JTextArea(20,38);
        textPane.setEditable(false);
        tfd = new JTextField(31);
        b1 = new JButton("Send");
        //textPane.setPreferredSize(new Dimension(420,314));
        textPane.setFocusable(false);
        scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(420,314));
        Container pane = getContentPane();
        FlowLayout fl = new FlowLayout();
        pane.setLayout(fl);
        pane.add(lCon);
        pane.add(scroll);
        pane.add(tfd);
        pane.add(b1);
        b1.addActionListener(this);
        tfd.addKeyListener(this);
        DefaultCaret caret = (DefaultCaret)textPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        try
        {
            socket = new Socket("localhost", 5199);
            os = new DataOutputStream(socket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            URL url = GChat.class.getResource("icon.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            this.setIconImage(img);
        }
        catch(IOException ex)
        {
        	JOptionPane.showMessageDialog(this, ex.getMessage());
        	System.exit(0);
        }
        
        threadRead = new Thread(this);
        threadRead.setName("read");
        threadRead.start();
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == b1)
        {
        	if(tfd.getText().isEmpty())
				return;
        	try
        	{
        		b1.setEnabled(false);
        		os.writeBytes(username + ": " + tfd.getText() + "\n");
        		os.flush();
        		b1.setEnabled(true);
        		
                StyledDocument doc = textPane.getStyledDocument();
                Style style = textPane.addStyle("Bold Style", null);
                StyleConstants.setBold(style, true);
                try 
                {
					doc.insertString(doc.getLength(), username + ": ", style);
					StyleConstants.setBold(style, false);
					doc.insertString(doc.getLength(), tfd.getText() + "\n", style);
					tfd.setText("");
                } 
                catch (BadLocationException e1) 
                {
                	JOptionPane.showMessageDialog(this, e1.getMessage());
            		System.exit(0);
				} 
                
        	}
        	catch(IOException ex)
        	{
        		JOptionPane.showMessageDialog(this, ex.getMessage());
        		System.exit(0);
        	}
        	
        }
        
    }
    
    

	@Override
	public void run() {
	   try
	   {
	  	StyledDocument doc = textPane.getStyledDocument();
                Style style = textPane.addStyle("Bold Style", null);
            
            
            try 
            {
            	while (true) 
            	{
            		boolean flag = false;
            		String str1 = br.readLine();
            		char[] array = str1.toCharArray();
            		
            		for(int i=0; i < array.length; i++)
       	         		if(!Character.isDigit(array[i]))
       	         		{
       	         			flag = true;
       	         			break;
       	         		}
            		
            		if(!flag)
            		{
            			lCon.setText("Connected People: " + str1);
            		}
            		else
            		{
            			String str2;
            			String str3;
            			int len = str1.indexOf(':');
            		
            			StyleConstants.setBold(style, true);
            		
            			str2 = str1.substring(0,len + 1);
            			str3 = str1.substring(len + 1);
            			doc.insertString(doc.getLength(), str2, style);
            			
            			StyleConstants.setBold(style, false);
            			doc.insertString(doc.getLength(),  str3 + "\n", style);
            		
            		}
            	}	                
            } 
            catch (BadLocationException e1) 
            {
            	JOptionPane.showMessageDialog(this, e1.getMessage());
        		System.exit(0);
			}
            
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage());
			System.exit(0);
		}
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyChar() == '\n')
		{
			if(tfd.getText().isEmpty())
				return;
			try
        	{
        		b1.setEnabled(false);
        		os.writeBytes(username + ": " + tfd.getText() + "\n");
        		os.flush();
        		b1.setEnabled(true);
        		
        		StyledDocument doc = textPane.getStyledDocument();
                Style style = textPane.addStyle("Bold Style", null);
                StyleConstants.setBold(style, true);
                try 
                {
					doc.insertString(doc.getLength(), username + ": ", style);
					StyleConstants.setBold(style, false);
					doc.insertString(doc.getLength(), tfd.getText() + "\n", style);
					tfd.setText("");
                } 
                catch (BadLocationException e1) 
                {
                	JOptionPane.showMessageDialog(this, e1.getMessage());
            		System.exit(0);
				} 
        	}
        	catch(IOException ex)
        	{
        		JOptionPane.showMessageDialog(this, ex.getMessage());
        		System.exit(0);
        	}
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
		
	}
    
}
