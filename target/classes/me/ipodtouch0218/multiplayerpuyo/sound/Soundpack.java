package me.ipodtouch0218.multiplayerpuyo.sound;

import java.util.HashMap;
import me.ipodtouch0218.java2dengine.sound.Sound;
import me.ipodtouch0218.multiplayerpuyo.misc.LoadingListener;


public enum Soundpack {
	BIT("8bit/"), 
	CLASSIC("classic/"), 
	MEANBEAN("meanbean/");
  
	private String dir;
	private HashMap<String, Sound> sounds = new HashMap<>();
  
	private Soundpack(String dir) { this.dir = dir; }
  

	public String getDirectory() { return dir; }
  
	public Sound getSound(GameSounds sound) { return (Sound)sounds.get(sound.getPath()); }
  
	public void loadPack(LoadingListener listener) {
		for (GameSounds sound : GameSounds.values()) {
			if (Sound.isSound(dir + sound.getPath())) {
				sounds.put(sound.getPath(), Sound.getSound(dir + sound.getPath(), true));
			}
			if (listener != null) {
				listener.loaded(sound.getPath());
			}
		}
	}
}