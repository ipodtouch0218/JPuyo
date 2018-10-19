package me.ipodtouch0218.multiplayerpuyo.misc;

import java.util.HashMap;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.sound.Sound;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;

public enum PuyoCharacter {

	SIG("sig"),
	AMITIE("amitie"),
	ECOLO("ecolo"),
	DRACO_CENTAUROUS("draco"),
	FELI("feli"),
	LEMRES("lemres"),
	RAFFINA("raffina"),
	ARLE("arle");
	
	private SpriteSheet poseSheet = null;
	
	private String resString;
	
	private PuyoType[][] icepuyos;
	
	private GameSprite background;
	private GameSprite selectImg;
	private HashMap<CharacterSounds, Sound> charSounds = new HashMap<>();
	private HashMap<CharacterPose, GameSprite> poses = new HashMap<>();
	private boolean soundsLoaded = false;
	
	PuyoCharacter(String res) {
		resString = res;
		poseSheet = new SpriteSheet("character/" + resString + "/poses.png", 64, 96);
		loadIcePuyos();
	}
	
	//---sprites---//
	public GameSprite getBackground() {
		if (background == null) {
			background = new GameSprite("character/" + resString + "/background.png");
		}
		return background;
	}
	public GameSprite getSelectImage() {
		if (selectImg == null) {
			selectImg = new GameSprite("character/" + resString + "/select.png");
		}
		return selectImg;
	}
	public GameSprite getPose(CharacterPose pose) {
		if (!poses.containsKey(pose)) {
			poses.put(pose, poseSheet.getSprite(pose.x, pose.y));
		}
		return poses.get(pose);
	}
	
	//---sound---//
	public Sound getSound(CharacterSounds sound) {
		if (!soundsLoaded) {
			loadSounds();
		}
		
		return charSounds.get(sound);
	}
	public void loadSounds() {
		if (soundsLoaded) { return; }
		
		for (CharacterSounds sounds : CharacterSounds.values()) {
			charSounds.put(sounds, sounds.getSound(this));
		}
		
		soundsLoaded = true;
	}
	public void unloadSounds() {
		if (!soundsLoaded) { return; }
		
		for (CharacterSounds sounds : CharacterSounds.values()) {
			Sound.unloadSound("character/" + resString + "/" + sounds.getLocation());
		}
		charSounds.clear();
		
	}
	public String getResStr() {
		return resString;
	}
	
	//---other---//
	public PuyoType[][] getIcePuyos() {
		return icepuyos;
	}
	private void loadIcePuyos() {
		icepuyos = new PuyoType[6][5];
		
		String contents = FeverBoardSet.getFileAsString("/res/data/character/" + resString + "/ice.inp");
		
		int y = 0;
		for (String row :  contents.split("\n")) {
			int x = 0;
			for (int symbol : row.chars().toArray()) {
				icepuyos[x][y] = PuyoType.getPuyoTypeFromSymbol((char) symbol);
				x++;
			}
			y++;
		}
	}
	
	
	//---static---//
	public static enum CharacterPose {
		WIN(0,0), LOSE(1,0), CHAIN(2,0);
		
		public int x,y;
		CharacterPose(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
