import org.json.JSONException;
import org.json.JSONObject;

public class ForumThread {
	private String tid;
	private String title;
	private String uid;
	private String author;
	private String createTime;
	
	public ForumThread() {
		tid = "";
		title = "";
		uid = "";
		author = "";
		createTime = "";
	}
	
	public ForumThread(JSONObject object) {
		try {
			tid = object.getString("tid");
			title = object.getString("title");
			uid = object.getString("uid");
			author = object.getString("author");
			createTime = object.getString("createTime");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJsonObject() {
		try {
			JSONObject object = new JSONObject();
			object.put("tid", tid);
			object.put("title", title);
			object.put("uid", uid);
			object.put("author", author);
			object.put("createTime", createTime);
			return object;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String toForumDoc() {
		String string = "[url=http://bbs.pcbeta.com/viewthread-" + tid + "-1-1.html]" + title + "[/url]\r\n"
				+ "作者: [url=http://i.pcbeta.com/?" + uid + "]" + author + "[/url] | " + createTime + "\r\n\r\n";
		return string;
	}
	
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String toString() {
		return title + System.getProperty("line.separator") + author + " | http://bbs.pcbeta.com/viewthread-" + tid + "-1-1.html" + System.getProperty("line.separator") + System.getProperty("line.separator");
	}
}
