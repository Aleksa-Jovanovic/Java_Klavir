package Klavir;

import java.util.ArrayList;

public class ComplexNote extends Symbol{

	private ArrayList<Note> noteList;
	
	//Za sad samo moze da se napravi kad mu se prosledi lisa nota koje cine kompleksnu notu
	public ComplexNote(int duration,ArrayList<Note> list) {
		super(duration);
		noteList=list;
	}

	public int numOfNotes() {
		return noteList.size();
	}
	
	public Note getNote(int index) {
		return noteList.get(index);
	}
	
	public ArrayList<Note> getNoteList(){
		return noteList;
	}
	
	//Override Metode
	@Override
	public boolean isNote() {
		return true;
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("|");
		noteList.stream().forEach(e->{
			sb.append(e.toString()+"|");
		});
		//s.deleteCharAt(s.length()-1);
		return sb.toString();
	}
	
	public boolean areSame(ArrayList<String> externalList) {
		boolean flag=true;
		if(noteList.size()!=externalList.size())return false;
		for(int i=0;i<externalList.size();i++) {
			for(int j=0;j<noteList.size();j++) {
				flag = externalList.get(i).equals(String.valueOf(noteList.get(i).getKey()));
				if(flag)break;
			}
			if(!flag)break;
		}
		return flag;
	}
	
	public boolean areDifferent(ArrayList<String> externalList) {
		if(noteList.size()<externalList.size())return true;
		String testKey;
		for(int i=0;i<externalList.size();i++) {
			testKey=externalList.get(i);
			int match=0;
			for(int j=0;j<noteList.size();j++) {
				if(testKey.equals(String.valueOf(noteList.get(j).getKey())))
					match++;
			}
			if(match==0) return true;
		}
		return false;
	}
	
	@Override
	public String toKeyString() {
		StringBuffer sb = new StringBuffer();
		sb.append("|");
		noteList.stream().forEach(e->{
			sb.append(Symbol.noteToKey.get(e.getValue())+"|");
		});
		//s.deleteCharAt(s.length()-1);
		return sb.toString();
	}

	
}
