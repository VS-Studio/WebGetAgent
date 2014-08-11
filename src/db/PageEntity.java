package db;

public class PageEntity {
	
	public String url;
	public String url_hash;
	public String last_modified_time;
	public String create_time;
	public String update_time;
	public String last_fetch_time;
	public String last_visit_time;
	public byte[] content;
	public String headers;
	
	@Override
	public String toString() {
		return "PageEntry [url=" + url + ", url_hash=" + url_hash
				+ ", last_modifyed_time=" + last_modified_time
				+ ", create_time=" + create_time + ", update_time="
				+ update_time + ", last_fetch_time=" + last_fetch_time
				+ ", last_visit_time=" + last_visit_time + ", content="
				+ content + "]";
	}

	
}
