package me.ipodtouch0218.multiplayerpuyo.sound;

import me.ipodtouch0218.java2dengine.sound.StreamedSound;

public enum GameMusic {
	
	CLASSIC("classic.wav", 3484000, -1), 
	BIT("8bit.wav"), 
	MEANBEAN("meanbean.wav"),
	FEVER("fever.wav", 2457697, 4836510),
	PANIC("panic.wav", 692500, -1);
	
	private String soundDir;
	private int loopStart;
	private int loopEnd;
	private StreamedSound sound;
	
	private GameMusic(String soundDir) { this(soundDir, -1, -1); }
	  
	private GameMusic(String soundDir, int loopStart, int loopEnd) {
		this.soundDir = soundDir;
	    this.loopStart = loopStart;
	    this.loopEnd = loopEnd;
	    sound = new StreamedSound("music/" + soundDir);
	}
	  
	public void start(long startPos) {
	    int pos = (int)(startPos % sound.getFrameLength());
	    sound.setStartingPos(pos);
	    sound.loop(loopStart,loopEnd,-1);
	}
	  
	public void stop() { sound.stop(); }
	  
	public StreamedSound getSound() { return sound; }
	public int getLoopStart() { return loopStart; }
	public int getLoopEnd() { return loopEnd; }
	public String getSoundDir() { return soundDir; }
}