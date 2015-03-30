package cn.edu.sdju.chat;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

public class ChatClient extends JFrame {

	private JLabel clientIDLabel, sessionObjectLabel, currentClientLabel;
	public JTextArea displayTextArea;
	public List currentClientList;
	public JTextField clientNameTextField;
	private JTextField messageJTextField;
	public JButton loginButton, logoutJButton;
	public JButton sendJButton;
	
	JMenuItem setIPJMenuItem, setPortJMenuItem, aboutJMenuItem;

	private Socket socket;

	private BufferedReader reader;
	private PrintWriter writer;

	private StringBuffer toClientName = null;

	public String ip;
	public int port = -1;

	public ChatClient() {
		this.setTitle("客户端");
		Container container = this.getContentPane();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;          //组件随着所给区域可以进行扩展
		container.setLayout(layout);
		
		createJMenuBar();

		constraints.weightx = 8.0;
		constraints.weighty = 1.0;
		// constraints.gridheight = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		JPanel topPanel = new JPanel();
		clientIDLabel = new JLabel("客户名:", SwingConstants.CENTER);
		topPanel.add(clientIDLabel);
		clientNameTextField = new JTextField(20);
		topPanel.add(clientNameTextField);
		loginButton = new JButton("登陆");
		topPanel.add(loginButton);
		logoutJButton = new JButton("退出");
		logoutJButton.setEnabled(false);
		topPanel.add(logoutJButton);
		layout.setConstraints(topPanel, constraints);
		container.add(topPanel);

		constraints.weightx = 8.0;
		constraints.weighty = 1.0;
		// constraints.gridheight = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		sessionObjectLabel = new JLabel("会话对象:");
		layout.setConstraints(sessionObjectLabel, constraints);
		container.add(sessionObjectLabel);

		constraints.weightx = 7.0;
		constraints.weighty = 30.0;
		// constraints.gridheight = 5;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		displayTextArea = new JTextArea();
		displayTextArea.setEditable(false);
		JScrollPane displayScrollPane = new JScrollPane(displayTextArea);
		layout.setConstraints(displayScrollPane, constraints);
		container.add(displayScrollPane);

		constraints.weightx = 1.0;
		constraints.weighty = 30.0;
		// constraints.gridheight = 5;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		JPanel currentClientJPanel = new JPanel(new BorderLayout());
		currentClientLabel = new JLabel("当前在线客户:");
		currentClientJPanel.add(currentClientLabel, BorderLayout.NORTH);
		currentClientList = new List();
		currentClientJPanel.add(currentClientList, BorderLayout.CENTER);
		layout.setConstraints(currentClientJPanel, constraints);
		container.add(currentClientJPanel);

		currentClientList.setMultipleMode(true);        //设置列表是多选模式

		constraints.weightx = 7.0;
		constraints.weighty = 3.0;
		// constraints.gridheight = 1;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		messageJTextField = new JTextField();
//		JScrollPane messageScrollPane = new JScrollPane(messageTextField);
		layout.setConstraints(messageJTextField, constraints);
		container.add(messageJTextField);

		constraints.weightx = 1.0;
		constraints.weighty = 3.0;
		// constraints.gridheight = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		sendJButton = new JButton("发送");
		sendJButton.setEnabled(false);
		layout.setConstraints(sendJButton, constraints);
		container.add(sendJButton);

		this.setBounds(400, 100, 450, 500);
		this.setVisible(true);

		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
		});
		
		setMonitorForSetIPJMenuItem();             //为设置ip项设置时间监听
		setMonitorForSetPortJMenuItem();		//味设置端口项设置事件监听
		setMonitorForAboutJMenuItem();			//为帮助项设置事件监听
		setMonitorForLogoutJButton();			//为登出项设置事件监听
		setMonitorForCurrentClientList();		//为当前用户列表设置事件监听
		setMonitorForSentJButton();       //为发送按钮就行事件监听
		loginButton.addActionListener(new Monitor()); //为登陆按钮设置监听
		
		messageJTextField.addKeyListener(new KeyAdapter() {
			
			public void keyPressed(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});
		
	}
	
	public void createJMenuBar() {
		JMenuBar jmb = new JMenuBar();
		JMenu setJMenu = new JMenu("设置");
		setIPJMenuItem = new JMenuItem("设置IP");
		
		setPortJMenuItem = new JMenuItem("设置端口");
		setJMenu.add(setIPJMenuItem);
		setJMenu.add(setPortJMenuItem);
		jmb.add(setJMenu);
		JMenu helpJMenu = new JMenu("帮助");
		helpJMenu.setMnemonic('H');
		aboutJMenuItem = new JMenuItem("帮助");
		aboutJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.CTRL_MASK));
		helpJMenu.add(aboutJMenuItem);
		jmb.add(helpJMenu);
		this.setJMenuBar(jmb);
	}
	
	public void setMonitorForSetIPJMenuItem() {
		setIPJMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ip = JOptionPane.showInputDialog(null,
						"请输入你要连接的服务器IP(格式：XXX.XXX.XXX.XXX)", "设置IP",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
	
	public void setMonitorForSetPortJMenuItem() {
		setPortJMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				String str = JOptionPane.showInputDialog(null,
						"请输入你要连接服务器 的端口，端口应为（1024-65535）的整数", "端口设置",
						JOptionPane.PLAIN_MESSAGE);
				try {
					port = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "端口设置不成功", "提示",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
	
	public void setMonitorForAboutJMenuItem() {
		aboutJMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"1.首先你要设置服务器IP和端口，然后输入一个属于自己的的用户名，如果此用户名已被使用，你将需要重新输入一个用户名进行登录，\n"
								+ "2.登录你将看到一个在线客户列表（如果你不是第一个登录的用户）\n"
								+ "3.在当前的在线客户列表中选择你要发送消息的对象，可以一个或多个\n"
								+ "4.选择对象后，你将在会话标签栏中看到你当前进行会话的对象。\n"
								+ "5.登录成功后你可以选择退出按钮，使当前用户退出，重新登录\n"
								+ "6.关闭此窗口表示离开", "帮助",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
	
	public void setMonitorForLogoutJButton() {
		logoutJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				writer.println("logout:");
				currentClientList.removeAll(); // 当用户离开时，清空当前在线客户列表
				clientNameTextField.setText("");
				clientNameTextField.setEnabled(true);
				loginButton.setEnabled(true);
				logoutJButton.setEnabled(false);
				toClientName = null;
				sessionObjectLabel.setText("会话对象:");
			}
		});
	}
	
	public void setMonitorForCurrentClientList() {
		currentClientList.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				String[] Names = (String[]) currentClientList
						.getSelectedObjects();
				if (Names.length == 0) {
					toClientName = null;
					return;
				}
				toClientName = new StringBuffer();
				for (int i = 0; i < Names.length; i++) {
					toClientName.append("#" + Names[i]);
				}
				sessionObjectLabel.setText("会话对象:" + toClientName);
			}
		});
	}
	
	public void setMonitorForSentJButton() {
		sendJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
	}
	
	public void sendMessage() {
		if (toClientName != null && !toClientName.equals("")) {
			String msg = messageJTextField.getText();
			if (!msg.equals("")) {
				writer.println(toClientName + "#" + msg);
				messageJTextField.setText("");
			} else {
				JOptionPane.showMessageDialog(null,
						"你没有输入如何消息，请输入内容后再发送！", "提示",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"你没有选择要会话的对象，请先选择当前在线的客户进行会话，", "提示",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private class Monitor implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (ip != null && port != -1) {
				try {
					String loginName = clientNameTextField.getText();
					if (loginName.trim().equals("")) {
						JOptionPane.showMessageDialog(null,
								"用户名不能为空，请先输入客户名在登录！", "提示",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					socket = new Socket(ip, port);
					Client_Thread ct = new Client_Thread(socket,
							ChatClient.this);
					ct.start();
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					writer = new PrintWriter(socket.getOutputStream(), true);
					writer.println("login:" + loginName);    			//登陆消息前加标标志，表示这是个登陆消息
					sendJButton.setEnabled(true);
				} catch (ConnectException e1) {
					JOptionPane.showMessageDialog(null, "服务器没有开启，请先开启服务器！",
							"警告", JOptionPane.ERROR_MESSAGE);
				} catch (UnknownHostException e1) {
					displayTextArea.append(e1.getMessage());
					displayTextArea.setCaretPosition(displayTextArea.getText()
							.length());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(null, "请先设置有效的IP和端口");
			}
		}
	}

	public static void main(String[] args) {
		new ChatClient();
	}

}
