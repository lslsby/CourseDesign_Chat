package cn.edu.sdju.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class Client_Thread extends Thread {

	private Socket socket;

	private ChatClient cc;

	private BufferedReader reader;

	public Client_Thread(Socket socket, ChatClient cc) {
		this.socket = socket;
		this.cc = cc;

		try {
			reader = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
		} catch (IOException e) {

		}
	}

	public void run() {
		String s = null;
		while (true) {
			try {
				while((s = reader.readLine()) != null) {
					if (s.endsWith("登录成功!")) {
						addDate();
						cc.displayTextArea.append(s + "\n");
						cc.logoutJButton.setEnabled(true);
						cc.clientNameTextField.setEnabled(false);
						cc.loginButton.setEnabled(false);
					} else if (s.startsWith("用户上线:")) {
						String loginName = s.substring(s.indexOf(":") + 1);
						addDate();
						cc.displayTextArea.append("客户：" + loginName + "上线  \n");
						cc.displayTextArea.setCaretPosition(cc.displayTextArea.getText().length());
						cc.currentClientList.add(loginName);
						continue;
					} else if (s.startsWith("用户下线:")) {
						String loginName = s.substring(s.indexOf(":") + 1);
						addDate();
						cc.displayTextArea.append("客户：" + loginName + " 下线    \r\n");
						cc.displayTextArea.setCaretPosition(cc.displayTextArea.getText().length());
						cc.currentClientList.remove(loginName);
						continue;
					} else {
						addDate();
						cc.displayTextArea.append("客户" + s +"\n");
						cc.displayTextArea.setCaretPosition(cc.displayTextArea.getText().length());
					}
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "服务器异常！", "提示", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
	}
	
	public void addDate() {
		Date now = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd E kk:mm:ss");
		cc.displayTextArea.append("\n" + f.format(now) + "\n");
		cc.displayTextArea.setCaretPosition(cc.displayTextArea.getText().length());
	}

}
