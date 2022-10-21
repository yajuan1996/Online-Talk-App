package tw.com.yj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tw.com.yj.utils.Friend;

public class YJUser extends JFrame {

	private JLabel ImgUser, UserName, UserID, ftitle;
	private JPanel friendlist;
	private ImageIcon addIcon,addIconNew,setIcon,setIconNew,logoutIcon,logoutIconNew;
	private JButton addFriend, setData, logout, newjbf;
	private LinkedList<String> friendlinklist;
	private LinkedList<Friend> friends ;
	private String Usernumber;
	private int roomnum, sbarValue;
	private Friend fuser;
	private JComboBox<String> condition;
	private JScrollPane jsp;
	private JScrollBar sbar;
	public Font myFont14 = new Font("微軟正黑體",Font.BOLD,14);
	public Font myFont16 = new Font("微軟正黑體",Font.BOLD,16);
	private Properties prop = new Properties();
	private Connection conn;
	
	
	// 建構式
	YJUser(String user) {
		super("個人主頁");
		setIconImage(new ImageIcon("YJDir/iconYJTalk.png").getImage());
				
		try {
			//資料庫連線
			prop.put("user", "talkuser");
			prop.put("password", "1111");
			conn = DriverManager.getConnection("jdbc:mysql://10.0.100.172:3306/yjtalk", prop);

			String sql = "SELECT * FROM userdata WHERE UserID = ? ";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			String Username = rs.getString("UserName");
			Usernumber = rs.getString("UserNumber");
			InputStream in = rs.getBinaryStream("UserIcon");
			BufferedImage bf = ImageIO.read(in);

			fuser = new Friend(Usernumber);
			
			ImgUser = new JLabel(new ImageIcon(bf));
			UserName = new JLabel(Username);
			UserID = new JLabel(user);
			friendlist = new JPanel();
			friendlist.setLayout(new BoxLayout(friendlist, BoxLayout.Y_AXIS));
			jsp = new JScrollPane(friendlist,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sbar=jsp.getVerticalScrollBar();
			
			ftitle=new JLabel(" 好友▼");
			
			friendlist.add(ftitle);
			ftitle.setAlignmentX(Component.LEFT_ALIGNMENT);
				
			friendlinklist = new LinkedList<>();
			//從關係表中取出所有朋友的編號,再從編號內容取得該朋友資料,再逐一加入linklist
			
			String sqlfindfriend = "SELECT Friend FROM friendlist WHERE User = ? ";
			PreparedStatement psfindfriend = conn.prepareStatement(sqlfindfriend);
			psfindfriend.setString(1, Usernumber);
			ResultSet rsfriend = psfindfriend.executeQuery();
			while(rsfriend.next()) {
				friendlinklist.add(rsfriend.getString("Friend"));
			}
			
			friends = new LinkedList<>();
			//巡訪所有朋友,並加入建立 朋友 類別	
			for(String f:friendlinklist) {
				friends.add(new Friend(f));
			}
			
			//載入所有朋友資訊
			for(Friend f:friends) {
				newjbf=new JButton(f.getname(),f.Icon());
				JLabel newfcond = new JLabel(f.getCondition());
				Box horizontalBox;
				horizontalBox = Box.createHorizontalBox();
				horizontalBox.setAlignmentX(Component.LEFT_ALIGNMENT);
				horizontalBox.add(newjbf);
				horizontalBox.add(Box.createHorizontalGlue());
				horizontalBox.add(newfcond);
				horizontalBox.add(Box.createRigidArea(new Dimension(10,0)));
				
				friendlist.add(horizontalBox);
				newjbf.setOpaque(false);
				newjbf.setContentAreaFilled(false);
				newjbf.setBorderPainted(false);

				newjbf.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println(Usernumber);
						System.out.println(f.getNumber());
							
						//載入聊天室
						loadchatroom(Usernumber,f.getNumber());
						
					}
				});				
				System.out.println(f.getname());
			}
			
			condition = new JComboBox<>();
			condition.setFont(myFont14);
			condition.addItem("在線上");
			condition.addItem("忙碌中");
			condition.addItem("已離線");
			condition.setForeground(new Color(10,155,0));
			condition.setFocusable(false);
			
