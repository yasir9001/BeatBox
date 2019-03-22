package finalizingproject;

import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.event.*;



class BeatBox{
       JFrame frame;      
       JPanel background, mainPanel,forSlider, pianoTab;
       JTabbedPane pane;
       Box buttonBox;JPanel nameBox;
       
       JSlider slider;
       JLabel  volumeLabel;
       ArrayList<JCheckBox> checkboxList;
       Sequencer sequencer;
       Sequence sequence ;
       Track track;
       
       String[]instrumentNames = {"Bass Drum", "Closed Hi-Hat", "open Hi-Hat", "Acoustic Snare", "Crash Cymble", "Hand Clap","High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cow Bell", "Vibraslap", "Low-mid Tom", "High Agogo", "open Hi Conga"};
       
       int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};//key numbers for instruments
       int channel = 9, velocity = 100, count = 0;
       
    
    
    public void buildGUI() {
        
        frame = new JFrame("Beat Box");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        BorderLayout layout = new BorderLayout();
        background = new JPanel(layout);//sets background's layout to be border.
        background.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pane = new JTabbedPane();
        
        checkboxList = new ArrayList<JCheckBox>();
        
        //now adding buttons, slider and checkboxes to the GUI and registring for event listeners
        setupBackgroundPanel();
        
        pane.add("Music Band",background);
        pane.addTab("Piano",pianoTab);
        pane.addChangeListener(new MyPaneEventListener());
        frame.getContentPane().add(pane);
        
        setUpMidi();//Create sequencer and create midi data, add it to track
        
        frame.setBounds(450,50,600,600);
        frame.pack();
        frame.setMaximumSize(new Dimension(frame.getSize()));
        
  
        try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println(e);
        }
        SwingUtilities.updateComponentTreeUI(frame);//apply changes to all component on the frame and when fram is resized.
  
        
       frame.setVisible(true);
       frame.setResizable(true);
    }//close method bildGUI()
    
    
    
    public void setupBackgroundPanel(){
        //code for buttonBox
        buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        
        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        
        JButton downTempo = new JButton("Tempo down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);//box class provides a container with borderlayout as its default style. 
        
        
        JButton reset = new JButton("Clear Pattern");
        reset.addActionListener(new MyResetListener());
        buttonBox.add(reset);
        
        
        JButton randomPat =new JButton("Random Pattern");
        randomPat.addActionListener(new MyRandomPatListener());
        buttonBox.add(randomPat);
        
        
        JButton save = new JButton("Save Patern");
        save.addActionListener(new MySaveListener());
        buttonBox.add(save);
        
        
        JButton restore = new JButton("Restore Patern");
        restore.addActionListener(new MyRestoreListener());
        buttonBox.add(restore);
      
        
        background.add(BorderLayout.EAST,  buttonBox);// adding buttonBox to background
        
        //code for slider
        slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        slider.setMinorTickSpacing(10);
        slider.setMajorTickSpacing(20);
        slider.setPaintTicks(false);
        slider.setPaintTrack(true);
        slider.setPaintLabels(false);   
        slider.addMouseListener(new MyMouseListener());
        slider.addKeyListener(new MyKeyListener());
        slider.addChangeListener(new MySliderChangeListener());
        forSlider = new JPanel();//panel to add slider
        
        JLabel label = new JLabel("Volume");
        forSlider.add(label);
        forSlider.add(slider);
        volumeLabel = new JLabel(String.valueOf(slider.getValue()));
        forSlider.add(volumeLabel);
        forSlider.add(new JLabel("                                                    "));
        background.add(BorderLayout.SOUTH, forSlider);
    
       //code for nameBox 
        nameBox = new JPanel();
        nameBox.setLayout(new GridLayout(16,1));
        
        for(int i = 0; i<16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }
        
        background.add(BorderLayout.WEST,  nameBox);
        
        
        //code for mainPanel
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        
        mainPanel = new JPanel(grid);//sets mainPanel's layout to be a grid of 16x16 for holding checkboxes
        background.add(BorderLayout.CENTER, mainPanel);
        for(int i=0; i<256;i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }//end loop
    }//end setupBackground method
   
    
    
    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
          
            sequence = new Sequence(Sequence.PPQ,4);
            sequencer.setTempoInBPM(120);
        }
        
        catch(Exception e){e.printStackTrace();}
    }// close method
    
    
    
    public void buildTrackAndStart() {
        int[] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        for (int i =0; i<16; i++){
            trackList = new int[16];
            
            int key = instruments[i];
        
                for (int j=0; j<16; j++) {
                    JCheckBox jc = (JCheckBox) checkboxList.get(j+(16*i));
                    if(jc.isSelected()){
                        trackList[j] = key;
                    }
                    else {
                        trackList[j] = 0;
                    }
                }//closed inner
            
            makeTracks(trackList);
            //track.add(makeEvent(176,1,127,0,16));//the midiEvent that gives back the controller event
        }//closed outer
        
        track.add(makeEvent(192,channel,1,0,15));//this event do nothing, but we add it to run all 16 beats even if the last beat are not selected,
        
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//close build and start method    
        
    
    
    
    public void makeTracks (int[] list){
        for(int i=0;i<16;i++){
            int key = list[i];
            
            if(key !=0){
                track.add(makeEvent(144,channel,key,velocity,i));//noteON Event
                track.add(makeEvent(128,channel,key,velocity,i+1));//NoteOff Event
            }
        }
    }//close method makeTracks 
    
    
    
    public MidiEvent makeEvent(int comd, int chan,int one, int two, int tick) {

    MidiEvent event = null;
    
    try{
    ShortMessage a = new ShortMessage();
    a.setMessage(comd, chan, one,two);
    event = new MidiEvent(a,tick);
    }
    
    catch(Exception e){
        e.printStackTrace();
    }
    return event;
    
    }//close method
    
    
    
    public class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }//inner closed
    
    
    
    public class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }//inner closed
    
    
    
    public class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
            
        }
    }//inner closed


    
    public class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .97));            
        }
    }//inner closed
    
    
    
    public class MyResetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            for(int i = 0; i<16; i++){
                for(int j = 0; j<16; j++ ){
         JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));        
         jc.setSelected(false);                                                 
                }// close inner for
            }//close outer for
        }
    }//close inner class
    
    
    
    public class MyRandomPatListener implements ActionListener{
   
        int[] select = new int[256];
   
        @Override
        
        public void actionPerformed(ActionEvent a){
            //randomly generates 1 or 0. done to  set the the checkboxes selected or unselected.
            for( int i=0; i<16;i++){
               for(int j =0; j<16;j++){    
                    select[i+(16*j)] =(int)  (Math.random()*1.1);
                }//end inner for
            }// end outer for
           
            
            for( int i=0; i<16;i++){
                for(int j =0; j<16;j++){
         
                    if(select[i+(16*j)]==1){
                        JCheckBox jc = (JCheckBox) checkboxList.get(i+(16*j));
                        jc.setSelected(true);
                    }// close if  
                }// close inner for
            }// close outer for
        }//close actionPerformed
    }//closeed inner class

    
    
    public class MySaveListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ev){
         
        /*This code brings up a file dialog box and waits on this line until the user chooses 'save' from the dialog box.  All the file dialog
        navigation and selecting a file, ect is done by the JFileChooser!*/
        
            JFileChooser fileSave = new JFileChooser();
            fileSave.showSaveDialog(frame);
            saveFile(fileSave.getSelectedFile());
    }// end actionPerformed method
}//end inner class
 
    
    
    private void saveFile(File file){
        
        boolean[] checkboxState = new boolean[256]; //make a boolean array to hold the state of the boxes
        for(int i = 0; i < 256; i++){
            //walk through the checkbox list and get the state of each box then add it to the array
            JCheckBox check = (JCheckBox) checkboxList.get(i); 
            if(check.isSelected()){
                checkboxState[i] = true;
            }
        }
         
    try{
        FileOutputStream fileStream = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(fileStream);
        os.writeObject(checkboxState);
    }catch(Exception ex){
    }
    } //close method

    
    
    public class MyRestoreListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a){
            JFileChooser openFile = new JFileChooser();
            openFile.showOpenDialog(frame);
            restoreFile(openFile.getSelectedFile());
        }//close method
    }//close inner
    
    
    
    public void restoreFile(File file){
        boolean [] checkboxState = null;
        try{
            FileInputStream fileIn = new FileInputStream((file));
            ObjectInputStream is = new ObjectInputStream(fileIn);
            checkboxState = (boolean[]) is.readObject();
        }
        catch(Exception e){
            System.out.print(e);
        }
        
        try{
        for(int i= 0; i<256;i++){
            
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if(checkboxState[i]){
                check.setSelected(true);
            }
            else{
                check.setSelected(false);
            }
        }
        }catch(NullPointerException e){System.out.println(e);}
        
        }//close method restoreFile
    
    
    
    public class MySliderChangeListener implements ChangeListener{

        @Override
        public void stateChanged(ChangeEvent e) {
            volumeLabel.setText(String.valueOf(slider.getValue()));
        }
        
    }//close inner class
    
    
    
    public class MyMouseListener implements MouseListener{

        @Override
        public void mouseReleased(MouseEvent e) {
            velocity = slider.getValue();
          
            buildTrackAndStart();
        }
        @Override
        public void mousePressed(MouseEvent e) {
            volumeLabel.setText(String.valueOf(slider.getValue()));
        }
        @Override
        public void mouseClicked(MouseEvent e) {}
        
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e)  {}
        
    }//end inner class
     
     
     
    public class MyKeyListener implements KeyListener{
        
        @Override
        public void keyReleased(KeyEvent e) {        
            if(e.getKeyCode()==37||e.getKeyCode()==38||e.getKeyCode()==39||e.getKeyCode()==40){
                velocity=slider.getValue();
                
                buildTrackAndStart();
            }
        }
        @Override
        public void keyTyped(KeyEvent e) {
        }
        @Override
        public void keyPressed(KeyEvent e) {
        }
        
    }//close inner
    
    
    
    public class MyPaneEventListener implements ChangeListener{

        @Override//for piano
        public void stateChanged(ChangeEvent e) {
            if(count%2==0){
                 channel=1; 
                 sequencer.stop();
               
                 nameBox.removeAll();
                 
                 for(int i=0;i<16;i++){
                     JLabel label = new JLabel("    key   "+(i+1)+"             ");
                     
                     nameBox.add(label);
                 }
                 count++;
            }//close if
            else if(count%2!=0){
                 channel=9; 
                 sequencer.stop();
               
                 nameBox.removeAll();
            for(int i = 0; i<16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
            }
                count++;
            }
        }//close method stateChanged
    }//close inner class
}//close class beatBox