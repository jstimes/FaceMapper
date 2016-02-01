package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Window extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private int width, height;
	private JPanel menuPanel, viewPanel;
	
	/**
	 * Contructs a new GUI
	 * @param width Width of the window
	 * @param height Height of the window
	 */
	public Window(int width, int height) {
		this.width = width;
		this.height = height;
		init();
	}
	
	/**
	 * Creates main window, JFrames, and buttons
	 */
	private void init() {
		setSize(new Dimension(width, height));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		setLayout(new BorderLayout());
		setResizable(false);
		setLocationRelativeTo(null);
		setTitle("FaceMapper - Team Bits Please");

		menuPanel = new JPanel();
		menuPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		menuPanel.setPreferredSize(new Dimension(width * 1/5, height));
		
		JButton trainButton = new JButton("Train");
		trainButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				train();
			}
			
		});
		menuPanel.add(trainButton);
		
		JButton identifyButton = new JButton("Identify");
		identifyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				identify();
			}
			
		});
		menuPanel.add(identifyButton);
		
		viewPanel = new JPanel();
		viewPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		viewPanel.setPreferredSize(new Dimension(width * 4/5, height));
		
		add(menuPanel, BorderLayout.WEST);
		add(viewPanel, BorderLayout.EAST);

		setVisible(true);
	}
	
	/**
	 * Select images to be used for training
	 */
	private void train() {
		System.out.println("TODO: Train!");
	}
	
	/**
	 * Select a single image to be analyzed
	 */
	private void identify() {
		System.out.println("TODO: Identify!");
		JFileChooser openFile = new JFileChooser();
		openFile.showOpenDialog(null);
		
		viewPanel.removeAll();
		viewPanel.add(createImageComponent(openFile.getSelectedFile().getAbsolutePath()));
		revalidate();
		repaint();
		
		System.out.println(openFile.getSelectedFile().getAbsolutePath());
	}
	
	/**
	 * Creates a swing component from an image path
	 * @param path Path to the image
	 * @return JLabel containing the image
	 */
	private JLabel createImageComponent(String path) {
		Image myPicture = null;
		try {
			myPicture = ImageIO.read(new File(path));
			myPicture = myPicture.getScaledInstance(viewPanel.getWidth(), viewPanel.getHeight(), Image.SCALE_SMOOTH);
		} catch (IOException e) {
			System.out.println("Unable to open file!");
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		picLabel.setBounds(0, 0, viewPanel.getWidth(), viewPanel.getHeight());
		return picLabel;
	}

}
