package Klavir;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.midi.MidiUnavailableException;

public class Composition extends Thread{

	public static final int SHORT_PAUSE = 200;
	public static final int LONG_PAUSE = 400;
	
	//Sve note i pauze iz kompozicije redom postavljene
	private static ArrayList<Symbol> listOfSymbols;
	private boolean pause=true;
	public MidiPlayer player;
	private static int index;
	
	public Composition() {
		listOfSymbols = new ArrayList<>();
		createPlayer();
	}
	
	public static ArrayList<Symbol> getSymbolList(){
		return listOfSymbols;
	}
	public static int getIndex() {
		return index;
	}
	public static void incIndex() {
		index++;
	}
	public static void clearIndex() {
		index=0;
	}
	
	private void createPlayer() {

		try {
			player = new MidiPlayer();
		} catch (MidiUnavailableException e) {}
	}
	
	/**
	 * 
	 * @param listOfKeys Niz nota moze biti jedna ili vise
	 * @param length Duzina trajanja note ili 8 ili 4 vrednost
	 */
	public static void addSymbol(ArrayList<String> listOfKeys, int length) {
		if(listOfKeys.size()>1) {
			//Complexna
			ArrayList<Note> notes = new ArrayList<>();
			for(String s:listOfKeys) {
				notes.add(new Note(length, s.toCharArray()[0]));
			}
			listOfSymbols.add(new ComplexNote(length, notes));
		}else if(listOfKeys.size()>0){
			listOfSymbols.add(new Note(length, listOfKeys.get(0).toCharArray()[0]));
		}
	}
	
	public static void addSymbol(Symbol symbol) {
		listOfSymbols.add(symbol);
	}
	
	public static void clearComposition() {
		listOfSymbols.clear();
		Klavir.writeNotes();
	}
	
	public static boolean isEmpty() {
		return listOfSymbols.isEmpty();
	}
	
	//Za ucitavanje nove kompozicije
	public void importCompostion(String fileName) {
		listOfSymbols.clear();
		index=0;
		readFile(fileName);
		restartComposition();
	}
	
	//Za exportovanje kompozicije
	public void exportTXT() {
		if(listOfSymbols.isEmpty())
			//Lista je prazna i nista se ne eksportuje
			return ;
		//List se prebacuje u txt 
	}
	
	
	//Run------------------------------------------------------------------------------------------
	@Override
	public void run() {
		try {
			index = 0;
			while(!interrupted()) {
				synchronized (this) {
					while(pause) {
						wait();
					}
				}
				if(listOfSymbols.size()==0) {
					pause=true;
					continue;
				}
				//Ovde se poziva nesto sto ce svirati note
				Klavir.writeNotes();
				playSymbol();
			}
		} catch (InterruptedException e) {}
	}
	//Kraj run-a
	

