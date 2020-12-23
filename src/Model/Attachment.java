package Model;

public class Attachment {
	private int id;
	private int id_mess;
	private String file_name;
	private String file_data;

	public Attachment() {

	}

	public Attachment(int id, int id_mess, String file_name, String file_data) {
		this.id = id;
		this.id_mess = id_mess;
		this.file_name = file_name;
		this.file_data = file_data;
	}

	public int getid() {
		return id;
	}

	public void setid(int id) {
		this.id = id;
	}

	public int getid_mess() {
		return id_mess;
	}

	public void setid_mess(int id_mess) {
		this.id_mess = id_mess;
	}

	public String getfile_name() {
		return file_name;
	}

	public void setfile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getfile_data() {
		return file_data;
	}

	public void setfile_data(String file_data) {
		this.file_data = file_data;
	}
}
