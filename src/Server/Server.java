package Server;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;

import DB.ConnectDB;
import Model.Message;
//import Model.Attachment;
import Model.User;

public class Server {
	public static void main(String[] args) throws Exception {
		new Server();
	}

	Vector<Menber> listUser = new Vector<>();

	public Server() {
		try {
			@SuppressWarnings("resource")
			ServerSocket server = new ServerSocket(9696);
			System.out.println("Server is open");
			while (true) {
				Socket socket = server.accept();
				Menber thread = new Menber(socket, this);
				listUser.add(thread);
				thread.start();
			}
		} catch (Exception e) {
			System.out.println("Error Server");
		}
	}
}

class Menber extends Thread {
	Socket socket;
	Server server;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	private PrintWriter pw = null;
	Gson gson = new Gson();

	public Menber(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		try {
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			if (pw == null) {
				pw = new PrintWriter(socket.getOutputStream());
			}
		} catch (Exception e) {
			System.out.println("Error User Thread");
		}
	}

	@SuppressWarnings("unchecked")
	public void run() {
		try {
			while (true) {
				String line = br.readLine();
				System.out.println(line + "");
				HashMap<String, String> request = new HashMap<>();
				request = gson.fromJson(line, request.getClass());
				HashMap<String, String> response = new HashMap<>();
				switch (request.get("command")) {
				case "authenticate":
					String username = request.get("username");
					String password = request.get("password");
					System.out.println("Login " + username + " " + password);
					if (login(username, password) == null) {
						response.put("status", "fail");
					} else {
						response.put("status", "success");
						response.put("id", Integer.toString(login(username, password).getid()));
						response.put("username", username);
						response.put("password", password);
					}
					break;
				case "register":
					String name = request.get("username");
					String pass = request.get("password");
					String email = request.get("email");
					User user = new User();
					user.setusername(name);
					user.setpassword(pass);
					user.setemail(email);
					System.out.println("Register " + user.getusername() + user.getpassword() + user.getemail());
					if (register(user) == false) {
						response.put("status", "fail");
					} else {
						response.put("status", "success");
						response.put("username", name);
						response.put("password", pass);
					}
					break;
				case "insert_mess":
					int id_sender = Integer.parseInt(request.get("id_sender"));
					String namereceiver = (String) request.get("receiver");
					int id_receiver = getIDbyUsername(namereceiver);
					String title = request.get("title");
					String content = request.get("content");
					String create_at = request.get("create_at");
					Message message = new Message();
					message.setid_sender(id_sender);
					message.setid_receiver(id_receiver);
					message.settitle(title);
					message.setcontent(content);
					message.setcreate_at(create_at);
					System.out.println("Add " + message.getid_sender() + message.getid_receiver() + message.gettitle()
							+ message.getcontent() + message.getcreate_at());
					if (addMess(message) == false) {
						response.put("status", "fail");
					} else {
						response.put("status", "success");
						response.put("id_sender", String.valueOf(id_sender));
						response.put("id_receiverr", String.valueOf(id_receiver));
						response.put("title", title);
						response.put("content", content);
						response.put("create_at", String.valueOf(create_at));
					}
					break;
				case "show_listMess":
					int id = Integer.parseInt(request.get("id"));
					ArrayList<Message> listmess = getAllMess(id);
					String list = gson.toJson(listmess);
					if (listmess != null) {
						response.put("status", "success");
						response.put("id", String.valueOf(id));
						response.put("show_listmess", list);
					} else {
						response.put("status", "fail");
					}
					break;
				case "getAllUser":
					ArrayList<User> listUser = getAllUser();
					String list_user = gson.toJson(listUser);
					if (listUser != null) {
						response.put("status", "success");
						response.put("listUser", list_user);
					} else {
						response.put("status", "fail");
					}
					break;
				case "search":
					String text = (String) request.get("text");
					int id_search = Integer.parseInt(request.get("id"));
					ArrayList<Message> list_search = searchMessage(text, id_search);
					String search = gson.toJson(list_search);
					if (list_search != null) {
						response.put("status", "success");
						response.put("id", String.valueOf(id_search));
						response.put("search", search);
					} else {
						response.put("status", "fail");
					}
					break;
				case "show_Mess":
					int mess_id = Integer.parseInt(request.get("id"));
					Message mess = getMess(mess_id);
					String mesString = gson.toJson(mess);
					response.put("status", "success");
					response.put("mess_id", String.valueOf(mess_id));
					response.put("show_Mess", mesString);
					break;
				case "delete_Mess":
					int Id = Integer.parseInt(request.get("id"));
					if (deleteMess(Id) > 0) {
						response.put("status", "success");
					} else {
						response.put("status", "fail");
					}
					break;
				}
				String responseLine = gson.toJson(response);
				responseLine = responseLine + "\n";
				System.out.println(responseLine);
				pw.write(responseLine);
				pw.flush();
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	public int getIDbyUsername(String username) throws SQLException {
		ResultSet result = null;
		Connection connect = ConnectDB.getConnection();
		String sql = "select id from user where username=?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setString(1, username);
			result = pst.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		result.next();
		return result.getInt("id");
	}

	public User login(String username, String password) {
		Connection connect = ConnectDB.getConnection();
		String sql = "select * from user where username='" + username + "' and password='" + password + "'";
		PreparedStatement ps;
		try {
			ps = (PreparedStatement) connect.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				User user = new User();
				user.setid(rs.getInt("id"));
				user.setusername(rs.getString("username"));
				user.setpassword(rs.getString("password"));
				user.setemail(rs.getString("email"));
				connect.close();
				return user;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean register(User user) {
		Connection connect = ConnectDB.getConnection();
		String sql = "insert into user(username,password,email) values(?,?,?)";
		try {
			PreparedStatement ps = connect.prepareCall(sql);
			ps.setString(1, user.getusername());
			ps.setString(2, user.getpassword());
			ps.setString(3, user.getemail());
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean addMess(Message message) {
		Connection connect = ConnectDB.getConnection();
		String sql = "insert into message (id_sender, id_receiver, title, content, create_at) values(?,?,?,?,?)";
		try {
			PreparedStatement ps = connect.prepareCall(sql);
			ps.setInt(1, message.getid_sender());
			ps.setInt(2, message.getid_receiver());
			ps.setString(3, message.gettitle());
			ps.setString(4, message.getcontent());
			ps.setString(5, message.getcreate_at());
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<Message> getAllMess(int id) {
		ArrayList<Message> listmess = new ArrayList<>();
		Connection connect = ConnectDB.getConnection();
		String sql = "select * from message where id_receiver = ? order by id desc";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Message mess = new Message(rs.getInt("id"), rs.getInt("id_sender"), rs.getInt("id_receiver"),
						rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
				listmess.add(mess);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listmess;
	}

	public ArrayList<User> getAllUser() {
		ArrayList<User> listUser = new ArrayList<>();
		Connection connect = ConnectDB.getConnection();
		String sql = "select * from user";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
						rs.getString("email"));
				listUser.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listUser;
	}

	public Message getMess(int id) {
		Message mess = new Message();
		Connection connect = ConnectDB.getConnection();
		String sql = "SELECT * FROM message WHERE id = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				mess = new Message(rs.getInt("id"), rs.getInt("id_sender"), rs.getInt("id_receiver"),
						rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mess;
	}

	public int deleteMess(int id) {
		int result = 0;
		Connection connect = ConnectDB.getConnection();
		String sql = "DELETE from message where id=?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			result = pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ArrayList<Message> searchMessage(String text, int id) {
		ArrayList<Message> list_mess = new ArrayList<Message>();
		ArrayList<User> list_user = searchUser(text);
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < list_user.size(); i++) {
			ids.add(list_user.get(i).getid());
		}
		String list_id = ids.toString();
		String end = "";
		for (int i = 1; i < list_id.length()-1; i++) {
			end = end + list_id.charAt(i);
		}
		Connection connect = ConnectDB.getConnection();
		if (list_user.size() == 0) {
			try {
				String sql = "select * from message where id_receiver = " + id + " and title like \'%" + text
						+ "%\' order by id desc";
				PreparedStatement pst = connect.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					Message mess = new Message(rs.getInt("id"), rs.getInt("id_sender"), rs.getInt("id_receiver"),
							rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
					list_mess.add(mess);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				String sql = "select * from message where id_receiver = " + id + " and id_sender in (" + end
						+ ") or id_receiver = " + id + " and title like \'%" + text + "%\' order by id desc";
				System.out.print("ddddd" + sql);
				PreparedStatement pst = connect.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					Message mess = new Message(rs.getInt("id"), rs.getInt("id_sender"), rs.getInt("id_receiver"),
							rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
					list_mess.add(mess);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
//		ArrayList<Message> list = new ArrayList<Message>();
//		for (int i = 0; i < list_mess.size(); i++) {
//			for (int j = i + 1; j < list_mess.size(); j++) {
//				if(list_mess.get(i).getid()!=list_mess.get(j).getid()) {
//					list.add(list_mess.get(i));
//				}
//			}
//		}
		return list_mess;
	}

	public ArrayList<User> searchUser(String text) {
		ArrayList<User> list = new ArrayList<User>();
		Connection connect = ConnectDB.getConnection();
		String sql = "select * from user where username like \'%" + text + "%\'";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
						rs.getString("email"));
				list.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.print(list.toString());
		return list;
	}
}