			//狀態變更事件
			condition.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					// TODO Auto-generated method stub
					if(ItemEvent.SELECTED == e.getStateChange()) {
						System.out.println(e.getItem());
					
					//更新狀態到資料庫
						try {
						
							String Updatesql ="UPDATE userdata SET UserCondition=? WHERE UserID=?";
							PreparedStatement ps = conn.prepareStatement(Updatesql);
							ps.setString(1,e.getItem().toString());
							ps.setString(2,UserID.getText());
							ps.executeUpdate();
							System.out.println("updateCondition OK!");
							
							}catch(Exception e1) {
								System.out.println(e1.toString());
							}
						
					}
				}
			});
			
			condition.addPopupMenuListener(new PopupMenuListener() {
				
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					//下拉彈出
					condition.setForeground(Color.black);
				}
				
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					//下拉合上
					if(condition.getSelectedItem().equals("已離線")){
						condition.setForeground(Color.gray);
					}else if(condition.getSelectedItem().equals("忙碌中")) {
						condition.setForeground(Color.red);
					}else {
						condition.setForeground(new Color(10,155,0));
						
					}
					
				}
				
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					
				}
			});
			
			
			addIcon = new ImageIcon("YJdir/iconAdd.png");
			addIconNew = new ImageIcon("YJdir/iconAddNew.png");
			setIcon = new ImageIcon("YJdir/iconSet.png");
			setIconNew = new ImageIcon("YJdir/iconSetNew.png");
			logoutIcon = new ImageIcon("YJdir/iconLogout.png");
			logoutIconNew = new ImageIcon("YJdir/iconLogoutNew.png");
			addFriend = new JButton(addIcon);
			addFriend.setOpaque(false);
			addFriend.setContentAreaFilled(false);
			addFriend.setBorderPainted(false);
			addFriend.setPreferredSize(new Dimension(48, 48));
			setData = new JButton(setIcon);
			setData.setOpaque(false);
			setData.setContentAreaFilled(false);
			setData.setBorderPainted(false);
			setData.setPreferredSize(new Dimension(32, 32));
			logout = new JButton(logoutIcon);
			logout.setOpaque(false);
			logout.setContentAreaFilled(false);
			logout.setBorderPainted(false);
			logout.setPreferredSize(new Dimension(36, 36));
			
			//加入按鈕事件:滑鼠滑動與點擊
			MyBtnListener myBtnListener = new MyBtnListener();
			
			addFriend.addMouseListener(myBtnListener);
			setData.addMouseListener(myBtnListener);
			logout.addMouseListener(myBtnListener);

		} catch (Exception e) {
			System.out.println(e.toString());
		}

		setLayout(new BorderLayout());

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

		top.add(ImgUser);
		top.add(UserName);
		top.add(condition);		
		top.add(addFriend);
		top.add(setData);
		top.add(logout);
		
		//設定背景與字體
		friendlist.setBackground(Color.white);
		top.setBackground(Color.white);
		condition.setBackground(Color.white);
		ftitle.setFont(myFont16);
		UserName.setFont(myFont16);
		

		add(top, BorderLayout.NORTH);
		add(jsp, BorderLayout.CENTER);

		setSize(350, 480);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//週期性載入畫面
		Timer timer1 = new Timer();
		timer1.schedule(new loadFriend(),0, 3*1000);
		
		
		
	}
	
	private class MyBtnListener implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent e) {
			// 滑鼠點擊時
			if (e.getSource()==addFriend) {
				addBtn();
			}else if(e.getSource()==setData) {
				new set();
			}else if(e.getSource()==logout) {
				int isLogout = new JOptionPane().showConfirmDialog(null,"是否確定登出?","登出",JOptionPane.YES_NO_OPTION);
				
				//確認要登出
				if(isLogout==JOptionPane.YES_OPTION){
		            
					try {
						
					String Updatesql ="UPDATE userdata SET UserCondition=? WHERE UserID=?";
					PreparedStatement ps = conn.prepareStatement(Updatesql);
					ps.setString(1,"已離線");
					ps.setString(2,UserID.getText());
					ps.executeUpdate();
					System.out.println("updateCondition OK!");
					conn.close();
					System.exit(0);
					
					}catch(Exception e1) {
						System.out.println(e1.toString());
					}
//						new YJLogin();
					
				}
			}
			
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// 滑鼠進入
			if (e.getSource()==addFriend) {
				addFriend.setIcon(addIconNew);
			}else if(e.getSource()==setData) {
				setData.setIcon(setIconNew);
			}else if(e.getSource()==logout) {
				logout.setIcon(logoutIconNew);
			}
			
		}@Override
		public void mouseExited(MouseEvent e) {
			// 滑鼠離開
			if (e.getSource()==addFriend) {
				addFriend.setIcon(addIcon);
			}else if(e.getSource()==setData) {
				setData.setIcon(setIcon);
			}else if(e.getSource()==logout) {
				logout.setIcon(logoutIcon);
			}
		}
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	
	//方法：加入好友
	private void addBtn() {
		JOptionPane jop = new JOptionPane();
		String FriendID = jop.showInputDialog(null,"請輸入對方代號","加入好友", JOptionPane.QUESTION_MESSAGE);

		if (FriendID != null && FriendID.length() > 0) {
			System.out.println(FriendID);

			try {

				String sql = "SELECT * FROM userdata WHERE UserID = ? ";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, FriendID);
				ResultSet rs = pstmt.executeQuery();
				rs.next();
				
				// 取得對方資訊
				String FriendNumber = rs.getString("UserNumber");

				System.out.println(FriendNumber);
				Friend newf = new Friend(FriendNumber);
				friendlinklist.add(FriendNumber);
				friends.add(newf);
				
				//加入關係表
					String addfshipsql = "INSERT INTO friendlist (FriendNumber,User,Friend,FriendCondition) " + 
							"VALUES (?,?,?,?)";
					PreparedStatement addfshippstmt = conn.prepareStatement(addfshipsql);
					
					addfshippstmt.setString(1, new String(Usernumber+FriendNumber));
					addfshippstmt.setString(2, Usernumber);
					addfshippstmt.setString(3, FriendNumber);
					addfshippstmt.setString(4,"1");
					addfshippstmt.executeUpdate();
						
					System.out.println("addfriendship OK");
				
					
					
			} catch (Exception e) {
				System.out.println(e.toString());
			}

		} else {
			System.out.println("CANCEL");

		}
		new loadFriend().run();

	}

	private class set extends JFrame {
		private static JLabel newUName,newImg;
		private static TextField newName;
		private static File selectedFile = null;
		private JLabel oldUName,oldImg;
		private JButton changeIcon ;
		
		set() {
			super("設定");
			UIManager.put("Label.font", myFont14);
			UIManager.put("Button.font", myFont14);
			  
			setLayout(new BorderLayout());
			JLabel title = new JLabel("設定您的圖像/暱稱",SwingConstants.LEFT);
			title.setFont(myFont16);
			JPanel center = new JPanel(new GridLayout(3, 3, 0, 2));
			
//			center.add(new JLabel("項目",SwingConstants.CENTER));
			center.add(new JLabel("目前頭像/暱稱",SwingConstants.CENTER));
			center.add(new JLabel("預覽變更",SwingConstants.CENTER));
			center.add(new JLabel("新圖像/新暱稱",SwingConstants.CENTER));
//			center.add(new JLabel("圖像",SwingConstants.CENTER));
			oldImg = new JLabel(fuser.Icon());
			center.add(oldImg);
			
			ImageIcon newIcon = new ImageIcon();
			newImg = new JLabel(newIcon);
			center.add(newImg);
			ImageIcon chooseIcon = new ImageIcon("YJdir/Folders-Add.png");
			ImageIcon chooseIconNew = new ImageIcon("YJdir/Folders-Add-new.png");
			
			changeIcon = new JButton(chooseIcon);
			
			changeIcon.setOpaque(false);
			changeIcon.setContentAreaFilled(false);
			changeIcon.setBorderPainted(false);
			changeIcon.setFocusable(false);
			center.add(changeIcon);
			
			//滑鼠移動事件：變更顏色
			changeIcon.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					changeIcon.setIcon(chooseIcon);
				
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					changeIcon.setIcon(chooseIcon);
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					changeIcon.setIcon(chooseIconNew);
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			
//			center.add(new JLabel("暱稱",SwingConstants.CENTER));
			oldUName = new JLabel(fuser.getname(),SwingConstants.CENTER);
			center.add(oldUName);
			newUName = new JLabel("",SwingConstants.CENTER);
			center.add(newUName);
			newName = new TextField("",SwingConstants.CENTER);
			center.add(newName);
			add(title,BorderLayout.NORTH);
			add(center, BorderLayout.CENTER);
			JButton btnSet = new JButton("儲存");
			add(btnSet, BorderLayout.SOUTH);
			
			//設定背景
			title.setOpaque(true);
			title.setBackground(Color.white);
			center.setBackground(Color.white);
			
			//週期性更新欲變更之暱稱
			Timer timer = new Timer();
			timer.schedule(new updateTask(),0,600);
									
			changeIcon.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();//宣告filechooser 
					int returnValue = fileChooser.showOpenDialog(null);//叫出filechooser 
					if (returnValue == JFileChooser.APPROVE_OPTION) //判斷是否選擇檔案 
					{ 
					selectedFile = fileChooser.getSelectedFile();//指派給File 
					System.out.println(selectedFile.getName());//印出檔名 
					try {
												
						BufferedImage newIconbf=ImageIO.read(selectedFile);
						newIcon.setImage(newIconbf);
						repaint();
						
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					} 
				}
				
			});
			
			//按下save儲存後更新資料庫
			btnSet.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(newUName.getText().length()==0 ) {
						
					}else{
						try {

							String sql = "UPDATE userdata SET  UserName = ? WHERE UserID = ?";
							PreparedStatement ps = conn.prepareStatement(sql);
							ps.setString(1, newUName.getText());
							ps.setString(2, UserID.getText());
							ps.executeUpdate();	
							fuser.setName(newUName.getText());
							oldUName.setText(fuser.getname());
							UserName.setText(fuser.getname());
						} catch (Exception e2) {
							System.out.println(e2.toString());
						}
					};
					
					if(selectedFile!=null ) {
								
						try{
							FileInputStream fin = new FileInputStream(selectedFile);

							String sql = "UPDATE userdata SET UserIcon  = ? WHERE UserID = ?";
							PreparedStatement ps = conn.prepareStatement(sql);
							ps.setBinaryStream(1, fin);
							ps.setString(2, UserID.getText());
							ps.executeUpdate();
							
							
							ImgUser.setIcon(newIcon);
							
							fuser.setIcon(newIcon);
							
							fin.close();
							
							
						} catch (Exception e2) {
							System.out.println(e2.toString());
						};
					
					}
					timer.cancel();
					dispose();
					
					loadUserdata(UserID.getText());
					revalidate();
					
				}
			});
			setIconImage(new ImageIcon("YJDir/iconSet.png").getImage());
			
			setSize(320, 300);
			setLocationRelativeTo(null);
			setVisible(true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
		private static class updateTask extends TimerTask{
			@Override
			public void run() {
				newUName.setText(newName.getText());
			}
		}
		
	}
	
	public class loadFriend extends TimerTask {
		@Override
		public void run() {
			
		sbarValue= sbar.getValue();
		
		friendlinklist.clear();
		
		friendlist.removeAll();
		friendlist.add(ftitle);
				
		friendlinklist = new LinkedList<>();
		//從關係表中取出所有朋友的編號,再從編號內容取得該朋友資料,再逐一加入linklist
		try {

		String sqlfindfriend = "SELECT Friend FROM friendlist WHERE User = ? ";
		PreparedStatement psfindfriend = conn.prepareStatement(sqlfindfriend);
		psfindfriend.setString(1, Usernumber);
		ResultSet rsfriend = psfindfriend.executeQuery();
		while(rsfriend.next()) {
			friendlinklist.add(rsfriend.getString("Friend"));
		}
		
		friends = new LinkedList<>();
		//巡訪所有朋友編號		
		for(String f:friendlinklist) {
			friends.add(new Friend(f));
			
		}
		
		for(Friend f:friends) {
						
			newjbf=new JButton(f.getname(),f.Icon());
			newjbf.setFont(myFont14);
			JLabel newfcond = new JLabel(f.getCondition());
			newfcond.setFont(myFont14);
			if(f.getCondition().equals("已離線")){
				newfcond.setForeground(Color.gray);
			}else if(f.getCondition().equals("忙碌中")) {
				newfcond.setForeground(Color.red);
			}else {
				newfcond.setForeground(new Color(10,155,0));
			}
			Box horizontalBox;
			horizontalBox = Box.createHorizontalBox();
			horizontalBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			horizontalBox.add(newjbf);
			horizontalBox.add(Box.createHorizontalGlue());
			horizontalBox.add(newfcond);
			horizontalBox.add(Box.createRigidArea(new Dimension(10,0)));
	 
			friendlist.add(horizontalBox);
			newjbf.setOpaque(false);
			newjbf.setContentAreaFilled(false);
			newjbf.setBorderPainted(false);

			newjbf.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println(Usernumber);
					System.out.println(f.getNumber());
						
					//載入聊天室
					loadchatroom(Usernumber,f.getNumber());
					
				}
			});				
//			System.out.println(f.getname());
			
		}
		
