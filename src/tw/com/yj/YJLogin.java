package tw.com.yj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class YJLogin extends JFrame {

	private JButton btnCheckLogin,btnSignup;
	private JLabel iconTalk,lbID,lbPwd;
	private JTextField inputID;
	private JPasswordField inputPwd;
	private Font myBtnFont = new Font("微軟正黑體",Font.BOLD,14);
	private Font myJLFont = new Font("微軟正黑體",Font.BOLD,14);
	private Properties prop = new Properties();	
	private Connection conn;
	
	public YJLogin() {
		super("登入");
		
		//取得現有IP
		InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				  System.out.println("Local Host IP:"+addr.getHostAddress());
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		
		//資料庫連線
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Driver OK");
			
		prop.put("user", "talkuser");
		prop.put("password", "1111");
		conn = DriverManager.getConnection(
				"jdbc:mysql://10.0.100.172:3306/yjtalk",prop);
		}catch(Exception e) {
			System.out.println("資料庫連線失敗");
		}
		setIconImage(new ImageIcon("YJDir/iconYJTalk.png").getImage());
		UIManager.put("Label.font", myJLFont);
		UIManager.put("Button.font", myBtnFont);
		
		iconTalk = new JLabel(new ImageIcon("YJdir/iconYJTalk.png"));
		lbID = new JLabel("使用者代號",JLabel.LEFT);
		lbPwd = new JLabel("密　　　碼",JLabel.LEFT);
		inputID = new JTextField(12);
		inputPwd = new JPasswordField(12);
		btnCheckLogin = new JButton("登入");
		btnSignup = new JButton("註冊");
		
		lbID.setFont(myJLFont);
		lbPwd.setFont(myJLFont);
		inputID.setFont(myBtnFont);
		inputPwd.setFont(myBtnFont);
		btnCheckLogin.setFont(myBtnFont);
		btnSignup.setFont(myBtnFont);
		//排版
		setLayout(new BorderLayout());
		
		JPanel top = new JPanel(new FlowLayout());
		top.add(iconTalk);
		top.setBackground(Color.white);
				
		add(top,BorderLayout.NORTH);
		
		JPanel center = new JPanel();
		
		GridBagLayout gbCenter = new GridBagLayout();
		GridBagConstraints gbConstraints = new GridBagConstraints();
		center.setLayout(gbCenter);
		
		gbConstraints.gridx = 0;
		gbConstraints.gridy = 0;
		gbConstraints.gridwidth=1;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =1;
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor=GridBagConstraints.EAST;
		center.add(lbID,gbConstraints);
		
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 0;
		gbConstraints.gridwidth=2;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =1;
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor=GridBagConstraints.WEST;
		center.add(inputID,gbConstraints);
		
		gbConstraints.gridx = 0;
		gbConstraints.gridy = 1;
		gbConstraints.gridwidth=1;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =1;
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor=GridBagConstraints.EAST;
		center.add(lbPwd,gbConstraints);
		
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 1;
		gbConstraints.gridwidth=2;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =1;
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor=GridBagConstraints.WEST;
		center.add(inputPwd,gbConstraints);
		
		gbConstraints.gridx = 0;
		gbConstraints.gridy = 2;
		gbConstraints.gridwidth=3;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =1;
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor=GridBagConstraints.CENTER;
		center.add(btnCheckLogin,gbConstraints);
		
		gbConstraints.gridx = 0;
		gbConstraints.gridy = 3;
		gbConstraints.gridwidth=3;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =1;
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor=GridBagConstraints.CENTER;
		center.add(btnSignup,gbConstraints);
		
		//空白區塊
		gbConstraints.gridx = 0;
		gbConstraints.gridy = 4;
		gbConstraints.gridwidth=3;
		gbConstraints.gridheight=1;
		gbConstraints.weightx =1;
		gbConstraints.weighty =4;
		gbConstraints.fill = GridBagConstraints.BOTH;
		gbConstraints.anchor=GridBagConstraints.CENTER;
		
		JPanel bottom = new JPanel();
		center.add(bottom,gbConstraints);
		bottom.setBackground(Color.white);
		center.setBackground(Color.white);
		
		add(center,BorderLayout.CENTER);
		
		btnSignup.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new SignupFrame();
			}
		});
		
		//登入按鈕
		btnCheckLogin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(!inputID.getText().equals("") && !inputPwd.getText().equals("") ) {
					
					try {
					
					String sql = "SELECT * FROM userdata WHERE UserID = ? ";
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,inputID.getText());
					ResultSet rs = pstmt.executeQuery();
					
					//向資料庫查詢是否有該帳號
					if(rs.next()) {
						
						//比對密碼
						String pswd = rs.getString("UserPassword");
						if(inputPwd.getText().equals(pswd)) {
							System.out.println("Login OK!");
							new JOptionPane().showMessageDialog(null,"登入成功!");
						new YJUser(inputID.getText()); //開啟user主頁
						
						//取得登入者IP,將IP更新至資料庫
						 InetAddress addr;
							try {
								addr = InetAddress.getLocalHost();
								  System.out.println("Local HostAddress:"+addr.getHostAddress());
								  String getIPsql ="UPDATE userdata SET UserIP=?,UserCondition=? WHERE UserID=?";
									PreparedStatement ps = conn.prepareStatement(getIPsql);
									ps.setString(1,addr.getHostAddress());
									ps.setString(2,"在線上");
									ps.setString(3,inputID.getText());
									ps.executeUpdate();
									System.out.println("updateIP OK!");
								  
							} catch (UnknownHostException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						
						closeframe(); //關閉登入畫面
						}
						
					}else {
						new JOptionPane().showMessageDialog(null, "帳號/密碼錯誤!", "錯誤", JOptionPane.ERROR_MESSAGE);
					}
						
						
					}catch(Exception e1){
						System.out.println(e1.toString());
					}
					
					
				}else {
					new JOptionPane().showMessageDialog(null, "帳號/密碼錯誤!", "錯誤", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		
		setSize(350, 480);
		setLocationRelativeTo(null);
		setBackground(Color.white);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
	}
	public void closeframe() {
		this.dispose(); //關閉視窗
	}
	
	public static void main(String[] args) {
		new YJLogin();
	}

	
	private class SignupFrame extends JFrame{
		private JLabel UserID,UserAccount,UserPwd,UserName,UserTel;
		private JTextField inputUserID,inputUserAccount,inputUserName,inputUserIcon,inputUserTel;
		private JPasswordField inputUserPwd;
		
		SignupFrame(){
			super("註冊");
			setIconImage(new ImageIcon("YJDir/iconYJTalk.png").getImage());
			
			JLabel title = new JLabel("請提供您的資訊：");
			JButton btnCheckSignup = new JButton("註冊");
			
			UserID = new JLabel("使用者代號");
			UserAccount =  new JLabel("電子信箱");
			UserPwd =  new JLabel("設定密碼");
			UserName =  new JLabel("姓　　名");
			UserTel =  new JLabel("電　　話");
			inputUserID = new JTextField(8);
			inputUserAccount = new JTextField(8);
			inputUserPwd = new JPasswordField(8);
			inputUserName = new JTextField(8);
			inputUserTel = new JTextField(8);
			
			title.setFont(myJLFont);
			UserID.setFont(myJLFont);
			UserAccount.setFont(myJLFont);
			UserPwd.setFont(myJLFont);
			UserName.setFont(myJLFont);
			UserTel.setFont(myJLFont);
			inputUserID.setFont(myJLFont);
			inputUserAccount.setFont(myBtnFont);
			inputUserPwd.setFont(myBtnFont);
			inputUserName.setFont(myBtnFont);
			inputUserTel.setFont(myBtnFont);
			btnCheckSignup.setFont(myJLFont);

			title.setOpaque(true);
			title.setBackground(Color.white);
			
			setLayout(new BorderLayout());
			add(title,BorderLayout.NORTH);
	
			add(btnCheckSignup,BorderLayout.SOUTH);
			
			JPanel center =new JPanel(new GridLayout(5,2,0,2));
			
			center.add(UserID);
			center.add(inputUserID);
			center.add(UserAccount);
			center.add(inputUserAccount);
			center.add(UserPwd);
			center.add(inputUserPwd);
			center.add(UserName);
			center.add(inputUserName);
			center.add(UserTel);
			center.add(inputUserTel);
			UserID.setHorizontalAlignment(JLabel.CENTER);
			UserAccount.setHorizontalAlignment(JLabel.CENTER);
			UserPwd.setHorizontalAlignment(JLabel.CENTER);
			UserName.setHorizontalAlignment(JLabel.CENTER);
			UserTel.setHorizontalAlignment(JLabel.CENTER);
			
			add(center,BorderLayout.CENTER);
			btnCheckSignup.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					insertData();
					dispose();
				}
			});
			
			center.setBackground(Color.white);
			
			setSize(320, 240);
			setLocationRelativeTo(null);
			setVisible(true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		}
		
		private void insertData() {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				System.out.println("Driver OK");
				
			} catch (ClassNotFoundException e) {
				System.out.println(e.toString());
			}
			
			try {
				
			
			String sql = "INSERT INTO userdata (UserID,UserAccount,UserPassword,UserName,UserTel,UserIcon,UserIP) " + 
					"VALUES (?,?,?,?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
				pstmt.setString(1, inputUserID.getText());
				pstmt.setString(2, inputUserAccount.getText());
				pstmt.setString(3, inputUserPwd.getText());
				pstmt.setString(4, inputUserName.getText());
				pstmt.setString(5, inputUserTel.getText());
				pstmt.setBinaryStream(6,new FileInputStream("YJdir/iconUser0.png"));
				
				
				InetAddress addr= InetAddress.getLocalHost();
				System.out.println("Signup IP:"+addr.getHostAddress());
				
				pstmt.setString(7,addr.getHostAddress());
				pstmt.executeUpdate();
				System.out.println("insertDB OK");
				new JOptionPane().showMessageDialog(null,"已註冊成功!");
				
				
			}catch(Exception e){
				System.out.println(e.toString());
			}
			
		}
	}
}
