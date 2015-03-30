package cn.edu.sdju.chat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer extends JFrame {
	
	private JTextArea displayJTextArea;
	
	private ServerSocket serverSocket;
	
	private Socket socket;
	
	public JMenuBar jmb;
	public JMenu setJMenu;
	public JMenu setPortJMenuItem;
	
	static int port;        //要监听的端口
	
	private Map<String,Server_Thread> threadList = new HashMap<String,Server_Thread>();        //线程列表
	
	public ChatServer() {
		this.setTitle("服务器");
		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		displayJTextArea = new JTextArea();
		displayJTextArea.setEditable(false);
		JScrollPane jsp = new JScrollPane(displayJTextArea);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		container.add(jsp, BorderLayout.CENTER);
//		jmb = new JMenuBar();
//		JMenu setJMenu = new JMenu("设置");
//		JMenuItem setPortJMenuItem = new JMenuItem("端口设置");
//		setPortJMenuItem.addActionListener(new Monitor());
//		setJMenu.add(setPortJMenuItem);
//		jmb.add(setJMenu);
//		this.setJMenuBar(jmb);
		this.setBounds(400, 150, 500, 350);
		this.setVisible(true);
		
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
	
	public void connect() {
		try {
			serverSocket = new ServerSocket(port);          //记录服务器端的一些信息
			displayJTextArea.append("本机名:" + InetAddress.getLocalHost().getHostName() + "\n");
			displayJTextArea.append("本机地址:" + InetAddress.getLocalHost().getHostAddress() + "\n");
			displayJTextArea.append("本机监听端口:" + port + "\n");
		} catch (BindException e) {
			JOptionPane.showMessageDialog(null, "此端口已被占用，无法监听！", "提示", JOptionPane.WARNING_MESSAGE);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
		}
		
		while (true) {
			try {
				socket = serverSocket.accept();               //等待客户端的连接
				Date now = new Date();
				SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd E kk:mm:ss");
				displayJTextArea.append("\n" + f.format(now) + "\n");
				displayJTextArea.append(socket.getInetAddress().getHostAddress()
						+ " is connected" + "\n");
				Server_Thread st = new Server_Thread(socket, threadList, displayJTextArea);       //客户连接上，建立对应的线程
				st.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String [] args) {
		ChatServer cs = new ChatServer();
		String str = JOptionPane.showInputDialog(null, "请输入你要监听的端口，端口应为（1024-65535）的整数", "端口设置", JOptionPane.PLAIN_MESSAGE);
		try {
			port = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "端口设置不成功", "提示", JOptionPane.INFORMATION_MESSAGE);
		}
		cs.connect();
	}
	
//	class Monitor implements ActionListener {
//		
//		public void actionPerformed(ActionEvent event) {
//			String str = JOptionPane.showInputDialog(null, "请输入你要监听的端口，端口应为（1024-65535）的整数", "端口设置", JOptionPane.PLAIN_MESSAGE);
//			try {
//				port = Integer.parseInt(str);
//			} catch (NumberFormatException e) {
//				JOptionPane.showMessageDialog(null, "端口设置不成功", "提示", JOptionPane.INFORMATION_MESSAGE);
//			}
//			connect();
//		}
//	}
	
}
