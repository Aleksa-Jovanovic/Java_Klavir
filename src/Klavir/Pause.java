package Klavir;

public class Pause extends Symbol{

	public Pause(int duration) {
		super(duration);
	}

	@Override
	public boolean isNote() {
		return false;
	}

	@Override
	public boolean isComplex() {
		return false;
	}

	@Override
	public String toString() {
		return "";
	}

	public boolean areSame() {
		
		try {
			if (symbolDuration == 4)
				Thread.sleep(Klavir.TIME_LONG);
			else
				Thread.sleep(Klavir.TIME_SHORT);
		} catch (InterruptedException e) {}
		Composition.incIndex();
		Klavir.writeNotes();
		return true;
	}
}
