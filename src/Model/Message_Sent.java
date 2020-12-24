package Model;

public class Message_Sent {
	private int id;
	private int id_sender;
	private String receivers;
	private String title;
	private String content;
	private String create_at;	
	
	public Message_Sent() {
	
	}
	
	public Message_Sent(int id, int id_sender, String receivers, String title, String content, String create_ad) {
		this.id = id;
		this.id_sender = id_sender;
		this.receivers = receivers;
		this.title = title;
		this.content = content;
		this.create_at = create_ad;
	}
	
	public int getid() {
		return id;
	}
	
	public void setid(int id) {
		this.id = id;
	}
	
	public int getid_sender() {
		return id_sender;
	}
	
	public void setid_sender(int id_sender) {
		this.id_sender = id_sender;
	}
	
	public String getreceivers() {
		return receivers;
	}
	
	public void setreceivers(String receivers) {
		this.receivers = receivers;
	}
	
	public String gettitle() {
		return title;
	}
	
	public void settitle(String title) {
		this.title = title;
	}
	
	public String getcontent() {
		return content;
	}
	
	public void setcontent(String content) {
		this.content = content;
	}
	
	public String getcreate_at() {
		return create_at;
	}
	
	public void setcreate_at(String create_at) {
		this.create_at = create_at;
	}
}
