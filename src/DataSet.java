import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataSet {
	private String lastUpdateTime;
	private String lastUpdateThread;
	private HashMap<String, ArrayList<ForumThread>> data;
	public static final String[] TITLE = {" ", "Acer 宏碁", "Asus 华硕", "Dell 戴尔", "Hasee 神舟", "HP 惠普", "Lenovo 联想", "MSI 微星", "Samsung 三星", "Sony 索尼", "Toshiba 东芝", "Others 其它"};
	
	public DataSet(String jsonStr) {
		data = new HashMap<>();
		try {
			JSONObject root = new JSONObject(jsonStr);
			lastUpdateTime = root.getString("lastUpdateTime");
			lastUpdateThread = root.getString("lastUpdateThread");
			JSONObject d = root.getJSONObject("data");
			for (@SuppressWarnings("unchecked")
			Iterator<String> iterator = d.keys(); iterator.hasNext(); ) {
				String key = iterator.next();
				JSONArray array = d.getJSONArray(key);
				ArrayList<ForumThread> list = new ArrayList<>();
				for (int i = 0; i < array.length(); i++)
					list.add(new ForumThread(array.getJSONObject(i)));
				data.put(key, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*for (String key : data.keySet()) {
			System.out.println(key);
			for (ForumThread t : data.get(key))
				System.out.println("tid: " + t.getTid() + " uid: " + t.getUid());
		}*/
	}

	public String toFileString() {
		ArrayList<Map.Entry<String, ArrayList<ForumThread>>> orderedList = new ArrayList<>(data.entrySet());
		Collections.sort(orderedList, new Comparator<Map.Entry<String, ArrayList<ForumThread>>>() {   
		    public int compare(Map.Entry<String, ArrayList<ForumThread>> o1, Map.Entry<String, ArrayList<ForumThread>> o2) {      
		        return (o1.getKey()).toString().compareTo(o2.getKey());
		    }
		}); 
		try {
			JSONObject root = new JSONObject();
			root.put("lastUpdateTime", lastUpdateTime);
			root.put("lastUpdateThread", lastUpdateThread);
			JSONObject data = new JSONObject();
			for (Entry<String, ArrayList<ForumThread>> entry : orderedList) {
				JSONArray array = new JSONArray();
				for (ForumThread t : entry.getValue())
					array.put(t.toJsonObject());
				data.put(entry.getKey(), array);
			}
			root.put("data", data);
			String s = root.toString(4);
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public ArrayList<Entry<String, String>> toForumDoc() {
		HashMap<String, String> res = new HashMap<>();
		for (Entry<String, ArrayList<ForumThread>> entry : data.entrySet()) {
			ArrayList<ForumThread> array = entry.getValue();
			int i = 0;
			for (i = UserInterface.LAPTOP_TYPE.length - 1; i > 0; i--)
				if (UserInterface.LAPTOP_TYPE[i].equals(entry.getKey()))
					break;
			StringBuilder stringBuilder = new StringBuilder("[size=5][color=#ff0000]" + TITLE[i] + "[/color][/size]\r\n");
			for (ForumThread t : array)
				stringBuilder.append(t.toForumDoc());
			res.put(entry.getKey(), stringBuilder.toString());
		}
		ArrayList<Entry<String, String>> r = new ArrayList<>(res.entrySet());
		Collections.sort(r, new Comparator<Entry<String, String>>() {

			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		return r;
	}
	
	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public String getLastUpdateThread() {
		return lastUpdateThread;
	}

	
	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public void setLastUpdateThread(String lastUpdateThread) {
		this.lastUpdateThread = lastUpdateThread;
	}

	public HashMap<String, ArrayList<ForumThread>> getData() {
		return data;
	}
}
