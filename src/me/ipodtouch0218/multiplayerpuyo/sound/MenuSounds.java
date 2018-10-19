package me.ipodtouch0218.multiplayerpuyo.sound;

import me.ipodtouch0218.java2dengine.sound.Sound;

public enum MenuSounds {

	MOVE("move.wav");
	
	private Sound sound;
	private String file;
	
	MenuSounds(String filename) {
		this.file = filename;
		sound = Sound.getSound("menu/" + filename, false);
	}
	
	public Sound getSound() {
		return sound;
	}
	public String getFile() {
		return file;
	}
	
}
