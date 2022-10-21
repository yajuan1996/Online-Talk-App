package tw.com.yj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import tw.com.yj.utils.Context;
import tw.com.yj.utils.Friend;

public class YJDialog extends JFrame {

	private JLabel ImgFriend,FriendName,FriendCondition;
	private int UserNumber;
	private JPanel dialog;
	private JTextField inputText;
	private JButton sendBtn;
	private JScrollPane jsp;
	private LinkedList<Context> Contexts;
	private int roomnumber;
	private Friend talkfriend;	
	public Font myFont14 = new Font("微軟正黑體",Font.BOLD,14);
	public Font myFont16 = new Font("微軟正黑體",Font.BOLD,16);
	private Properties prop = new Properties();
	private Connection conn;
	
	// 與朋友的對話框
	public YJDialog(String userNumber,String friendNumber,int rnumber) {
		super("聊天");
		
		//資料庫連線
		try {
		prop.put("user", "talkuser");
		prop.put("password", "1111");
		conn = DriverManager.getConnection(
				"jdbc:mysql://10.0.100.172:3306/yjtalk",prop);
		}catch(Exception e) {
			System.out.println("資料庫連線失敗");
		}
		
		setIconImage(new ImageIcon("YJDir/iconYJTalk.png").getImage());
		UIManager.put("Label.font", myFont14);
		UIManager.put("Button.font", myFont14);
		
		talkfriend = new Friend(friendNumber);
		ImgFriend = new JLabel(talkfriend.Icon());
		FriendName = new JLabel(talkfriend.getname());
		FriendName.setFont(myFont16);
		
		FriendCondition = new JLabel(talkfriend.getCondition());
		if(talkfriend.getCondition().equals("已離線")){
			FriendCondition.setForeground(Color.gray);
		}else if(talkfriend.getCondition().equals("忙碌中")) {
			FriendCondition.setForeground(Color.red);
		}else {
			FriendCondition.setForeground(new Color(10,155,0));
		}
		
		
		UserNumber = Integer.parseInt(userNumber);
		roomnumber = rnumber;
		dialog = new JPanel();
		dialog.setLayout(new BoxLayout(dialog,BoxLayout.Y_AXIS));
		jsp = new JScrollPane(dialog,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		inputText = new JTextField();
		inputText.setFont(myFont16);
		sendBtn = new JButton("傳送");
		sendBtn.setFont(myFont16);
		Contexts = new LinkedList<>();
		
		//載入聊天內容
		loadContext(roomnumber);
		
		// 排版
		setLayout(new BorderLayout());

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

		top.add(ImgFriend);
		top.add(FriendName);
		top.add(FriendCondition);

		add(top, BorderLayout.NORTH);
		add(jsp, BorderLayout.CENTER);

		
		JPanel bottomJPanel = new JPanel(new BorderLayout());
		bottomJPanel.add(inputText, BorderLayout.CENTER);
		bottomJPanel.add(sendBtn, BorderLayout.EAST);

		add(bottomJPanel, BorderLayout.SOUTH);

		// 按下按鈕的監聽
		sendBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				clickBtn();
			}
		});
		
		//設定背景
		dialog.setBackground(Color.white);
		top.setBackground(Color.white);
		
		setSize(350, 480);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		//接收訊息
