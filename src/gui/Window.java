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
        viewPanel.setPreferredSize(new Dimension(width * 4 / 5, height));
        
        add(menuPanel, BorderLayout.WEST);
        add(viewPanel, BorderLayout.EAST);
        
        setVisible(true);
    }
    
    /**
     * Select images to be used for training
     */
    private void train() {
        // TODO
        JFileChooser openFile = new JFileChooser();
        
        // Have the user enter the name
        String userInput = JOptionPane.showInputDialog(getContentPane(), "Enter the Name of the Student", "User Input",
                                                       JOptionPane.INFORMATION_MESSAGE);
        
        // check if they entered something, and if not do nothing
        if (userInput != null) {
            openFile.setMultiSelectionEnabled(true);
            openFile.showOpenDialog(null);
            
            // Create a map using the file paths of selected images and send to
            // training functions
            if (openFile.getSelectedFiles() != null) {
                
                printFilePaths(openFile.getSelectedFiles());
                putFilesInList(openFile.getSelectedFiles());
                
                printf(userInput);
                Map<String, List<String>> m = new HashMap<String, List<String>>();
                m.put(userInput, putFilesInList(openFile.getSelectedFiles()));
                printf(m.get(userInput).toString());
                
                // Do the training
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
    
    /**
     * Debug method for printing the locations of multiple selected files
     *
     * @param files
     *            File objects of which to print paths
     */
    private void printFilePaths(File[] files) {
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i].getAbsolutePath());
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
        // TODO
        JFileChooser openFile = new JFileChooser();
        openFile.showOpenDialog(null);
        
        if (openFile.getSelectedFile() != null) {
            // Send this location to processing
            System.out.println(openFile.getSelectedFile().getAbsolutePath());
            
            // ask about the class name and if they want to take attendance
            String userInput = JOptionPane.showInputDialog(getContentPane(), "Enter Class Name",
                                                           "Class Name Requirement", JOptionPane.DEFAULT_OPTION);
            
            if (userInput != null) {
                Integer attendance = JOptionPane.showConfirmDialog(getContentPane(), "Take Attendnce?", "Attendance",
                                                                   JOptionPane.YES_NO_OPTION);
                
                if (attendance == JOptionPane.YES_OPTION) {
                    // do the process for identify but with attendance
                    
                    OpenCVBinding.Result result = OpenCVBinding.recognize(openFile.getSelectedFile().getAbsolutePath(),
                                                                          true, userInput);
                    
                    // Display processed image with name overlays
                    viewPanel.removeAll();
                    viewPanel.add(createImageComponent(openFile.getSelectedFile().getAbsolutePath()));
                    revalidate();
                    repaint();
                    
                    if (!(result.success)) {
                        // make an error pop-up
                        JOptionPane.showMessageDialog(getContentPane(), result.errors.toString(), "Error",
                                                      JOptionPane.ERROR_MESSAGE);
                        
                    }
                } else {
                    // do the process for identify but without attendance
                    OpenCVBinding.Result result = OpenCVBinding.recognize(openFile.getSelectedFile().getAbsolutePath(),
                                                                          false, userInput);
                    
                    // Display processed image with name overlays
                    viewPanel.removeAll();
                    viewPanel.add(createImageComponent(openFile.getSelectedFile().getAbsolutePath()));
                    revalidate();
                    repaint();
                    
                    if (!(result.success)) {
                        // make an error pop-up
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
    
    /*
     * Shorthand method for printing to console
     */
    private void printf(String s) {
        System.out.println(s);
    }
    
}
