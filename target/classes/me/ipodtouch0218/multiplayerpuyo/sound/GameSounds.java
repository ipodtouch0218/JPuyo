package me.ipodtouch0218.multiplayerpuyo.sound;

import me.ipodtouch0218.java2dengine.sound.Sound;

public enum GameSounds {
	
	CHAIN_1("chain/chain1.wav"), 
	CHAIN_2("chain/chain2.wav"), 
	CHAIN_3("chain/chain3.wav"), 
	CHAIN_4("chain/chain4.wav"), 
	CHAIN_5("chain/chain5.wav"), 
	CHAIN_6("chain/chain6.wav"), 
	CHAIN_7("chain/chain7.wav"), 
  
	HEAVY("chain/heavy.wav"), 
	DROP("drop.wav"), 
	MOVE("move.wav"), 
	ROTATE("rotate.wav"), 
	LOSE("lose.wav"), 
  
	GARBAGE_DROP_0("garbage/drop0.wav"),
	GARBAGE_DROP_1("garbage/drop1.wav"), 
   
	GARBAGE_SEND_0("garbage/send0.wav"), 
	GARBAGE_SEND_1("garbage/send1.wav"),
	GARBAGE_SEND_2("garbage/send2.wav"),
	GARBAGE_SEND_3("garbage/send3.wav"),
	GARBAGE_SEND_4("garbage/send4.wav"),
  
	ALL_CLEAR("allclear.wav"),
  
	TIMER("timer.wav"),
	TIMER_STOP("timerStop.wav"),
	FEVER_FLIP("feverFlip.wav"),
	FEVER_METER_FILL("feverMeter.wav"),
	FEVER_START("feverStart.wav"),
	
	ICE_CHIP("iceChip.wav"),
	ICE_BREAK("iceBreak.wav");
  
	private static Soundpack soundpack = Soundpack.BIT;
	private static boolean isMuted;
	private String dir;
  
	private GameSounds(String dir) {
		this.dir = dir;
	}
  
	public void play() {
		if (isMuted) return;
		if (getSound() == null) return;
		try {
			getSound().play();
		} catch (Exception e) {}
	}
  
	public Sound getSound() { return soundpack.getSound(this); }
	public String getPath() { return dir; }
  
	public static void setSoundpack(Soundpack pack) { soundpack = pack; }
	public static Soundpack getSoundpack() { return soundpack; }
	public static boolean isMuted() { return isMuted; }
	public static void setMuted(boolean value) { isMuted = value; }
}