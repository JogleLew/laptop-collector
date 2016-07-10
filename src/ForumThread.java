
public class ForumThread {
	private String tid;
	private String title;
	private String uid;
	private String author;
	private String createTime;
	
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
		return title + "\n" + author + " | http://bbs.pcbeta.com/viewthread-" + tid + "-1-1.html\n\n";
	}
}
