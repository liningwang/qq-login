package com.wangln;

import java.io.UnsupportedEncodingException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.Request;

import net.sf.json.JSONObject;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class LoginDao {
	Connection con;

	public LoginDao() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(
					"jdbc:mysql://127.0.0.1:3306/loginthrid", "root", "root");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void register(String nickname, String gender, String city,
			String photo, String openid) {
		try {
			PreparedStatement pre = (PreparedStatement) con
					.prepareStatement("insert into login(openid,name,url,address) values(?,?,?,?)");
			pre.setString(1, openid);
			pre.setBytes(2, nickname.getBytes("utf-8"));
			pre.setString(3, photo);
			pre.setString(4, city);
			pre.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String login(String openID) {
		Map<String, Object> map = new HashMap<String, Object>();
		int flag = 0; 
		try {
			PreparedStatement pre = (PreparedStatement) con
					.prepareStatement("select * from login where openid=?");

			pre.setString(1, openID);

			ResultSet result = pre.executeQuery();
			while (result.next()) {
				flag = 1;
				String name1 = result.getString("name");
				String address = result.getString("address");
				String url = result.getString("url");
				String openid = result.getString("openid");
				map.put("name", name1);
				map.put("address", address);
				map.put("url", url);
				map.put("openid", openid);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map.put("flag", flag);
		JSONObject obj = new JSONObject();
		obj.putAll(map);
		return obj.toString();
	}
}
