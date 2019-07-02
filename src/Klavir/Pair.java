package Klavir;

public class Pair<K,V> {

	private K firstPair;
	private V secondPair;
	
	public K first() {
		return firstPair;
	}
	
	public V second() {
		return secondPair;
	}
	
	public Pair (K firstElem, V secondElem){
		firstPair=firstElem;
		secondPair=secondElem;
	}
}
