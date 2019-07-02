package Klavir;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;

public class Klavir extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<JLabel> whiteLabel = new ArrayList<>();
	private static ArrayList<JButton> whiteButton = new ArrayList<>(); //Ima ih 35
	private static final int NUM_WHITE_KEYS=35;
	private ArrayList<JLabel> blackLabel = new ArrayList<>();
	private static ArrayList<JButton> blackButton = new ArrayList<>(); //Ima ih 25
	private static final int NUM_BLACK_KEYS=25;
	private static ArrayList<JLabel> screenLabel = new ArrayList<>();
	private static boolean notesOverKeys=false;
	public static JButton play = new JButton("Play");
	
	
	private static ImageIcon white = new ImageIcon("images/whiteTile.png");
	private static ImageIcon black = new ImageIcon("images/blackTile.png");
	private static ImageIcon pressedWhite = new ImageIcon("images/pressedWhiteTile.png");
	private static ImageIcon pressedBlack = new ImageIcon("images/pressedBlackTile.png");
	
	//private String[] lowName = {"C", "D", "E", "F", "G", "A", "B"};
	//private String[] highName = {"C#", "D#", "F#", "G#", "A#"};
	
	
	private Composition c = new Composition();
	
	public static final long TIME_LONG = 400; //Kada izmedju nota ima velika pazua
	public static final long TIME_SHORT = 250; //Kada izmedju nota ima mala pauza
	private static final long TIME_NOBREAK = 100; //Kada se note sviraju jedna za drugom bez pauze
	//private static final long TIME_SAME = 1; //Kada je nota akord
	private boolean record = false; //Kada snima a kada ne
	private long timePressed=0;
	private long timeReleased=0;
	private long noteLenght;
	private long pauseLength;
	private ArrayList<String> pressedKeys = new ArrayList<>();

	
	//Konstruktor-------------------------------------------------------------------------
	public Klavir() {
		super("Piano");
		ImageIcon mainIcon = new ImageIcon("images/klavir.png");
		this.setIconImage(mainIcon.getImage());
		Symbol.loadMap();
		c.readFile("fur_elise"); // Ovo nece ici ovde
		setSize(white.getIconWidth()*NUM_WHITE_KEYS,white.getIconHeight()*5);
		setResizable(false);
		this.setLayout(new BorderLayout());
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				c.turnOff();
				dispose();
			}
		});
		//Dodavanje elemenata---------------------------------------------------------------------------------------------------
		addComponent();
		setVisible(true);
	}
	//-------------------------------------------------------------------------------------
	
	
	private class ListenForKeys extends KeyAdapter{
		int index;
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_SHIFT)return;
			String key = String.valueOf(e.getKeyChar());
			
			//Dodavanje note
			
			timePressed = e.getWhen();
			if (record) { // Ako se snima prvo se doda prethodna pauza (Nota se dodaje u Release delu)
				// Dodavanje pauze
				if (timeReleased > 0) {
					pauseLength = timePressed - timeReleased;
					if (pauseLength > TIME_NOBREAK) {
						if (pauseLength > TIME_SHORT)
							Composition.addSymbol(new Pause(4));
						else
							Composition.addSymbol(new Pause(8));
					}
					// Dodavanje note u listu
					if(!pressedKeys.contains(key))
						pressedKeys.add(key);
				}
			}else if(!Composition.isEmpty()) { //Ovaj deo je za igru!
				if(!pressedKeys.contains(key))
					pressedKeys.add(key);
				/**Symbol symbolInComposition = Composition.getSymbolList().get(Composition.getIndex());
				if(symbolInComposition.isComplex()) {
					//Kompleksna nota
					if(((ComplexNote)symbolInComposition).areSame(pressedKeys)) {
						pressedKeys.clear();
						Composition.incIndex();
						writeNotes();	
					}else if(((ComplexNote)symbolInComposition).areDifferent(pressedKeys))
						pressedKeys.clear();
				}*/
			}
			
			
			//Sviranje note
			if(Symbol.whiteKeys.contains(key)) {
				//Belo dugme
				index=Symbol.whiteKeys.indexOf(key);
				whiteButton.get(index).setIcon(pressedWhite);
				c.player.play(Symbol.noteMap.get(key).second());
			}else {
				//Crno dugme
				index=Symbol.blackKeys.indexOf(key);
				blackButton.get(index).setIcon(pressedBlack);
				c.player.play(Symbol.noteMap.get(key).second());
			}
			
			
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_SHIFT)return;
			String key = String.valueOf(e.getKeyChar());
			
			timeReleased=e.getWhen();
			noteLenght=timeReleased - timePressed;
			
			//Dodavanje note
			if(record) {
				if(noteLenght>TIME_SHORT)
					Composition.addSymbol(pressedKeys, 4);
				else
					Composition.addSymbol(pressedKeys, 8);
				pressedKeys.clear();
			}
			
			//Ovo je deo za IGRU
			else if((!Composition.isEmpty()) && Composition.getIndex() < Composition.getSymbolList().size()) { 
				Symbol symbolInComposition = Composition.getSymbolList().get(Composition.getIndex());
				//System.out.println(Composition.getIndex());
				//System.out.println(((Note)symbolInComposition).getKey());
				if(symbolInComposition.isComplex()) {
					//Kompleksna nota
					if(((ComplexNote)symbolInComposition).areSame(pressedKeys)) {
						pressedKeys.clear();
						Composition.incIndex();
						writeNotes();	
					}else if(((ComplexNote)symbolInComposition).areDifferent(pressedKeys))
						pressedKeys.clear();
				}else if(symbolInComposition.isNote()) {
					//Obicna nota
					if(((Note)symbolInComposition).areSame(pressedKeys)) {
						pressedKeys.clear();
						Composition.incIndex();
						writeNotes();
					}else
						pressedKeys.clear();
				}
				if(Composition.getIndex()==Composition.getSymbolList().size()){
					Composition.clearIndex();
					writeNotes();
				}
			}
			
			
			//Sviranje note
			if(Symbol.whiteKeys.contains(key)) {
				//Belo dugme
				index=Symbol.whiteKeys.indexOf(key);
				whiteButton.get(index).setIcon(white);
				c.player.release(Symbol.noteMap.get(key).second());
			}else {
				//Crno dugme
				index=Symbol.blackKeys.indexOf(key);
				blackButton.get(index).setIcon(black);
				c.player.release(Symbol.noteMap.get(key).second());
			}
			
			if ((!Composition.isEmpty())) {
				// Pomeranje preostalih pauza
				while (Composition.getIndex() < Composition.getSymbolList().size()
						&& !Composition.getSymbolList().get(Composition.getIndex()).isNote()) {
					((Pause) Composition.getSymbolList().get(Composition.getIndex())).areSame();
				}

				if (Composition.getIndex() == Composition.getSymbolList().size()) {
					Composition.clearIndex();
					writeNotes();
				}
			}
			
		}
		
	}
	
	private class ListenForMouse extends MouseAdapter{
		int index;
		
		@Override
		public void mousePressed(MouseEvent e) {
			//Snimanje pause ako treba
			if(record) {
				timePressed=e.getWhen();
				if(timeReleased>0) {
					pauseLength=timePressed-timeReleased;
					if(pauseLength>TIME_SHORT)
						Composition.addSymbol(new Pause(4));
					else if(pauseLength>TIME_NOBREAK)
						Composition.addSymbol(new Pause(8));
				}
			}
			
			
			JButton button = ((JButton)e.getSource());
			if(whiteButton.contains(button)) {
				button.setIcon(pressedWhite);
				index=whiteButton.indexOf(button);
				String key=Symbol.whiteKeys.get(index);
				c.player.play(Symbol.noteMap.get(key).second());	
			}
			else {
				button.setIcon(pressedBlack);
				index=blackButton.indexOf(button);
				String key=Symbol.blackKeys.get(index);
				c.player.play(Symbol.noteMap.get(key).second());
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			//Racunanje duzine note
			if(record) {
				timeReleased=e.getWhen();
				noteLenght=timeReleased-timePressed;
			}
			
			JButton button = ((JButton)e.getSource());
			if(whiteButton.contains(button)) {
				button.setIcon(white);
				String key=Symbol.whiteKeys.get(index);
				c.player.release(Symbol.noteMap.get(key).second());
				if(record)
					if(noteLenght>TIME_SHORT)
						Composition.addSymbol(new Note(4,key.toCharArray()[0]));
					else
						Composition.addSymbol(new Note(8,key.toCharArray()[0]));
			}
			else {
				button.setIcon(black);
				String key=Symbol.blackKeys.get(index);
				c.player.release(Symbol.noteMap.get(key).second());
				if(record)
					if(noteLenght>TIME_SHORT)
						Composition.addSymbol(new Note(4,key.toCharArray()[0]));
					else
						Composition.addSymbol(new Note(8,key.toCharArray()[0]));
			}
		}
	}
	ListenForMouse mouse = new ListenForMouse();
	ListenForKeys keyboard = new ListenForKeys();
	
	//Record-------------------------------------------
	private void recordStart() {
		timePressed=0;
		timeReleased=0;
		record=true;
	}
	private void recordStop() {
		record=false;
		timePressed=0;
		timeReleased=0;
	}
	
	//AddComponent-------------------------------
	private void addComponent() {
		//Zapad
		play.addActionListener(e->{
			c.startComposition();
			play.setEnabled(false);
		});
		JButton pause = new JButton("Pause");
		pause.addActionListener(e->{
			c.pauseComposition();
		});
		JButton notesOverKyesButton = new JButton("Notes/Keys");
		notesOverKyesButton.addActionListener(e->{
			notesOverKeys=!notesOverKeys;
		});
		JButton restart = new JButton("Restart");
		restart.addActionListener(e->{
			c.restartComposition();
		});
		JButton recordButton = new JButton("Record");
		JButton stopRecordButton = new JButton("Stop \nRecording");
		stopRecordButton.setEnabled(false);
		recordButton.addActionListener(e->{
			recordStart(); /////MOZDA SAMO DA SE STAVI record=true !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			recordButton.setEnabled(false);
			stopRecordButton.setEnabled(true);
		});
		stopRecordButton.addActionListener(e->{
			recordStop();
			recordButton.setEnabled(true);
			stopRecordButton.setEnabled(false);
		});
		JButton clearComposition = new JButton("Clear Composition");
		clearComposition.setPreferredSize(new Dimension(this.getWidth()/8,50));
		clearComposition.addActionListener(e->{
			Composition.clearComposition();
		});
		JPanel dugmici = new JPanel();
		dugmici.setLayout(new GridLayout(2, 1, 0, 0));
		dugmici.setBackground(Color.LIGHT_GRAY);
		add(dugmici,"East");
		
		JPanel panelUp = new JPanel();
		//JPanel panelUpRead = new JPanel();
		JPanel panelRead1 = new JPanel();
		panelRead1.setLayout(new BorderLayout());
		JPanel panelRead2 = new JPanel();
		Border panelBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2);
		JButton readButton = new JButton("Import Composition");
		JTextField textField = new JTextField(20);
		readButton.addActionListener(e->{
			String str = textField.getText();
			if(str.length()==0) {
				textField.setText("Unesite ime kompozicije!");
				return;
			}
			c.restartComposition();
			c.importCompostion(str);
			if(Composition.getSymbolList().size()==0)
				textField.setText("Uneto pogresno ime kompozicije!");
		});
		panelRead1.add(readButton,BorderLayout.SOUTH);
		panelRead2.add(textField);
		panelUp.add(panelRead1);
		panelUp.add(panelRead2);
		
		JPanel panelDown = new JPanel();
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();
		
		panelUp.setPreferredSize(new Dimension(this.getWidth()/10,this.getHeight()/3));
		panelUp.setBorder(panelBorder);
		panelUp.setLayout(new GridLayout(2,1,0,0));
		panelDown.setLayout(new GridLayout(4,1,0,0));
		panelDown.setBorder(panelBorder);
		
		panel2.add(play);
		panel2.add(pause);
		panel2.add(restart);
		panel1.add(notesOverKyesButton);
		panel4.add(recordButton);
		panel4.add(stopRecordButton);
		panel3.add(clearComposition);
		
		panelDown.add(panel1);
		panelDown.add(panel2);
		panelDown.add(panel3);
		panelDown.add(panel4);
		
		dugmici.add(panelUp);
		dugmici.add(panelDown);
		//Jug-------------------------------------------------
		JLayeredPane keysPanel = new JLayeredPane();
		keysPanel.setPreferredSize(new Dimension(this.getWidth(), white.getIconHeight()));
		//keysPanel.setLayout(new BoxLayout(keysPanel, BoxLayout.X_AXIS));
		add(keysPanel,"South");
		
			//Dodavanje dugmica
		int x=0;
		int y=keysPanel.getY();
		int razlika = (white.getIconWidth() - black.getIconWidth())/2;
		int[] newBlackX = { x+white.getIconWidth()/2 + razlika, x+white.getIconWidth()/2 * 3 + razlika, x+white.getIconWidth() * 3 + white.getIconWidth()/2 + razlika, x+white.getIconWidth() * 4 + white.getIconWidth()/2 + razlika,
				x+white.getIconWidth() * 5 +white.getIconWidth()/2 + razlika };
		
			//Postavljanje belih dirki i imena
		for (int i = 0; i < NUM_WHITE_KEYS; i++) {
			JButton button = new JButton(white);
			button.setBounds(x, y, white.getIconWidth(), white.getIconHeight());
			button.addMouseListener(mouse);
			button.addKeyListener(keyboard);
			whiteButton.add(i, button);
			//button.setPressedIcon(new ImageIcon("images/pressedWhiteTile.png"));
			JLabel label = new JLabel(Symbol.whiteKeys.get(i), JLabel.CENTER); //Default txt je dugme sa tastature
			label.setBounds(x, y + white.getIconHeight()/6*5, white.getIconWidth(), white.getIconHeight()/6);
			whiteLabel.add(i, label);
			x += white.getIconWidth();
			keysPanel.add(button, new Integer(1));
			keysPanel.add(label, new Integer(2));
		}
		
			//Postavljanje crnih dirki i imena
		for (int i = 0; i < NUM_BLACK_KEYS; i++) {
			JButton button = new JButton(black);
			button.setBounds(newBlackX[i/5], y, black.getIconWidth(), black.getIconHeight());
			button.addMouseListener(mouse);
			button.addKeyListener(keyboard);
			blackButton.add(i, button);
			//button.setPressedIcon(new ImageIcon("images/pressedBlackTile.png"));
			JLabel label = new JLabel(Symbol.blackKeys.get(i), JLabel.CENTER);
			label.setBounds(newBlackX[i/5], y + black.getIconHeight()/5*4, black.getIconWidth(), black.getIconHeight()/5);
			label.setForeground(Color.WHITE);
			blackLabel.add(i, label);
			newBlackX[i/5]+=white.getIconWidth()*7;
			//newX+=white.getIconWidth()*2;
			keysPanel.add(button, new Integer(3));
			keysPanel.add(label, new Integer(4));
		} 
		//LABELE---------------------------------------------------------------------------------------------------
		JPanel westPanel = new JPanel();
		westPanel.setPreferredSize(new Dimension(white.getIconWidth()*2, this.getHeight()-keysPanel.getHeight()));
		westPanel.setBackground(Color.LIGHT_GRAY);
		add(westPanel,"West");
		//Centar-Labels----------------------------------------
		JPanel labelPanel = new JPanel();
		//labelPanel.setPreferredSize(new Dimension(this.getWidth()/2,this.getHeight()-keysPanel.getHeight()));
		labelPanel.setBackground(Color.LIGHT_GRAY);
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,this.getHeight()/3));
		//GridBagConstraints gbc = new GridBagConstraints();
		add(labelPanel,"Center");
		
		JLabel label;
		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
		for(int i=0;i<8;i++) {
			label=new JLabel("/", JLabel.CENTER);
			label.setOpaque(true);
			label.setBorder(border);
			label.setPreferredSize(new Dimension(this.getWidth()/12, 80));
			screenLabel.add(label);
			labelPanel.add(label);
		}
		
		
		//Meni koji ima samo export --------------------------------------
		MenuBar menuBar = new MenuBar();
		setMenuBar(menuBar);
		Menu menu = new Menu("File");
		menuBar.add(menu);
		MenuItem exportTxt = new MenuItem("Export to .txt");
		menu.add(exportTxt);
		exportTxt.addActionListener(e->{
			c.exportTXT();
		});
		
	}
	
	public static void writeNotes() {
		
		// Pisi note
		for (int i = 0; i < screenLabel.size(); i++) {
			if (Composition.getIndex() + i >= Composition.getSymbolList().size()) {
				screenLabel.get(i).setText("/");
				screenLabel.get(i).setBackground(Color.WHITE);
				screenLabel.get(i).setForeground(new Color(0,0,0));
				screenLabel.get(i).setPreferredSize(new Dimension(white.getIconWidth() * NUM_WHITE_KEYS / 12, 80));
				continue;
			}
			Symbol symbol = Composition.getSymbolList().get(Composition.getIndex() + i);
			String txt;
			screenLabel.get(i).setForeground(new Color(200,255,255));
			if (notesOverKeys)
				txt = symbol.toString();
			else
				txt = symbol.toKeyString();
			screenLabel.get(i).setText(txt);
			int width, height;

			if (symbol.isComplex())
				height = 80;
			else
				height = 40;

			if (symbol.getDuration() == 4) {
				width = white.getIconWidth() * NUM_WHITE_KEYS / 12;
				screenLabel.get(i).setBackground(new Color(150, 0, 0));
			} else {
				width = white.getIconWidth() * NUM_WHITE_KEYS / 12 / 2;
				screenLabel.get(i).setBackground(new Color(0, 120, 0));
			}
			screenLabel.get(i).setPreferredSize(new Dimension(width, height));
		}

	}
	
	
	public static void pressKeys(ArrayList<Note>pressedKeys) {
		pressedKeys.stream().forEach(e->{
			String key =String.valueOf(e.getKey());
			if(Symbol.whiteKeys.contains(key)) {
				int location = Symbol.whiteKeys.indexOf(key);
				whiteButton.get(location).setIcon(pressedWhite);
			}else {
				int location = Symbol.blackKeys.indexOf(key);
				blackButton.get(location).setIcon(pressedBlack);
			}
		});
	}
	
	public static void releaseKeys(ArrayList<Note>pressedKeys) {
		pressedKeys.stream().forEach(e->{
			String key =String.valueOf(e.getKey());
			if(Symbol.whiteKeys.contains(key)) {
				int location = Symbol.whiteKeys.indexOf(key);
				whiteButton.get(location).setIcon(white);
			}else {
				int location = Symbol.blackKeys.indexOf(key);
				blackButton.get(location).setIcon(black);
			}
		});
	}
	
	public static void main(String[] args) {
		new Klavir();
	}
}	

	