//		receiveMsg remsg = new receiveMsg();
//		Timer timer1 = new Timer();
//		timer1.schedule(remsg,0,2*1000);
		
		//週期性更新聊天內容
		updateMsg updatemsg = new updateMsg();
		Timer timer = new Timer();
		timer.schedule(updatemsg,0,1*1000);
		
		//滾輪控制在最下方
		JScrollBar sbar=jsp.getVerticalScrollBar();
		sbar.setValue(sbar.getMaximum());
		
	}

	public static void main(String[] args) {
		YJDialog yj = new YJDialog("2","1",1);
		
	}
	
	// 按下傳送按鈕 
		public void clickBtn() {
//			InetAddress addr;
//			try {
//			addr = InetAddress.getLocalHost();
//			System.out.println("Local Host Address:"+addr.getHostAddress());
//					
//			} catch (UnknownHostException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		    
			String getText = inputText.getText();
			inputText.setText("");
					
		
			//單筆資料加入資料庫			
			try {

			String addmsgsql = "INSERT INTO message (RoomNumber,Sender,Context) " + 
					"VALUES (?,?,?)";
			PreparedStatement addmsgpstmt = conn.prepareStatement(addmsgsql);
			
			addmsgpstmt.setInt(1, roomnumber);
			addmsgpstmt.setInt(2, (int)UserNumber);
			addmsgpstmt.setString(3, getText);
			addmsgpstmt.executeUpdate();
				
			System.out.println("addmessage OK");	
			
			}catch(Exception e) {
				System.out.println(e.toString());
			}
			
			//傳送給對方
			try {
				
				//UDP
				
//				byte[] msg = getText.getBytes();
//				
//				DatagramSocket socket = new DatagramSocket();
//				DatagramPacket packet = new DatagramPacket(msg, msg.length, 
//						InetAddress.getByName(talkfriend.getIp()), 8888);
//				socket.send(packet);
//				socket.close();
//				TCP
//				Socket socket = new Socket(InetAddress.getByName(talkfriend.getIp()),
//						8888);
//				
//				OutputStream out = socket.getOutputStream();
//				out.write(getText.getBytes());
//				out.flush();
//				out.close();
//				
//				socket.close();
//				dialog.removeAll();
//				loadContext(roomnumber);
//				revalidate();
			
				JScrollBar sbar=jsp.getVerticalScrollBar();
				sbar.setValue(sbar.getMaximum());
				
				System.out.println("Send OK");
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			
		}
		//載入所有對話
		public void loadContext(int roomnumber) {
			Contexts.clear();
			
			try {

			String sql = "SELECT * FROM message WHERE RoomNumber = ? ";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, roomnumber);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
			Context context = new Context()	;
			context.setRoomnumber(roomnumber);
			context.setSender(rs.getInt("Sender"));
			context.setText(rs.getString("Context"));
			Contexts.add(context);
			
			};
			for(Context c:Contexts) {
			
				Friend f = new Friend(c.getSender()+"");
				
				JLabel textlabel = new JLabel(f.getname()+":"+c.getText());
				Box horizontalBox;
				horizontalBox = Box.createHorizontalBox();
				
				//使用者訊息靠右
				if(f.getNumber().equals(UserNumber+"")) {
				
					horizontalBox.add(Box.createHorizontalGlue());
				    horizontalBox.add(textlabel);
					dialog.add(horizontalBox);
					dialog.add(Box.createRigidArea(new Dimension(0,10)));
					
				}else {
					
					horizontalBox.add(textlabel);
				    horizontalBox.add(Box.createHorizontalGlue());
				    dialog.add(horizontalBox);
				    dialog.add(Box.createRigidArea(new Dimension(0,10)));
				    
				}
				
			}
			}catch(Exception e) {
				System.out.println(e.toString());
			}
		}
	
	private class updateMsg extends TimerTask{
		@Override
		public void run() {
			dialog.removeAll();
			loadContext(roomnumber);
			revalidate();
			
			JScrollBar sbar=jsp.getVerticalScrollBar();
			sbar.setValue(sbar.getMaximum());
		}
	}
	
	//IP問題---暫不進行
	private class receiveMsg extends TimerTask{
		@Override
		public void run() {
			try {
				
//				UDP
//				byte [] buf = new byte[4*1024];
//				DatagramSocket socket = new DatagramSocket(8888);
//				DatagramPacket packet = new DatagramPacket(buf, buf.length);
//				System.out.println("wait......");
//				socket.receive(packet);
//				socket.close();
//				
//				String ip = packet.getAddress().getHostAddress(); 
//				byte [] data = packet.getData(); //取得傳送的內容
//				int size = packet.getLength();
//				String mesg = new String(data,0,size);
//				System.out.println(ip+":"+mesg);
				
				
//				TCP
//				ServerSocket server = new ServerSocket(8888);
//				System.out.println("wait......");
//				Socket socket = server.accept();
//				
//				
//				String urip = socket.getInetAddress().getHostAddress();
//				System.out.println("ip => " + urip);
//				
//				InputStream in = socket.getInputStream();
//				InputStreamReader ir = new InputStreamReader(in);
//				BufferedReader reader = new BufferedReader(ir);
//				
//				String line; StringBuffer sb = new StringBuffer();
//				while ( (line = reader.readLine()) != null) {
//					sb.append(line);
//					System.out.println(line);
//				}
//				in.close();
//				
//				socket.close();
//					
//				server.close();
//				System.out.println("Server OK");
//				System.out.println(urip + ":" + sb.toString());
				
//				loadContext(roomnumber);
				
//				Context context = new Context()	;
//				context.setRoomnumber(roomnumber);
//				context.setSender(Integer.parseInt(talkfriend.getNumber()) );
//				context.setText(mesg);
//				Contexts.add(context);
				
			//	JLabel receivetextlabel = new JLabel("receive:"+sb.toString());
			//	dialog.add(receivetextlabel);
				
//				dialog.removeAll();
//				loadContext(roomnumber);
//				revalidate();
			
				JScrollBar sbar=jsp.getVerticalScrollBar();
				sbar.setValue(sbar.getMaximum());
				
			} catch (Exception e) {
				System.out.println(e.toString());
			
			}
		}
	}
}