//			System.out.println(sbarValue);
//			sbar.setValue(sbarValue);
		}catch(Exception e) {
			System.out.println(e.toString());
		}finally {
//			System.out.println(sbarValue);
			sbar.setValue(sbarValue);
		}
				
		}
	}

	
	public static void main(String[] args) {
		new YJUser("d");
	}
	
	public void loadUserdata(String user) {
		try {

			String sql = "SELECT * FROM userdata WHERE UserID = ? ";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			String Username = rs.getString("UserName");
//			Usernumber = rs.getString("UserNumber");
			InputStream in = rs.getBinaryStream("UserIcon");
			BufferedImage bf = ImageIO.read(in);
			
			
			
		}catch(Exception e) {
			System.out.println(e.toString());
		}
	}

	public void loadchatroom(String Usernumber,String fnumber) {
	
		try {

			//查詢有無聊天室,查無時新增聊天室並加入成員
			
			String sql = "SELECT RoomNumber FROM roommember "
					+ "WHERE RoomType=1 AND MemberNumber =? AND RoomNumber "
					+ "IN (SELECT RoomNumber FROM roommember "
					+ "WHERE RoomType=1 AND MemberNumber =? )";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,Usernumber);
			pstmt.setString(2, fnumber);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				//已有聊天室
				roomnum = rs.getInt("RoomNumber");
				System.out.println("有聊天室"+Usernumber+fnumber+roomnum);
				new YJDialog(Usernumber,fnumber,roomnum);
			}else {
				//沒有聊天室
																
				//取得現有聊天室數量
				String roomnumsql = "SELECT count(RoomNumber) FROM chatroom ";		
				PreparedStatement roomnumps = conn.prepareStatement(roomnumsql);
				ResultSet roomnumrs = roomnumps.executeQuery();
				roomnumrs.next();
				roomnum = roomnumrs.getInt(1);
				System.out.println(roomnum);
				
				//將聊天室加入資料庫
				String addchatroomsql ="INSERT INTO chatroom "
						+ "(RoomNumber, RoomName, RoomFile) VALUES (?,?, NULL)";
				PreparedStatement chatroomps = conn.prepareStatement(addchatroomsql);
				chatroomps.setInt(1, roomnum+1);
				chatroomps.setString(2, Usernumber+fnumber);
				chatroomps.executeUpdate();
				System.out.println("add chatroom OK!");
				
				//建立聊天室與成員關聯表				
				String addroommembersql ="INSERT INTO roommember "
						+ "(RoomNumber, MemberNumber, RoomType) VALUES (?,?, 1),(?,?, 1)";
				PreparedStatement roommemberps = conn.prepareStatement(addroommembersql);
				roommemberps.setInt(1, roomnum+1);
				roommemberps.setString(2, Usernumber);
				roommemberps.setInt(3, roomnum+1);
				roommemberps.setString(4, fnumber);
				roommemberps.executeUpdate();
				System.out.println("add roommember OK!");
				
				//建立新聊天室
				new YJDialog(Usernumber,fnumber,roomnum+1);				
				
			}
			
		} catch (Exception e2) {
			System.out.println(e2.toString());
		};
	
	
	}
	
	
}