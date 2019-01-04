/*
	Alexander Shmakov
	cmpt400:Project
	Apr 8, 2018
*/

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Window extends JFrame {

	private JPanel jp;

	private JLabel jl;
	private JTextField jtf;
	private JButton jb;
	private JTextArea jta;
	private String filename;

	public Window() {

		jb = new JButton("Request");
		jp = new JPanel(new GridBagLayout());
		getContentPane().add(jp, BorderLayout.NORTH);
		// setting the margins of each component is the panel
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);

		jl = new JLabel("Enter filename:");
		jta = new JTextArea(20,50);
		jtf = new JTextField(30);
		filename = null;

		setLayout(new GridLayout(1,4));
		setTitle("FileShare");
		setVisible(true);
		setSize(600,500);
		// setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		c.gridx = 0;
		c.gridy = 0;
		jp.add(jl,c);

		c.gridx = 0;
		c.gridy = 1;
		jp.add(jtf,c);

		c.gridx = 0;
		c.gridy = 2;
		jp.add(jb,c);
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addLine(jtf.getText());
				setFileName(jtf.getText());
			}
		});

		c.gridx = 0;
		c.gridy = 3;
		JScrollPane scroll = new JScrollPane (jta,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jp.add(scroll,c);

		add(jp);
	}

	public String getFileName() {
		return this.filename;
	}

	public void setFileName(String str) {
		this.filename = str;
	}

	public void addLine(String str) {
		this.jta.append(str+"\n");
		this.jta.setCaretPosition(this.jta.getDocument().getLength());
	}

	public static void main(String[] args){
		Window win = new Window();

		System.out.println("FileName is: "+win.getFileName());
	}
}