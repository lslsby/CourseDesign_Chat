package cn.edu.sdju.chat;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Server_Thread extends Thread {
	
	private String loginName;

	private Socket socket;
	
	private Map<String,Server_Thread> threadList;
	
	private JTextArea displayTextArea;
	
	BufferedReader reader;
	
	PrintWriter writer;
	
	public Server_Thread(Socket socket, Map threadList, JTextArea displayTextArea) {
		this.socket = socket;
		this.threadList = threadList;
		this.displayTextArea = displayTextArea;
		
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			String msg = null;
			try {
				msg = reader.readLine();
				//下面的条件语句是判断消息的特征，是登陆，登出，还是其他的特征
				if (msg.startsWith("login")) {
					loginName = msg.substring(msg.indexOf(':') + 1).trim();
					if (! threadList.containsKey(loginName)) {
						writer.println(loginName + ": 登录成功!");
						threadList.put(loginName, this);
						displayTextArea.append(loginName + " 进入聊天室 \n");
						displayTextArea.setCaretPosition(displayTextArea.getText().length());        //设置滚动条显示在低端
						
						Iterator<Entry<String, Server_Thread>> it = threadList.entrySet().iterator();       // 通知所有在线用户 该用户上线了
						while (it.hasNext()) {
							Map.Entry entry = (Map.Entry) it.next();
							Server_Thread st = (Server_Thread) entry.getValue();
							if (st != this) {
								st.writer.println("用户上线:" + loginName);               // 将上线用户通知各个线程
								writer.println("用户上线:" + st.loginName);            // 各个线程通知此上线用户
							}
						}
					} else {
//						JOptionPane.showMessageDialog(null, loginName + " 已存在，请选择其他的用户名登录", "提示", JOptionPane.INFORMATION_MESSAGE);
						writer.println(loginName + " 已存在，请选择其他的用户名登录");
					}
				} else if(msg.startsWith("logout")){
					logout();
					break;
				} else {
					String [] toClientNames;
					toClientNames = msg.substring(msg.indexOf('#') + 1).split("#");
					String message = toClientNames[toClientNames.length - 1];       //最后一个分隔得是要发送给客户的消息
					for (int i = 0; i < toClientNames.length - 1; i++) {
						Server_Thread st = threadList.get(toClientNames[i].trim());
//						if(st != this) {
							st.writer.println(loginName + ": " + message);
//						}
					}
					writer.println(loginName + ": " + message);    //向自己也发送信息
				}
			} catch (IOException e) {
				logout();
				break;
			}
		}
	}
	
	public void logout() {
		Iterator it = threadList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Server_Thread st = (Server_Thread) entry.getValue();
			if (st != this && st.isAlive()) {
				st.writer.println("用户下线:" + loginName);
			}
		}
		threadList.remove(loginName);
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
		}
		Date now = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd E kk:mm:ss");
		displayTextArea.append("\n" + f.format(now) + "\n");
		displayTextArea.append("用户下线:" + loginName + "\n");
		displayTextArea.setCaretPosition(displayTextArea.getText().length());
	}
}
