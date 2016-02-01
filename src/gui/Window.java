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

/**
 * GUI to handle image selection and calling of processing commands
 * @author Andrew Dailey
 *
 */
public class Window extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final int width = 960;
	private final int height = 540;
	private JPanel menuPanel, viewPanel;
	
	/**
	 * Contructs a new GUI
	 */
	public Window() {
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
		//TODO
		JFileChooser openFile = new JFileChooser();
		openFile.setMultiSelectionEnabled(true);
		openFile.showOpenDialog(null);
		
		// Create a map using the file paths of selected images and send to training functions
		if(openFile.getSelectedFiles() != null) {
			printFilePaths(openFile.getSelectedFiles());
		}

	}
	
	/**
	 * Debug method for printing the locations of multiple selected files
	 * @param files File objects of which to print paths
	 */
	private void printFilePaths(File[] files) {
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getAbsolutePath());
		}
	}
	
	/**
	 * Select a single image to be analyzed
	 */
	private void identify() {
		//TODO
		JFileChooser openFile = new JFileChooser();
		openFile.showOpenDialog(null);
		
		if(openFile.getSelectedFile() != null) {
			// Send this location to processing
			System.out.println(openFile.getSelectedFile().getAbsolutePath());
			
			// Display processed image with name overlays
			viewPanel.removeAll();
			viewPanel.add(createImageComponent(openFile.getSelectedFile().getAbsolutePath()));
			revalidate();
			repaint();
		}

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
