package Klavir;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.stream.Stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public abstract class Symbol {

	protected static Map<String, Pair <String,Integer>> noteMap = new HashMap<String, Pair<String,Integer>>(); //Glavna mapa
	protected static Map<String,Integer> keyMap = new HashMap<String,Integer>();
	protected static Map<Integer,String> noteToKey = new HashMap<Integer,String>(); //Mapa nota u key
	public static ArrayList<String> whiteKeys = new ArrayList<>();
	public static ArrayList<String> blackKeys = new ArrayList<>();
	
	protected int symbolDuration; //Moze biti 4(1/4) ili 8(1/8)
	
	public Symbol(int duration) {
		symbolDuration=duration;
	}
	
	public abstract boolean isNote();
	public abstract boolean isComplex();
	public String toKeyString() {return"";}
	//public abstract boolean areSame(ArrayList<Note> externalList);
	
	public int getDuration() {
		return symbolDuration;
	}
	
	//Ucitavanje mape!
	public static void loadMap() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data/map.csv")));
			Stream<String> stream = br.lines();
			stream.forEach(e->{
				//Parsiranje
				Pattern pattern = Pattern.compile("^(.*),(.*),(.*)$");
				Matcher match = pattern.matcher(e);
				if(match.matches()) {
					String mapKey = match.group(1);
					String name = match.group(2);
					Integer value = Integer.parseInt(match.group(3));
					
					//Dodavanje u mape
					keyMap.put(name, value);
					noteToKey.put(value, mapKey);
					noteMap.put(mapKey, new Pair<String, Integer>(name, value));
					
					//Dodavanje u niz kljuceva koji ce biti sinhronizovani sa nizom dirki!
					if(name.length()==2)
						whiteKeys.add(mapKey);
					else
						blackKeys.add(mapKey);
				}
			});
			br.close();
		} catch (IOException e) {}
	}
	
	
}	
	
