
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Main {
	String filepath = "请选择要上传的文件";
	JFrame main_frame;

	JLabel registerLabel;
	JLabel answerLabel;
	JLabel resultLabel;
	TextArea token;
	TextArea cid;
	TextArea result;
	Container c;
	Button fileChooser ;
	Button submit ;
	JLabel uploadFileLabel;
	JLabel disclaimer;
	public Main() {

		main_frame = new JFrame();
		main_frame.setSize(600, 250);
		main_frame.setLocationRelativeTo(null);//打开居中
		main_frame.setTitle("免费图片上传保管程序");
		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		c = main_frame.getContentPane();
		c.setLayout(null);

		registerLabel = new JLabel();
		registerLabel.setText("token");
		registerLabel.setBackground(Color.GREEN);
		registerLabel.setBounds(20, 20, 60, 40);
		c.add(registerLabel);

		token = new TextArea();
		token.setBounds(80, 20, 500, 40);
		c.add(token);

		answerLabel = new JLabel();
		answerLabel.setText("x:categoryId");
		answerLabel.setBackground(Color.GREEN);
		answerLabel.setBounds(20, 60, 100, 40);
		c.add(answerLabel);

		cid = new TextArea();
		cid.setBounds(120, 60, 460, 40);
		c.add(cid);

		resultLabel = new JLabel();
		resultLabel.setText("result");
		resultLabel.setBackground(Color.GREEN);
		resultLabel.setBounds(20, 100, 60, 40);
		c.add(resultLabel);

		result = new TextArea();
		result.setBounds(80, 100, 500, 40);
		c.add(result);
		
		fileChooser = new Button("选择文件");
		fileChooser.setBounds(20, 150, 80, 20);
		fileChooser.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("请选择要上传的文件...");
				fc.setApproveButtonText("确定");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
				
				int ret = fc.showOpenDialog(null);
				if (ret == JFileChooser.CANCEL_OPTION) {
					filepath = "请选择要上传的文件";
					
				} else if (ret == JFileChooser.APPROVE_OPTION) {
					filepath = fc.getSelectedFile().getAbsolutePath();
				} else {
					filepath = "请选择要上传的文件";
				}
				System.out.println(filepath);
				uploadFileLabel.setText(filepath);
				fc.setBounds(20, 180, 0, 0);
				c.add(fc);
			}
			
		});
		c.add(fileChooser);
		
		uploadFileLabel = new JLabel();
		uploadFileLabel.setText("请选择要上传的文件");
		uploadFileLabel.setBackground(Color.GREEN);
		uploadFileLabel.setBounds(110, 150, 400, 20);
		c.add(uploadFileLabel);
		
		submit = new Button("上传");
		submit.setBounds(520, 150, 60, 20);
		submit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				String tokenValue = token.getText();
				String cidValue = cid.getText();
				if(tokenValue.equals("")) {
					JOptionPane.showMessageDialog(null, "token必须填写", "InfoBox: " + "提示信息", JOptionPane.INFORMATION_MESSAGE);
				}
				if(cidValue.equals("")) {
					JOptionPane.showMessageDialog(null, "x:categoryId必须填写", "InfoBox: " + "提示信息", JOptionPane.INFORMATION_MESSAGE);
				}
				if(filepath.equals("请选择要上传的文件")) {
					JOptionPane.showMessageDialog(null, "文件必须选择", "InfoBox: " + "提示信息", JOptionPane.INFORMATION_MESSAGE);
				}
				if(!tokenValue.equals("")&&!cidValue.equals("")&&!filepath.equals("请选择要上传的文件")) {
					String resultValue = HttpUtil.uploadImageBugs(tokenValue, cidValue, filepath);		
					result.setText(resultValue);
				}
			}
			
		});
		c.add(submit);
		
		disclaimer = new JLabel();
		disclaimer.setText("本程序仅做验证性测试，请勿用于非法用途，代码开源，因程序导致的纠纷请自行承担后果！");
		disclaimer.setBackground(Color.GREEN);
		disclaimer.setBounds(20, 170, 600, 20);
		c.add(disclaimer);
		

		main_frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Main();
	}
}
