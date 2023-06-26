import java.applet.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.util.Arrays;
import java.util.EventListener;
import java.awt.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SoundBox extends JFrame implements Runnable, AdjustmentListener, ActionListener{

    JToggleButton button [][] = new JToggleButton[37][180];
    JScrollPane buttonPane;
    JScrollBar tempoBar;
    JMenuBar menuBar;
    JMenu file, instrumentMenu, addremoveColumn;
    JMenuItem save,load;
    JMenuItem[] instrumentItems, addremoveJMenuItems;
    JButton stopPlay, clear, fillRandom, restartButton;
    JFileChooser fileChooser;
    JLabel[] labels = new JLabel[button.length];
    JPanel buttonPanel, labelPanel,tempoPanel,menuButtonPanel;
    JLabel tempoLabel;
    boolean notStopped = true;
    JFrame frame = new JFrame();
    String [] clipNames;
    Clip[] clip;
    int tempo;
    boolean playing = false;
    int row=0,col=0;
    Font font = new Font("Times new Roman",Font.PLAIN,10);
    String [] instrumentNames={"Piano","Bell", "Marimba"};
    String [] addRemove = {"Add Column", "Add 10 Columns", "Remove Column", "Remove 10 Columns"};

    public SoundBox(){
        setSize(1000,800);
        clipNames = new String[]{"C0","B1","ASharp1","A1","GSharp1","G1","FSharp1","F1","E1","DSharp1","D1","CSharp1","C1", "B2","ASharp2","A2","GSharp2","G2","FSharp2","F2","E2","DSharp2","D2","CSharp2","C2", "B3","ASharp3","A3","GSharp3","G3","FSharp3","F3","E3","DSharp3","D3","CSharp3","C3"};
        clip = new Clip[clipNames.length];
        String initInstrument = "\\"+instrumentNames[0]+"\\"+instrumentNames[0];

        try{
            for(int x = 0 ; x < clipNames.length ; x++){
                URL url = this.getClass().getClassLoader().getResource(initInstrument+" - "+clipNames[x]+".wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                clip[x] = AudioSystem.getClip();
                clip[x].open(audioIn);
            }
        }
        catch(UnsupportedAudioFileException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(LineUnavailableException e){
            e.printStackTrace();
        }

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(button.length, button[0].length,2,5));
		for(int r = 0 ; r < button.length ; r++){
            String name = clipNames[r].replaceAll("Sharp", "#");
		    for(int c = 0 ; c < button[0].length ; c++){
                button[r][c]=new JToggleButton();
                button[r][c].setFont(font);
                button[r][c].setText(name);
                button[r][c].setPreferredSize(new Dimension(30,30));
                button[r][c].setMargin(new Insets(0, 0, 0, 0));
                buttonPanel.add(button[r][c]);
            }
        }
        tempoBar = new JScrollBar(JScrollBar.HORIZONTAL, 200, 0 ,50, 1000);
        tempoBar.addAdjustmentListener(this);
        tempo = tempoBar.getValue();
        tempoLabel = new JLabel(String.format("%s%6s", "Tempo: ", tempo));
        tempoPanel = new JPanel(new BorderLayout());
        tempoPanel.add(tempoLabel,BorderLayout.WEST);
        tempoPanel.add(tempoBar,BorderLayout.CENTER);

        String currDir = System.getProperty("user.dir");
        fileChooser = new JFileChooser(currDir);

        menuBar = new JMenuBar();
        menuBar.setLayout(new GridLayout(1,2));
        file = new JMenu("File");
        save = new JMenuItem("Save");
        load = new JMenuItem("Load");
        file.add(save);
        file.add(load);
        save.addActionListener(this);
        load.addActionListener(this);

        addremoveColumn = new JMenu("Add Remove Columns");
        addremoveJMenuItems = new JMenuItem[addRemove.length];
        for(int x = 0; x < addRemove.length ; x++){
            addremoveJMenuItems[x] = new JMenuItem(addRemove[x]);
            addremoveJMenuItems[x].addActionListener(this);
            addremoveColumn.add(addremoveJMenuItems[x]);
        }

        instrumentMenu = new JMenu("Instruments");
        instrumentItems = new JMenuItem[instrumentNames.length];
        for(int x = 0; x < instrumentNames.length ; x++){
            instrumentItems[x] = new JMenuItem(instrumentNames[x]);
            instrumentItems[x].addActionListener(this);
            instrumentMenu.add(instrumentItems[x]);
        }

        menuBar.add(file);
        menuBar.add(instrumentMenu);
        menuBar.add(addremoveColumn);

        menuButtonPanel = new JPanel();
        menuButtonPanel.setLayout(new GridLayout(1,4));
        stopPlay = new JButton("Play");
        stopPlay.setBounds(50, 50, 150, 50);
        stopPlay.addActionListener(this);
        menuButtonPanel.add(stopPlay);
        clear = new JButton("Clear");
        clear.setBounds(50, 50, 150, 50);
        clear.addActionListener(this);
        menuButtonPanel.add(clear);
        fillRandom = new JButton("Fill Random");
        fillRandom.setBounds(50, 50, 150, 50);
        fillRandom.addActionListener(this);
        menuButtonPanel.add(fillRandom);
        restartButton = new JButton("Restart");
        restartButton.setBounds(50, 50, 150, 50);
        restartButton.addActionListener(this);
        menuButtonPanel.add(restartButton);
        menuBar.add(menuButtonPanel, BorderLayout.EAST);

        buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(buttonPane, BorderLayout.CENTER);
        this.add(tempoPanel, BorderLayout.SOUTH);
        this.add(menuBar, BorderLayout.NORTH);

		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread timing = new Thread(this);
		timing.start();
    

    }
    public static void main(String [] args){
        SoundBox app = new SoundBox();
    }
    public boolean getRandom(){
        return Math.random()<0.5;
    }
    public void saveSong(){
        FileFilter filter = new FileNameExtensionFilter("*.txt", ".txt");
        fileChooser.setFileFilter(filter);
        if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();

            try{
                String st = file.getAbsolutePath();
                if(st.indexOf(".txt")>0)
                    st = st.substring(0, st.length() - 4);
                    String output = "";
                    
                    for(int r = 0 ; r < button.length ; r++){
                        if(r==0){
                            output+=tempo;
                            for(int x = 0 ; x < button[0].length ; x++){
                                output+=" ";
                            }
                        }
                        else{
                            for(int c = 0 ; c<button[0].length ; c++){
                                if(button[r-1][c].isSelected())
                                    output+="x";
                                else output+="-";
                            }
                        }
                        output+="\n";
                    }

                    BufferedWriter outputStream = null;
                    outputStream=new BufferedWriter(new FileWriter(st+".txt"));
                    outputStream.write(output);
                    outputStream.close();
            }catch(IOException e){

            }
        }
    }
    public void setNotes(Character [][] notes){
        System.out.println("Setting Notes");
        buttonPane.remove(buttonPanel);

        buttonPanel = new JPanel();

        button = new JToggleButton[37][notes[0].length];
        buttonPanel.setLayout(new GridLayout(button.length, button[0].length));
        for(int r = 0 ; r < button.length ; r++){
            String name = clipNames[r].replaceAll("Sharp", "#");
		    for(int c = 0 ; c < button[0].length ; c++){
                button[r][c]=new JToggleButton();
                button[r][c].setFont(font);
                button[r][c].setText(name);
                button[r][c].setPreferredSize(new Dimension(30,30));
                button[r][c].setMargin(new Insets(0, 0, 0, 0));
                buttonPanel.add(button[r][c]);
            }
        }
        this.remove(buttonPane);
        buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(buttonPane, BorderLayout.CENTER);

        for(int r = 0 ; r<button.length ; r++){
            for(int c = 0 ; c < button[0].length ; c++){
                try{
                    if(notes[r][c] == 'x')
                        button[r][c].setSelected(true);
                    else 
                        button[r][c].setSelected(false);
                }
                catch(NullPointerException npe){}
                catch(ArrayIndexOutOfBoundsException ae){}
            }
        }
        this.revalidate();
    }

    public void resizeButtons(int number){
        JToggleButton[][]temp = new JToggleButton[button.length][button[0].length+number];
        for(int r = 0 ; r < temp.length ; r++){
            for(int c = 0; c < temp[0].length ; c++){
                temp[r][c] = new JToggleButton();
                try{
                    if(button[r][c].isSelected())
                        temp[r][c].setSelected(true);
                }catch(ArrayIndexOutOfBoundsException e){

                }
            }
        }

        buttonPane.remove(buttonPanel);
        buttonPanel = new JPanel();
        button = new JToggleButton[temp.length][temp[0].length];
        buttonPanel.setLayout(new GridLayout(button.length, button[0].length));
		for(int r = 0 ; r < button.length ; r++){
            String name = clipNames[r].replaceAll("Sharp", "#");
		    for(int c = 0 ; c < button[0].length ; c++){
                button[r][c]=new JToggleButton();
                button[r][c].setFont(font);
                button[r][c].setText(name);
                button[r][c].setPreferredSize(new Dimension(30,30));
                button[r][c].setMargin(new Insets(0, 0, 0, 0));
                buttonPanel.add(button[r][c]);
            }
        }
        this.remove(buttonPane);
        buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(buttonPane,BorderLayout.CENTER);

        for(int r = 0 ; r<temp.length ; r++){
            for(int c = 0 ; c < temp[0].length ; c++){
                try{
                    if(temp[r][c].isSelected())
                        button[r][c].setSelected(true);
                }
                catch(NullPointerException e){}
                catch(ArrayIndexOutOfBoundsException e){}

            }
        }
        this.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == save){
            saveSong();
        }
        if(e.getSource() == stopPlay){
            playing=!playing;
            if(!playing){
                stopPlay.setText("Play");
            }
            else{
                stopPlay.setText("Stop");
            }
        }
        if(e.getSource() == load){
            int returnVal = fileChooser.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                try{
                    File loadFile = fileChooser.getSelectedFile();
                    BufferedReader input = new BufferedReader(new FileReader(loadFile));
                    String temp;
                    temp=input.readLine();
                    tempo = Integer.parseInt(temp.substring(0,3));
                    tempoBar.setValue(tempo);
                    Character[][] song = new Character[button.length][temp.length()-2];

                    int r = 0;
                    while((temp=input.readLine())!=null){
                        for(int c=2;c<song[0].length-1;c++){
                            song[r][c-2]=temp.charAt(c);
                        }
                        r++;
                    }
                    setNotes(song);
                }catch(IOException ee){

                }
                col = 0;
                playing = false;
                stopPlay.setText("Play");

            }
        }

        if(e.getSource() == fillRandom){
            for(int r = 0 ; r < button.length ; r++){
                for(int c = 0 ; c < button[0].length;c++){
                    button[r][c].setSelected(getRandom());
                }
            }
            col = 0;
            playing = false;
            stopPlay.setText("Play");
        }
        if(e.getSource() == clear){
            for(int r = 0 ; r < button.length ; r++){
                for(int c = 0 ; c < button[0].length;c++){
                    button[r][c].setSelected(false);
                }
            }
            col = 0;
            playing = false;
            stopPlay.setText("Play");
        }
        if(e.getSource() == restartButton){
            col = 0;
            playing = true;
        }
        for(int y = 0 ; y < instrumentItems.length ; y++){
            if(e.getSource() == instrumentItems[y]){
                String selectedInstrument = "\\"+instrumentNames[y]+"\\"+instrumentNames[y];
                try{
                    for(int x = 0 ; x < clipNames.length ; x++){
                        URL url = this.getClass().getClassLoader().getResource(selectedInstrument+" - "+clipNames[x]+".wav");
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                        clip[x] = AudioSystem.getClip();
                        clip[x].open(audioIn);
                    }
                }
                catch(UnsupportedAudioFileException ee){
                    ee.printStackTrace();
                }
                catch(IOException ee){
                    ee.printStackTrace();
                }
                catch(LineUnavailableException ee){
                    ee.printStackTrace();
                }
                col = 0; 
                playing = false;
                stopPlay.setText("Play");
            }
        }
        for(int x = 0 ; x < addRemove.length ; x++){
            if(e.getSource() == addremoveJMenuItems[x]){
                if(x == 0){
                    resizeButtons(1);
                    playing=false;
                    stopPlay.setText("Play");
                }
                else if(x == 1){
                    resizeButtons(10);
                    playing=false;
                    stopPlay.setText("Play");
                }
                else if(x == 2){
                    if(button[0].length-1>1){
                        resizeButtons(-1);
                        playing = false;
                        col = button[0].length-1;
                        stopPlay.setText("Play");
                    }
                }
                else{
                    if(button[0].length-10>1){
                        resizeButtons(-10);
                        playing = false;
                        col = button[0].length-10;
                        stopPlay.setText("Play");
                    }
                }
            }
        }

    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        tempo = tempoBar.getValue();
        tempoLabel.setText(String.format("%s%6s", "Tempo: ", tempo));

    }

    @Override
    public void run() {
        do{
            try{
                if(!playing){
                    new Thread().sleep(0);
                }
                else{
                    for(int r = 0 ; r < button.length ; r++){
                        if(button[r][col].isSelected()){
                            clip[r].start();
                            button[r][col].setForeground(Color.GREEN);
                        }
                    }
                    new Thread().sleep(tempo);
                    for(int r = 0 ; r<button.length ; r++){
                        if(button[r][col].isSelected()){
                            clip[r].stop();
                            clip[r].setFramePosition(0);
                            button[r][col].setForeground(Color.BLACK);
                        }
                    }
                    col++;
                    if(col == button[0].length)
                        col = 0;
                }
            }catch(InterruptedException e){

            }
        }while(notStopped);
    }
}