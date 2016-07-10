import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class UserInterface extends JFrame {
	private JTextField lastUpdatedTime;
	private JButton updateButton;
	private JButton generateButton;
	private JTable threadTable;
	private DefaultTableModel tableModel;
	private Controller controller;
	private Vector<ForumThread> dataAll;
	private HashMap<String, Integer> regexes;
	
	public class BackgroundThread extends Thread {
		private long lastTime;
		
		public BackgroundThread(long lastTime) {
			this.lastTime = lastTime;
		}
		
		@Override
		public void run() {
			int j = 0;
			boolean flag = true;
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
					String[] sp = ((String) data.get(i).get(2)).split("-");
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
		lastUpdatedTime.setText("2016-1-1");
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
				Date date = new Date(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]) - 1, Integer.parseInt(sp[2]));
				BackgroundThread thread = new BackgroundThread(date.getTime());
				thread.start();
			}
		});
		add(updateButton);
		
		generateButton = new JButton();
		generateButton.setText("生成结果");
		generateButton.setBounds(500, 20, 130, 25);
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = new File("result.txt");
				FileWriter fw = null;
				try {
					fw = new FileWriter(file);
					for (ForumThread t : dataAll)
						fw.write(t.toString());
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(UserInterface.this,"生成失败", "生成失败",JOptionPane.ERROR_MESSAGE);
					return;
				} finally {
					try {
						fw.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				JOptionPane.showMessageDialog(UserInterface.this,"生成成功", "生成成功",JOptionPane.PLAIN_MESSAGE);
			}
		});
		add(generateButton);
		
		Object[][] data = new Object[0][5];
		String[] colomnNames = {"标题", "作者", "发帖时间", "挑选"};
		tableModel = new DefaultTableModel(data, colomnNames);
		threadTable = new JTable(tableModel);
		threadTable.setFont(new Font("Menu.font", Font.PLAIN, 15));
		threadTable.setRowHeight(20);
		TableColumnModel colomnModel = threadTable.getColumnModel();
		colomnModel.getColumn(0).setPreferredWidth(400);
		colomnModel.getColumn(1).setPreferredWidth(100);
		colomnModel.getColumn(2).setPreferredWidth(100);
		colomnModel.getColumn(3).setPreferredWidth(15);
		
		colomnModel.getColumn(3).setCellEditor(threadTable.getDefaultEditor(Boolean.class));   
		colomnModel.getColumn(3).setCellRenderer(threadTable.getDefaultRenderer(Boolean.class));
		colomnModel.getColumn(3).setCellEditor(new DefaultCellEditor(new JCheckBox()));
		
		JScrollPane scrollPane = new JScrollPane(threadTable);
		scrollPane.setBounds(20, 55, 760, 460);
		add(scrollPane);
		
		setSize(800, 600);
		setTitle("自动更新");
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		controller = new Controller();
		regexes = controller.getRegex();
	}
	
	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
	}
}