	//Mozda ne mora synchronized
	private synchronized void playSymbol() throws InterruptedException{
		if(listOfSymbols.get(index).isNote()) {
			if(listOfSymbols.get(index).isComplex()) {
				//Kompleksna nota
				int dur = listOfSymbols.get(index).getDuration();
				Klavir.pressKeys(((ComplexNote)listOfSymbols.get(index)).getNoteList());
				player.playComplexNote(((ComplexNote)listOfSymbols.get(index)).getNoteList(), dur==4?LONG_PAUSE:SHORT_PAUSE);
				Klavir.releaseKeys(((ComplexNote)listOfSymbols.get(index)).getNoteList());
			}else {
				//Obicna nota
				int dur = listOfSymbols.get(index).getDuration();
				ArrayList<Note> helperList = new ArrayList<Note>();
				helperList.add(((Note)listOfSymbols.get(index)));
				Klavir.pressKeys(helperList);
				//Mozda ovde bude bio problem!!!
				player.play(((Note)listOfSymbols.get(index)).getValue(), dur==4?LONG_PAUSE:SHORT_PAUSE);
				Klavir.releaseKeys(helperList);
				helperList.clear();
			}
		}else {
			//Pauza
			if(listOfSymbols.get(index).getDuration()==4)
				sleep(LONG_PAUSE);
			else
				sleep(SHORT_PAUSE);
		}
		//Pomeranje indeksa
		index++;
		if(listOfSymbols.size()==index) {
			index=0;
			Klavir.play.setEnabled(true);
			//Treba da se restartuju labele koje se crtaju
			pause=true;
		}
		//Klavir.shiftLabels();
	}
	//
	//ReadFile----------------------------------------------------------------------
	public void readFile(String fileName) {
		String dir="data/";
		String postfix=".txt";
		
		fileName=dir+fileName+postfix;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			Stream<String> stream = br.lines();
			stream.forEach(e->{
				//Parsiranje
				Pattern mainReg = Pattern.compile("([^\\[]*)\\[([^\\]]*)\\]");
				Pattern inBracketsReg = Pattern.compile("([#a-zA-Z0-9]+)");
				Pattern outBracketsReg = Pattern.compile("([ ]*)([|#a-zA-Z0-9]+)([ ]*)");
				
				Matcher match = mainReg.matcher(e);
				if(!match.find()) {
					outBracketsHandle(e,outBracketsReg);
				}else {
					match.reset();
					while(match.find()) {
						String outBrackets  = match.group(1);
						String inBrackets = match.group(2);
						outBracketsHandle(outBrackets,outBracketsReg);
						inBracketsHandle(inBrackets,inBracketsReg);
					}	
				}
					
			});
			br.close();	
				
		} catch (IOException e) {
			//e.printStackTrace();
			listOfSymbols.clear();
		}
	}
	
	private void inBracketsHandle(String e, Pattern inBracketsReg) {
		String symbol;
		Matcher match = inBracketsReg.matcher(e);
		ArrayList<Note> noteList = new ArrayList<Note>();
		while(match.find()) {
			symbol=match.group(1);
			if(symbol.length()==1) { //Note se sviraju odvojeno ali duzine 1/8
				char i = symbol.toCharArray()[0];
				if(!keyValid(i)) { //Ako kljuc nije validan!
					continue;
				}
				if(i!='|')
					listOfSymbols.add(new Note(8, i));
			}
			else {
				//Kada se note sviraju istovremeno
				noteList = new ArrayList<Note>();
				for(char i:symbol.toCharArray()) {
					if(!keyValid(i)) { //Ako kljuc nije validan!
						continue;
					}
					noteList.add(new Note(4, i));
				}
				listOfSymbols.add(new ComplexNote(4, noteList));
			}
		}
	}

	private void outBracketsHandle(String e, Pattern outBracketsReg) {
		String spaceFirst;
		String spaceSecond;
		String symbol;
		Matcher match = outBracketsReg.matcher(e);
		while(match.find()) {
			spaceFirst=match.group(1);
			symbol=match.group(2);
			spaceSecond=match.group(3);
			for(int i=0;i<spaceFirst.length();i++)
				listOfSymbols.add(new Pause(8));
			
			for(char i:symbol.toCharArray()) {
				if(!keyValid(i)) { //Ako kljuc nije validan!
					continue;
				}
				if(i=='|')
					listOfSymbols.add(new Pause(4));
				else
					listOfSymbols.add(new Note(4, i));
			}
			
			for(int i=0;i<spaceSecond.length();i++)
				listOfSymbols.add(new Pause(8));
		}
		
	}

	//
	

	private boolean keyValid(char key) {
		return Symbol.noteMap.keySet().stream().anyMatch(e -> e.equals(String.valueOf(key)));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Symbol s:listOfSymbols) {
			sb.append(s.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	//Kontrola niti -------------------------------------------------------------------------------
	public synchronized void startComposition() {
		if(pause) {
			if(!this.isAlive())
				start();
			pause=false;
			notify();
		}
	}
	
	public void pauseComposition() {
		pause=true;
		Klavir.play.setEnabled(true);
	}
	
	public void restartComposition() {
		pause=true;
		index = 0;
		if(listOfSymbols.size()>0 && !Composition.getSymbolList().get(0).isNote()) 
			index++;
		Klavir.play.setEnabled(true);
		Klavir.writeNotes();
		//Restartovanje labela 
		//Tipa pozove se fukcija koja na osnovu indexa crta stvari
	}
	public void turnOff() {
		this.interrupt();
	}
	//
	
}
