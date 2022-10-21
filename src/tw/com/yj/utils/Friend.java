package tw.com.yj.utils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Friend {
	private String name, id, number,ip,condition;
	private JLabel labelicon;
	private ImageIcon newIcon;
	Properties prop = new Properties();

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Friend(String number) {
		this.number = number;

		prop.put("user", "talkuser");
		prop.put("password", "1111");
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.100.172:3306/yjtalk", prop);

			String sql = "SELECT * FROM userdata WHERE UserNumber = ? ";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, number);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			name = rs.getString("UserName");
			ip = rs.getString("UserIP");
			InputStream in = rs.getBinaryStream("UserIcon");
			BufferedImage bf = ImageIO.read(in);
			newIcon = new ImageIcon(bf);
			condition = rs.getString("UserCondition");

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public JLabel getIcon() {

		labelicon = new JLabel(name,newIcon,SwingConstants.LEFT);

		return labelicon;
	}

	public ImageIcon Icon() {

		return newIcon;
	}

	public String getname() {
		return name;
	}

	public String getid() {
		return id;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public void setIcon(ImageIcon newIcon) {
		this.newIcon=newIcon;
	}
	
	
}
