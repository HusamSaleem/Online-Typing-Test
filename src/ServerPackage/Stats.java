package ServerPackage;

public class Stats {
	private String wpm;
	private String accuracy;
	
	public Stats() {
		this.wpm = "";
		this.accuracy = "";
	}
	
	public Stats(String wpm, String accuracy) {
		this.wpm = wpm;
		this.accuracy = accuracy;
	}

	public int getAccuracy() {
		return (int) Float.parseFloat(accuracy);
	}

	public void setAccuracy(String accuracy) {
		this.accuracy = accuracy;
	}

	public int getWpm() {
		return (int) Float.parseFloat(wpm);
	}

	public void setWpm(String wpm) {
		this.wpm = wpm;
	}
}
