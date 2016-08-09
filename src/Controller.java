
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class Controller {
	private String fid = getAllFile("config/fid.txt");
	private String session = getAllFile("config/session.txt");
	private DataSet data = new DataSet(getAllFile("config/data.txt"));
	private String threadInfo = getAllFile("config/threads.txt");

	public String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Cookie", session);
            // 建立实际的连接
            connection.connect();
            
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"GBK"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
	
	public String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Cookie", session);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryqVqtFrpj2wyX6aOR");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "GBK"));
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "GBK"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

	public boolean writeToForum(String fid, String tid, String pid, String page, String msg) {
		String result1 = sendGet("http://bbs.pcbeta.com/forum.php?mod=post&action=edit&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=" + page);
		
		int index1 = result1.indexOf("<input type=\"hidden\" name=\"formhash\" id=\"formhash\" value=\"");
		index1 += "<input type=\"hidden\" name=\"formhash\" id=\"formhash\" value=\"".length();
		int index2 = index1;
		while (result1.charAt(index2) != '\"')
			index2++;
		String formhash = result1.substring(index1, index2);
		
		index1 = result1.indexOf("<input type=\"hidden\" name=\"posttime\" id=\"posttime\" value=\"");
		index1 += "<input type=\"hidden\" name=\"posttime\" id=\"posttime\" value=\"".length();
		index2 = index1;
		while (result1.charAt(index2) != '\"')
			index2++;
		String posttime = result1.substring(index1, index2);
		
		String result2 = sendPost("http://bbs.pcbeta.com/forum.php?mod=post&action=edit&extra=&editsubmit=yes", 
				getPostString(formhash, posttime, fid, tid, pid, page, msg));
		if (result2.contains("帖子编辑成功"))
			return true;
		return false;
	}
	
	private String getPostString(String formhash, String posttime, String fid, String tid, String pid, String page, String msg) {
		StringBuilder string = new StringBuilder();
		String boundary = "------WebKitFormBoundaryqVqtFrpj2wyX6aOR\r\n";
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"formhash\"\r\n\r\n" + formhash + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"posttime\"\r\n\r\n" + posttime + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"delattachop\"\r\n\r\n" + 0 + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"wysiwyg\"\r\n\r\n" + 1 + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"fid\"\r\n\r\n" + fid + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"tid\"\r\n\r\n" + tid + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"pid\"\r\n\r\n" + pid + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"page\"\r\n\r\n" + 1 + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"subject\"\r\n\r\n" + "" + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n" + msg + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"editsubmit\"\r\n\r\n" + true + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"save\"\r\n\r\n" + 0 + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"uploadalbum\"\r\n\r\n" + 2043 + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"newalbum\"\r\n\r\n" + "" + "\r\n");
		string.append(boundary);
		string.append("Content-Disposition: form-data; name=\"usesig\"\r\n\r\n" + 1 + "\r\n");
		return string.toString();
	}
	
	private String getTableString(String s) {
		String result = "";
		String[] sp1 = s.split("<table summary=\"forum_" + fid + "\".*?>");
		if (sp1.length >= 2) {
			String[] sp2 = sp1[1].split("</table>");
			result = sp2[0];
		}
		return result;
	}
	
	private Vector<String> getTagString(String s, String tag) {
		Vector<String> result = new Vector<>();
		String temp = s.trim();
		while (temp.length() > 0) {
			int startIndex = temp.indexOf("<" + tag);
			int endIndex = temp.indexOf("</" + tag + ">");
			if (startIndex >= 0 && endIndex >= 0) {
				result.add(temp.substring(startIndex, endIndex + tag.length() + 3));
				temp = temp.substring(endIndex + tag.length() + 3);
			}
			else
				break;
		}
		return result;
	}
	
	private Vector<ForumThread> getThreads(String tableString) {
		Vector<ForumThread> result = new Vector<>();
		Vector<String> t = getTagString(tableString, "tbody");
		for (String x : t) {
			String tid;
			int tidFrom = x.indexOf("normalthread_"), tidTo;
			if (-1 == tidFrom)
				continue;
			tidFrom += 13;
			tidTo = tidFrom;
			while (Character.isDigit(x.charAt(tidTo)))
				tidTo++;
			tid = x.substring(tidFrom, tidTo);
			
			String title;
			int titleFrom = x.indexOf("</em>") + 5, titleTo, index;
			while (x.charAt(titleFrom) != '>')
				titleFrom++;
			titleFrom++;
			titleTo = titleFrom;
			while (x.charAt(titleTo) != '<')
				titleTo++;
			title = x.substring(titleFrom, titleTo);
			
			index = x.indexOf("<em>[<a");
			if (index != -1) {
				Vector<String> ttt = getTagString(x.substring(index), "a");
				title = "[" + ttt.get(0).replaceAll("<(/){0,1}a.*?>", "").replaceAll("<(/){0,1}font.*?>", "") + "]" + title;
			}
			
			String uid, author, time;
			int index1 = x.indexOf("<cite>") + 6, index2;
			index2 = index1;
			while (!Character.isDigit(x.charAt(index1)))
				index1++;
			index2 = index1;
			while (Character.isDigit(x.charAt(index2)))
				index2++;
			uid = x.substring(index1, index2);
			
			index1 = index2;
			while (x.charAt(index1) != '>')
				index1++;
			index1++;
			index2 = index1;
			while (x.charAt(index2) != '<')
				index2++;
			author = x.substring(index1, index2);
			
			index1 = x.indexOf("<em><span");
			Vector<String> tt = getTagString(x.substring(index1), "span");
			time = tt.get(0).replaceAll("<.*?span.*?>", "");
			
			ForumThread thread = new ForumThread();
			thread.setTid(tid);
			thread.setTitle(title);
			thread.setUid(uid);
			thread.setAuthor(author);
			thread.setCreateTime(time);
			
			result.add(thread);
		}
		return result;
	}
	
	public Vector<ForumThread> getData(int i) {
		String s = sendGet("http://bbs.pcbeta.com/forum.php?mod=forumdisplay&fid=" + fid + "&orderby=dateline&filter=author&orderby=dateline&page=" + i);
		String tableString = getTableString(s);
		Vector<ForumThread> threads = getThreads(tableString);
		return threads;
	}
	
	public HashMap<String, Integer> getRegex() {
		HashMap<String, Integer> result = new HashMap<>();
		File f = new File("config/value.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String s = ".";
			while (true) {
				s = reader.readLine();
				if (s == null)
					break;
				s = s.trim();
				String[] sp = s.split("=");
				if (sp.length > 1)
					result.put(sp[0].toLowerCase(), Integer.parseInt(sp[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public DataSet getDataSet() {
		return data;
	}

	public boolean writeLocalDataSet() {
		String string = data.toFileString();
		boolean flag = true;
		try {
			File file = new File("config/data.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(string);
			writer.close();
			
			SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
			File file2 = new File("config/data" + sdf.format(new Date())+ ".txt");
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
			writer2.write(string);
			writer2.close();
			
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	
	public String getAllFile(String filepath) {
		String result = "";
		File f = new File(filepath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String s = ".";
			while (true) {
				s = reader.readLine();
				if (s == null)
					break;
				s = s.trim();
				result += s + " ";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result.trim();
	}

	public String getThreadInfo() {
		return threadInfo;
	}
}
