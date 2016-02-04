package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import binding.OpenCVBinding;

/**
 * GUI to handle image selection and calling of processing commands
 *
 * @author Andrew Dailey
 * @author Juan Venegas
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
        menuPanel.setPreferredSize(new Dimension(width * 1 / 5, height));
        
        // Button for training
        JButton trainButton = new JButton("Train");
        trainButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                train();
            }
            
        });
        menuPanel.add(trainButton);
        
        // Button for identifying an image
        JButton identifyButton = new JButton("Identify");
        identifyButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                identify();
            }
            
        });
        menuPanel.add(identifyButton);
        
        // Panel to view analyzed image
        viewPanel = new JPanel();
        viewPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        viewPanel.setPreferredSize(new Dimension(width * 4 / 5, height));
        
        add(menuPanel, BorderLayout.WEST);
        add(viewPanel, BorderLayout.EAST);
        
        setVisible(true);
    }
    
    /**
     * Select images to be used for training
     */
    private void train() {
        JFileChooser openFile = new JFileChooser();
        
        // Have the user enter the name
        String userInput = JOptionPane.showInputDialog(getContentPane(), "Enter the Name of the Student", "User Input",
                                                       JOptionPane.INFORMATION_MESSAGE);
        
        // Check if they entered something, and if not do nothing
        if (userInput != null) {
            openFile.setMultiSelectionEnabled(true);
            openFile.showOpenDialog(null);
            
            // Create a map using the file paths of selected images
            // Send to training functions
            if (openFile.getSelectedFiles() != null) {
                
                putFilesInList(openFile.getSelectedFiles());
                
                Map<String, List<String>> m = new HashMap<String, List<String>>();
                m.put(userInput, putFilesInList(openFile.getSelectedFiles()));
                
                // Train OpenCV using bindings
                OpenCVBinding.Result result = OpenCVBinding.trainFiles(m);
                
                if (!(result.success)) {
                    JOptionPane.showMessageDialog(getContentPane(), result.errors.toString(), "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(getContentPane(), "Trained Successfully", "Success",
                                                  JOptionPane.INFORMATION_MESSAGE);
                }
                
            } else {
                JOptionPane.showMessageDialog(this, "Null File", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
    }
    
    private ArrayList<String> putFilesInList(File[] files) {
        ArrayList<String> list = new ArrayList<String>();
        
        for (int i = 0; i < files.length; i++) {
            list.add(files[i].getAbsolutePath());
        }
        
        return list;
    }
    
    /**
     * Select a single image to be analyzed
     */
    private void identify() {
        JFileChooser openFile = new JFileChooser();
        openFile.showOpenDialog(null);
        
        if (openFile.getSelectedFile() != null) {
            
            // Prompt user for class name
            String userInput = JOptionPane.showInputDialog(getContentPane(), "Enter Class Name",
                                                           "Class Name Requirement", JOptionPane.DEFAULT_OPTION);
            
            if (userInput != null) {
                Integer attendance = JOptionPane.showConfirmDialog(getContentPane(), "Take Attendance?", "Attendance",
                                                                   JOptionPane.YES_NO_OPTION);
                
                if (attendance == JOptionPane.YES_OPTION) {
                    // Identify with attendance
                    OpenCVBinding.Result result = OpenCVBinding.recognize(openFile.getSelectedFile().getAbsolutePath(),
                                                                          true, userInput);
                    
                    // Display processed image with name overlays
                    viewPanel.removeAll();
                    viewPanel.add(createImageComponent(openFile.getSelectedFile().getAbsolutePath()));
                    revalidate();
                    repaint();
                    
                    if (!(result.success)) {
                        JOptionPane.showMessageDialog(getContentPane(), result.errors.toString(), "Error",
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Identify without attendance
                    OpenCVBinding.Result result = OpenCVBinding.recognize(openFile.getSelectedFile().getAbsolutePath(),
                                                                          false, userInput);
                    
                    // Display processed image with name overlays
                    viewPanel.removeAll();
                    viewPanel.add(createImageComponent(openFile.getSelectedFile().getAbsolutePath()));
                    revalidate();
                    repaint();
                    
                    if (!(result.success)) {
                        JOptionPane.showMessageDialog(getContentPane(), result.errors.toString(), "Error",
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        }
        
    }
    
    /**
     * Creates a swing component from an image path
     * 
     * @param path
     *            Path to the image
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
