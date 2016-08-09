
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.net.URI;
import java.text.SimpleDateFormat;

public class UserInterface extends JFrame {
	private static final long serialVersionUID = 1L;
	public static final String[] LAPTOP_TYPE = {" ", "Acer", "Asus", "Dell", "Hasee", "HP", "Lenovo", "MSI", "Samsung", "Sony", "Toshiba", "Others"};
	private JTextField lastUpdatedTime;
	private JButton updateButton;
	private JButton generateButton;
	private JButton writeButton;
	private JTable threadTable;
	private DefaultTableModel tableModel;
	private Controller controller;
	private Vector<ForumThread> dataAll;
	private HashMap<String, Integer> regexes;
	
	private String lastUpdateThread;
	private String currentUpdateThread;
	private String currentUpdateTime;
	
	public class BackgroundThread extends Thread {
		private long lastTime;
		
		public BackgroundThread(long lastTime) {
			this.lastTime = lastTime;
		}
		
		@Override
		public void run() {
			int j = 0;
			boolean flag = true;
			currentUpdateThread = "";
			Date nowdate = new Date();
			currentUpdateTime = (nowdate.getYear() + 1900) + "-" + (nowdate.getMonth() + 1) + "-" + nowdate.getDate();
			dataAll = new Vector<>();
			while (flag) {
				j++;
				Vector<Vector<Object>> data = new Vector<>();
				Vector<ForumThread> threads = controller.getData(j);
				for (ForumThread thread : threads) {
					Vector<Object> v = new Vector<>();
					v.add(thread.getTitle());
					v.add(thread.getAuthor());
					v.add(thread.getCreateTime());
					data.add(v);
				}
				for (int i = 0; i < data.size(); i++) {
					if (threads.get(i).getTid().equals(lastUpdateThread)) {
						flag = false;
						break;
					}
					String[] sp = ((String) data.get(i).get(2)).split("-");
					@SuppressWarnings("deprecation")
					Date date = new Date(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]) - 1, Integer.parseInt(sp[2]), 0, 0);
					if (date.getTime() >= lastTime) {
						int value = 0;
						for (String regex : regexes.keySet()) {
							Pattern pattern = Pattern.compile(regex);
							Matcher matcher = pattern.matcher(((String) data.get(i).get(0)).toLowerCase());
							if (matcher.find())
								value += regexes.get(regex);
						}
						if (value > 0) {
							tableModel.addRow(data.get(i));
							dataAll.add(threads.get(i));
							if (currentUpdateThread == null || currentUpdateThread.length() == 0)
								currentUpdateThread = threads.get(i).getTid();
						}
					}
					else {
						flag = false;
						break;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			SwingUtilities.invokeLater(new Runnable(){  
				@Override  
				public void run() {
					updateButton.setText("重新抓取");
					updateButton.setEnabled(true); 
				}  
			});  
		}
	}
	
