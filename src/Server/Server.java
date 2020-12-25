package Server;

import java.util.List;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;

import DB.ConnectDB;
import Model.Attachment;
import Model.Attachment_Sent;
import Model.Message;
import Model.Message_Sent;
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
				case "forget_Password":
					String email1 = request.get("email");
					String pass1 = request.get("password");
					User user11 = UpdatePassword(email1, pass1);
					if (user11 == null) {
						response.put("status", "fail");
					} else {
						response.put("status", "success");
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
				case "show_listMessSent":
					int idSent = Integer.parseInt(request.get("id"));
					ArrayList<Message_Sent> listmessSent = getAllMessSent(idSent);
					String list1 = gson.toJson(listmessSent);
					if (listmessSent != null) {
						response.put("status", "success");
						response.put("id", String.valueOf(idSent));
						response.put("show_listmess", list1);
					} else {
						response.put("status", "fail");
					}
					break;
				case "show_Mess":
					int mess_id = Integer.parseInt(request.get("id"));
					Message mess = getMess(mess_id);
					String messString = gson.toJson(mess);
					response.put("status", "success");
					response.put("show_Mess", messString);
					break;
				case "show_MessSent":
					int messSent_id = Integer.parseInt(request.get("id"));
					Message_Sent mess1 = getMessSent(messSent_id);
					String messString1 = gson.toJson(mess1);
					response.put("status", "success");
					response.put("show_Mess", messString1);
					break;
				case "show_Attachment":
					int idMess = Integer.parseInt(request.get("id_mess"));
					ArrayList<Attachment> atm = getAttachment(idMess);
					String attachmentString = gson.toJson(atm);
					if (atm != null) {
						response.put("status", "success");
						response.put("show_Attachment", attachmentString);
					} else {
						response.put("status", "fail");
					}
					break;
				case "show_AttachmentSent":
					int idMess1 = Integer.parseInt(request.get("id_mess"));
					ArrayList<Attachment_Sent> atm1 = getAttachmentSent(idMess1);
					String attachmentString1 = gson.toJson(atm1);
					if (atm1 != null) {
						response.put("status", "success");
						response.put("show_Attachment", attachmentString1);
					} else {
						response.put("status", "fail");
					}
					break;
				case "download_Attachment":
					int id_down = Integer.parseInt(request.get("id_down"));
					Attachment down = downloadAttachment(id_down);
					String attachment_down = gson.toJson(down);
					if (down != null) {
						response.put("status", "success");
						response.put("attachment_down", attachment_down);
					} else {
						response.put("status", "fail");
					}
					break;
				case "insert_mess":
					int id_sender = Integer.parseInt(request.get("id_sender"));
					String namereceiver =(String)request.get("receiver");
					String[] receivers = namereceiver.split(",");
					String title = request.get("title");
					String content = request.get("content");
					String create_at = request.get("create_at");
					Message message = new Message();
					message.setid_sender(id_sender);
					message.settitle(title);
					message.setcontent(content);
					message.setcreate_at(create_at);
					String status = ""; 
					String receiver = ""; 
					String idmess = "";
					for (String string : receivers) {
						int id_receiver = getIDbyUsername(string.trim());
						message.setid_receiver(id_receiver);
						System.out.println("Add " + message.getid_sender() + message.getid_receiver() + message.gettitle()
								+ message.getcontent() + message.getcreate_at());
						int idm = addMess(message);
						if ( idm == -1) {
							status+="fail,";
							receiver+=string+",";
						} else {
							status+="success,";
							receiver+=string+",";
							idmess+=String.valueOf(idm)+",";
						}
					}
					status=status.substring(0, status.length()-1);
					receiver=receiver.substring(0, receiver.length()-1);
					if (!idmess.equals("")) {
						idmess=idmess.substring(0, idmess.length()-1);
					}
					
					Message_Sent message_Sent = new Message_Sent();
					message_Sent.setid_sender(id_sender);
					message_Sent.settitle(title);
					message_Sent.setcontent(content);
					message_Sent.setcreate_at(create_at);
					message_Sent.setreceivers(receiver);
					String idmesssent = String.valueOf(addMessSent(message_Sent));
					
					response.put("status", status);
					response.put("receiver", receiver);
					response.put("id_mess",idmess);
					response.put("id_mess_sent", idmesssent);
					break;
				case "insert_attachment":
					String id_meessString = (String) request.get("id_mess");
					String id_mess_sent = id_meessString.substring(id_meessString.lastIndexOf(",")+1);
					System.out.println(id_mess_sent);
					String id_mess = id_meessString.substring(0,id_meessString.lastIndexOf(","));
					System.out.println(id_mess);
					String[] id_messs = id_mess.split(",");
					int[] id_mess1 = new int[id_messs.length];
					for (int i = 0; i < id_mess1.length; i++) {
						id_mess1[i]=Integer.parseInt(id_messs[i]);
					}
					String file_name = (String) request.get("file_name");
					String file_data = (String) request.get("file_data");
					Attachment_Sent attachment_sent = new Attachment_Sent();
					attachment_sent.setfile_data(file_data);
					attachment_sent.setfile_name(file_name);
					attachment_sent.setid_mess(Integer.parseInt(id_mess_sent));
					Attachment attachment = new Attachment();
					attachment.setfile_name(file_name);
					attachment.setfile_data(file_data);
					for (int i : id_mess1) {
						attachment.setid_mess(i);
						System.out.println(
								"Add " + attachment.getid_mess() + attachment.getfile_name() + attachment.getfile_data());
						if (addAttachment(attachment) == false) {
							response.put("status", "fail");
						} else {
							response.put("status", "success");
//							response.put("id_mess", String.valueOf(id_mess));
//							response.put("file_name", file_name);
//							response.put("file_data", file_data);
						}
					}
					addAttachmentSent(attachment_sent);
					break;
				case "delete_Mess":
					int Id = Integer.parseInt(request.get("id"));
					if (deleteMess(Id) > 0) {
						response.put("status", "success");
					} else {
						response.put("status", "fail");
					}
					break;
				case "delete_MessSent":
					int Id1 = Integer.parseInt(request.get("id"));
					if (deleteMessSent(Id1) > 0) {
						response.put("status", "success");
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
				case "search_sent":
					String text1 = (String) request.get("text");
					int id_search1 = Integer.parseInt(request.get("id"));
					ArrayList<Message_Sent> list_search_sent = searchMessageSent(text1, id_search1);
					String search1 = gson.toJson(list_search_sent);
					if (list_search_sent != null) {
						response.put("status", "success");
						response.put("id", String.valueOf(id_search1));
						response.put("search", search1);
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
				case "valid_Email":
					String emaiString = (String)request.get("email");
					int valid = validEmail(emaiString);
					if (valid==1) {
						response.put("status", "available");
					}
					else if (valid==0) {
						response.put("status", "unavailable");
					}
					else {
						response.put("status", "error");
					}
					break;
				case "valid_Acc":
					String emailString = (String)request.get("email");
					String userString = (String)request.get("username");
					int valid1 = validAcc(emailString, userString);
					if (valid1==0) {
						response.put("status", "unavailable");
					}
					else if (valid1==1) {
						response.put("status", "userexist");
					}
					else if (valid1==2) {
						response.put("status", "emailexist");
					}
					else {
						response.put("status", "error");
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

	public User UpdatePassword(String email, String Pass) {
		Connection connect = ConnectDB.getConnection();
		String sql1 = "select * from user where email=?";
		String sql2 = "update user set password = ? where email = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql2);
			pst.setString(1, Pass);
			pst.setString(2, email);
			System.out.println(pst.toString());
			if (pst.executeUpdate() == 1) {
				PreparedStatement pst1 = connect.prepareStatement(sql1);
				pst1.setString(1, email);
				ResultSet rs = pst1.executeQuery();
				if (rs.next()) {
					User user = new User();
					user.setid(rs.getInt("id"));
					user.setemail(rs.getString("email"));
					user.setusername(rs.getString("username"));
					user.setpassword(rs.getString("password"));
					connect.close();
					return user;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return null;
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
	
	public ArrayList<Message_Sent> getAllMessSent(int id) {
		ArrayList<Message_Sent> listmess = new ArrayList<>();
		Connection connect = ConnectDB.getConnection();
		String sql = "select * from message_sent where id_sender = ? order by id desc";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Message_Sent mess = new Message_Sent(rs.getInt("id"), rs.getInt("id_sender"), rs.getString("receivers"),
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
		if(result.next())
			return result.getInt("id");
		else return -1;
	}

	public int addMess(Message message) throws SQLException {
		Connection connect = ConnectDB.getConnection();
		if (message.getid_receiver()==-1) {
			return -1;
		}
		else {
			String sql = "insert into message (id_sender, id_receiver, title, content, create_at) values(?,?,?,?,?)";
			PreparedStatement ps = connect.prepareCall(sql);
			ps.setInt(1, message.getid_sender());
			ps.setInt(2, message.getid_receiver());
			ps.setString(3, message.gettitle());
			ps.setString(4, message.getcontent());
			ps.setString(5, message.getcreate_at());
			if(ps.executeUpdate()==0) {
				return -1;
			}
			else {
				return getMaxIdMess();
			}
		}
	}
	
	public int addMessSent(Message_Sent message_sent) throws SQLException {
		Connection connect = ConnectDB.getConnection();
		String sql = "insert into message_sent (id_sender, receivers, title, content, create_at) values(?,?,?,?,?)";
		PreparedStatement ps = connect.prepareCall(sql);
		ps.setInt(1, message_sent.getid_sender());
		ps.setString(2, message_sent.getreceivers());
		ps.setString(3, message_sent.gettitle());
		ps.setString(4, message_sent.getcontent());
		ps.setString(5, message_sent.getcreate_at());
		if(ps.executeUpdate()==0) {
			return -1;
		}
		else {
			return getMaxIdMessSent();
		}
	}

	public boolean addAttachment(Attachment attachment) throws SQLException {
		Connection connect = ConnectDB.getConnection();
		String sql = "Insert into attachment(id_mess,file_name,file_data) " + " values (?,?,?) ";
		boolean check = false;
		try {
			ByteArrayInputStream file_data = new ByteArrayInputStream((attachment.getfile_data()).getBytes());
			PreparedStatement pstm = connect.prepareStatement(sql);
			pstm.setInt(1, attachment.getid_mess());
			pstm.setString(2, attachment.getfile_name());
			pstm.setBlob(3, file_data);
			pstm.executeUpdate();
			check = true;
		} catch (SQLException e) {
			e.printStackTrace();
			check = false;
		}
		return check;
	}
	
	public boolean addAttachmentSent(Attachment_Sent attachment_sent) throws SQLException {
		Connection connect = ConnectDB.getConnection();
		String sql = "Insert into attachment_sent (id_mess,file_name,file_data) " + " values (?,?,?) ";
		boolean check = false;
		try {
			ByteArrayInputStream file_data = new ByteArrayInputStream((attachment_sent.getfile_data()).getBytes());
			PreparedStatement pstm = connect.prepareStatement(sql);
			pstm.setInt(1, attachment_sent.getid_mess());
			pstm.setString(2, attachment_sent.getfile_name());
			pstm.setBlob(3, file_data);
			if (pstm.executeUpdate()==1) {
				check = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			check = false;
		}
		return check;
	}

	public int getMaxIdMess() throws SQLException {
		Connection connect = ConnectDB.getConnection();
		String sql = "Select max(a.id) from message a";
		PreparedStatement pstm = connect.prepareStatement(sql);
		ResultSet rs = pstm.executeQuery();
		if (rs.next()) {
			int max = rs.getInt(1);
			return max;
		}
		return 0;
	}
	
	public int getMaxIdMessSent() throws SQLException {
		Connection connect = ConnectDB.getConnection();
		String sql = "Select max(a.id) from message_sent a";
		PreparedStatement pstm = connect.prepareStatement(sql);
		ResultSet rs = pstm.executeQuery();
		if (rs.next()) {
			int max = rs.getInt(1);
			return max;
		}
		return 0;
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
	
	public Message_Sent getMessSent(int id) {
		Message_Sent mess_sent = new Message_Sent();
		Connection connect = ConnectDB.getConnection();
		String sql = "SELECT * FROM message_sent WHERE id = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				mess_sent = new Message_Sent(rs.getInt("id"), rs.getInt("id_sender"), rs.getString("receivers"),
						rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mess_sent;
	}

	public ArrayList<Attachment> getAttachment(int id_mess) throws IOException {
		ArrayList<Attachment> data = new ArrayList<Attachment>();
		Attachment attachment = new Attachment();
		Connection connect = ConnectDB.getConnection();
		String sql = "SELECT * FROM attachment WHERE id_mess = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id_mess);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Blob blob = rs.getBlob("file_data");
				InputStream inputStream = blob.getBinaryStream();
				byte[] in_byte = inputStream.readAllBytes();
				String file_data = in_byte.toString();
				attachment = new Attachment(rs.getInt("id"), rs.getInt("id_mess"), rs.getString("file_name"),
						file_data);
				data.add(attachment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public ArrayList<Attachment_Sent> getAttachmentSent(int id_mess) throws IOException {
		ArrayList<Attachment_Sent> data = new ArrayList<Attachment_Sent>();
		Attachment_Sent attachment_sent = new Attachment_Sent();
		Connection connect = ConnectDB.getConnection();
		String sql = "SELECT * FROM attachment_sent WHERE id_mess = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id_mess);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Blob blob = rs.getBlob("file_data");
				InputStream inputStream = blob.getBinaryStream();
				byte[] in_byte = inputStream.readAllBytes();
				String file_data = in_byte.toString();
				attachment_sent = new Attachment_Sent(rs.getInt("id"), rs.getInt("id_mess"), rs.getString("file_name"),
						file_data);
				data.add(attachment_sent);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public Attachment downloadAttachment(int id) throws IOException {
		Attachment attachment = new Attachment();
		Connection connect = ConnectDB.getConnection();
		String sql = "SELECT * FROM attachment WHERE id = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				Blob blob = rs.getBlob("file_data");
				InputStream inputStream = blob.getBinaryStream();
				byte[] in_byte = inputStream.readAllBytes();
				String file_data = in_byte.toString();
				attachment = new Attachment(rs.getInt("id"), rs.getInt("id_mess"), rs.getString("file_name"),
						file_data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return attachment;
	}
	
	public Attachment_Sent downloadAttachmentSent(int id) throws IOException {
		Attachment_Sent attachment_sent = new Attachment_Sent();
		Connection connect = ConnectDB.getConnection();
		String sql = "SELECT * FROM attachment_sent WHERE id = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				Blob blob = rs.getBlob("file_data");
				InputStream inputStream = blob.getBinaryStream();
				byte[] in_byte = inputStream.readAllBytes();
				String file_data = in_byte.toString();
				attachment_sent = new Attachment_Sent(rs.getInt("id"), rs.getInt("id_mess"), rs.getString("file_name"),
						file_data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return attachment_sent;
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
	
	public int deleteMessSent(int id) {
		int result = 0;
		Connection connect = ConnectDB.getConnection();
		String sql = "DELETE from message_sent where id=?";
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
		for (int i = 1; i < list_id.length() - 1; i++) {
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
		return list_mess;
	}
	
	public ArrayList<Message_Sent> searchMessageSent(String text, int id) {
		ArrayList<Message_Sent> list_mess_sent = new ArrayList<Message_Sent>();
		ArrayList<User> list_user = searchUser(text);
		Connection connect = ConnectDB.getConnection();
		if (list_user.size() == 0) {
			try {
				String sql = "select * from message_sent where id_sender = " + id + " and title like \'%" + text
						+ "%\' order by id desc";
				PreparedStatement pst = connect.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					Message_Sent mess_sent = new Message_Sent(rs.getInt("id"), rs.getInt("id_sender"), rs.getString("receivers"),
							rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
					list_mess_sent.add(mess_sent);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				String sql = "select * from message_sent where id_sender = " + id + " and receivers like \'%" + text
						+ "%\' or id_sender = " + id + " and title like \'%" + text + "%\' order by id desc";
				System.out.print("ddddd" + sql);
				PreparedStatement pst = connect.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					Message_Sent mess_sent = new Message_Sent(rs.getInt("id"), rs.getInt("id_sender"), rs.getString("receivers"),
							rs.getString("title"), rs.getString("content"), rs.getString("create_at"));
					list_mess_sent.add(mess_sent);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list_mess_sent;
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
	
	public int validEmail(String email) {
		Connection connect = ConnectDB.getConnection();
		String sql = "select email from user where email =?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setString(1, email);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				return 1;
			}
			else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int validAcc(String email, String username) {
		Connection connect = ConnectDB.getConnection();
		String sql = "select username,email from user where email = ? or username = ?";
		try {
			PreparedStatement pst = connect.prepareStatement(sql);
			pst.setString(1, email);
			pst.setString(2, username);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				if (rs.getString("username").equals(username)) {
					return 1;
				}
				else {
					return 2;
				}
			}
			else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}