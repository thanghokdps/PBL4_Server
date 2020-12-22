package Model;

public class Message {
	private int id;
	private int id_sender;
	private int id_receiver;
	private String title;
	private String content;
	private String create_at;

	public Message() {

	}

	public Message(int id, int id_sender, int id_receiver, String title, String content, String create_ad) {
		this.id = id;
		this.id_sender = id_sender;
		this.id_receiver = id_receiver;
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

	public int getid_receiver() {
		return id_receiver;
	}

	public void setid_receiver(int id_receiver) {
		this.id_receiver = id_receiver;
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