	public UserInterface() {
		setLayout(null);
		JLabel text1 = new JLabel();
		text1.setText("上次更新时间：");
		text1.setBounds(20, 20, 100, 25);
		add(text1);
		
		lastUpdatedTime = new JTextField();
		lastUpdatedTime.setText("");
		lastUpdatedTime.setEditable(false);
		lastUpdatedTime.setHorizontalAlignment(JTextField.CENTER);
		lastUpdatedTime.setBounds(140, 20, 150, 25);
		add(lastUpdatedTime);
		
		updateButton = new JButton();
		updateButton.setText("开始抓取");
		updateButton.setBounds(360, 20, 130, 25);
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateButton.setEnabled(false);
				updateButton.setText("正在抓取...");
				tableModel.setRowCount(0);
				String[] sp = lastUpdatedTime.getText().split("-");
				@SuppressWarnings("deprecation")
				Date date = new Date(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]) - 1, Integer.parseInt(sp[2]));
				BackgroundThread thread = new BackgroundThread(date.getTime());
				thread.start();
			}
		});
		add(updateButton);
		
		generateButton = new JButton();
		generateButton.setText("写入本地库");
		generateButton.setBounds(495, 20, 130, 25);
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, ArrayList<ForumThread>> list = controller.getDataSet().getData();
				if (dataAll != null) {
					for (int i = dataAll.size() - 1; i >= 0; i--) {
						String string = (String)tableModel.getValueAt(i, 3);
						if (string != null && string.length() > 0)
							list.get(string).add(dataAll.get(i));
					}
					controller.getDataSet().setLastUpdateTime(currentUpdateTime);
					controller.getDataSet().setLastUpdateThread(currentUpdateThread);
				}
				boolean b = controller.writeLocalDataSet();
				if (b) {
					lastUpdatedTime.setText(controller.getDataSet().getLastUpdateTime());
					JOptionPane.showMessageDialog(UserInterface.this,"写入成功", "写入成功",JOptionPane.PLAIN_MESSAGE);
				}
				else
					JOptionPane.showMessageDialog(UserInterface.this,"写入失败", "写入失败",JOptionPane.ERROR_MESSAGE);
			}
		});
		add(generateButton);
		
		writeButton = new JButton();
		writeButton.setText("更新到远景");
		writeButton.setBounds(625, 20, 160, 25);
		writeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Entry<String, String>> result = controller.getDataSet().toForumDoc();
				String[] infos = controller.getThreadInfo().split(" ");
				
				// Update Time
				String[] info = infos[0].split(",");
				boolean isSucc = controller.writeToForum(info[0], info[1], info[2], info[3], "[align=center][color=#ff00][size=7]更新日期：" + controller.getDataSet().getLastUpdateTime() + "[/size][/color][/align]"
						+ "\r\n[align=center][color=#9932cc]由Laptop Automator生成[/color][/align]");
				showResult(isSucc, "2F");
				sleep3s();
				
				// Acer   -> 3F
				// Asus   -> 4F
				// Dell   -> 5F
				// HP     -> 6F
				// Hasee  -> 7F
				// Lenovo -> 8F
				for (int i = 1; i <= 6; i++) {
					info = infos[i].split(",");
					isSucc = controller.writeToForum(info[0], info[1], info[2], info[3], result.get(i - 1).getValue());
					showResult(isSucc, (i + 2) + "F");
					sleep3s();
				}
				
				// MSI && Samsung && Sony && Toshiba
				info = infos[7].split(",");
				isSucc = controller.writeToForum(info[0], info[1], info[2], info[3], result.get(6).getValue() + "\r\n" + result.get(8).getValue() + "\r\n" + result.get(9).getValue() + "\r\n" + result.get(10).getValue());
				showResult(isSucc, "9F");
				sleep3s();
				
				// Others
				info = infos[8].split(",");
				isSucc = controller.writeToForum(info[0], info[1], info[2], info[3], result.get(7).getValue());
				showResult(isSucc, "10F");
			}
			
			public void showResult(boolean isSucc, String floor) {
				if (!isSucc)
					JOptionPane.showMessageDialog(UserInterface.this, floor + "进入论坛审核", floor + "进入论坛审核", JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane.showMessageDialog(UserInterface.this, floor + "写入论坛成功", floor + "写入论坛成功", JOptionPane.PLAIN_MESSAGE);
			}
			
			public void sleep3s() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		add(writeButton);
		
		Object[][] data = new Object[0][5];
		String[] colomnNames = {"标题", "作者", "发帖时间", "挑选"};
		tableModel = new DefaultTableModel(data, colomnNames) {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {
                return (column == 3);
            }
		};
		threadTable = new JTable(tableModel);
		threadTable.setFont(new Font("Menu.font", Font.PLAIN, 15));
		threadTable.setRowHeight(20);
		TableColumnModel colomnModel = threadTable.getColumnModel();
		colomnModel.getColumn(0).setPreferredWidth(400);
		colomnModel.getColumn(1).setPreferredWidth(100);
		colomnModel.getColumn(2).setPreferredWidth(100);
		colomnModel.getColumn(3).setPreferredWidth(100);
		
//		colomnModel.getColumn(3).setCellEditor(threadTable.getDefaultEditor(Boolean.class));   
//		colomnModel.getColumn(3).setCellRenderer(threadTable.getDefaultRenderer(Boolean.class));
//		colomnModel.getColumn(3).setCellEditor(new DefaultCellEditor(new JCheckBox()));
		JComboBox<String> comboBox = new JComboBox<String>();
		for (int i = 0; i < LAPTOP_TYPE.length; i++)
			comboBox.addItem(LAPTOP_TYPE[i]);
        comboBox.setSelectedIndex(0);
        DefaultCellEditor cellEditor = new DefaultCellEditor(comboBox);
		colomnModel.getColumn(3).setCellEditor(cellEditor);
		
		threadTable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && threadTable.getSelectedColumn() == 0) {
					int row = threadTable.getSelectedRow();
					String page = "http://bbs.pcbeta.com/viewthread-" + dataAll.get(row).getTid() + "-1-1.html";
					try {
						URI uri = new URI(page);
						Desktop.getDesktop().browse(uri);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
			    }
			}
		});
		JScrollPane scrollPane = new JScrollPane(threadTable);
		scrollPane.setBounds(20, 55, 760, 460);
		add(scrollPane);
		
		setSize(800, 600);
		setTitle("Laptop Automator");
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		controller = new Controller();
		regexes = controller.getRegex();
		lastUpdatedTime.setText(controller.getDataSet().getLastUpdateTime());
		lastUpdateThread = controller.getDataSet().getLastUpdateThread();
	}
	
	public static void main(String[] args) {
		new UserInterface();
	}
}
