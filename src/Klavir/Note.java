package Klavir;

import java.util.ArrayList;

public class Note extends Symbol{

	private String data;
	private boolean high;
	private int value;
	private int octave;
	private char key;
	private String name; //Mozda bude bio char
	
	public Note(int duration,char keyPressed) {
		super(duration);
		key=keyPressed;
		data=Symbol.noteMap.get(String.valueOf(key)).first();
		value=Symbol.noteMap.get(String.valueOf(key)).second();
		readData();
	}

	//Getteri
	public int getValue() {
		return value;
	}
	
	public int getOctave() {
		return octave;
	}
	
	public char getKey() {
		return key;
	}
	
	public boolean isHigh() {
		return high;
	}
	
	public String getName() {
		return name;
	}
	//
	
	
	public boolean areSame(ArrayList<String> externalList) {
		if(externalList.size()==0)return false;
		return externalList.get(0).equals(String.valueOf(this.getKey()));
	}
	
	
	//Read String data and put info from string to high,octave and name
	private void readData() {
		if(data.length()==3)high=true;
		else
			high=false;
		name=String.valueOf(data.charAt(0)); //Get name of note
		octave=Character.getNumericValue(data.charAt(data.length()-1)); //Get value of note
	}
	//
	
	//Override Metode
	@Override
	public boolean isNote() {
		return true;
	}

	@Override
	public boolean isComplex() {
		return false;
	}

	
	//Mora da se doradi
	@Override
	public String toString() { 
		return ""+data;
	}
	
	@Override
	public String toKeyString() {
		return Symbol.noteToKey.get(value);
	}
	//
}
