
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;
import javax.security.auth.login.Configuration;
import javax.swing.SortingFocusTraversalPolicy;

public class Controller {

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
            connection.setRequestProperty("Cookie", 
            		"Hm_lpvt_76c941eab16e9b48cd0fb4a6d9482a4f=1467805787; "
            		+ "Hm_lvt_76c941eab16e9b48cd0fb4a6d9482a4f=1467776841,1467781215,1467791096,1467804692; "
            		+ "__cfduid=d3ab7c1111aaeddf14a5110ad1eab8f861440588965; "
            		+ "_ga=GA1.2.1543143282.141716; "
            		+ "_gat=1; "
            		+ "jqCP_887f_auth=f3ecAdBIdteaTLwxukjkFtM2Z7ACqRaOIdtqKZHqAG3EMR424GxYlFwOH%2BkVkwVC52GYz0RnEgWhoFPba9lZEE2R0bk; "
            		+ "jqCP_887f_checkpm=1; "
            		+ "jqCP_887f_forum_lastvisit=D_557_1467781222D_554_1467805786; "
            		+ "jqCP_887f_home_readfeed=1466172476; "
            		+ "jqCP_887f_lastact=1467805786%09home.php%09spacecp; "
            		+ "jqCP_887f_lastvisit=1467801433; "
            		+ "jqCP_887f_mobile=no; "
            		+ "jqCP_887f_mrd=%09; "
            		+ "jqCP_887f_pvi=1803909953; "
            		+ "jqCP_887f_saltkey=cDSNc1TC; "
            		+ "jqCP_887f_sendmail=1; "
            		+ "jqCP_887f_sid=kjmCR2; "
            		+ "jqCP_887f_smile=5D1; "
            		+ "jqCP_887f_ulastactivity=9eb6aWctbWnlOQhMbVLSxs4ndu45BnhtXcX1UAvIDWLvkE972pNe; "
            		+ "jqCP_887f_uuid=723618; "
            		+ "jqCP_887f_zantip=1; "
            		+ "pgv_info=ssi=s9382113800; "
            		+ "pgv_pvi=9878400950"
            );
            // 建立实际的连接
            connection.connect();
            
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"GB2312"));
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
	
	private String getTableString(String s) {
		String result = "";
		String[] sp1 = s.split("<table summary=\"forum_554\".*?>");
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
		long nowTime = new Date().getTime();
		String s = sendGet("http://bbs.pcbeta.com/forum.php?mod=forumdisplay&fid=554&orderby=dateline&filter=author&orderby=dateline&page=" + i);
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
}
