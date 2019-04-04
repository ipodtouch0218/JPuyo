package me.ipodtouch0218.multiplayerpuyo.misc;

import java.util.HashMap;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.sound.Sound;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.misc.PartyItem.PartyItemSet;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;

public enum PuyoCharacter {

	SIG("sig", PartyItemSet.BALANCED, new PoseDirection[]{PoseDirection.BOTTOM,PoseDirection.TOP,PoseDirection.TOP,PoseDirection.BOTTOM,PoseDirection.BOTTOM_RIGHT,PoseDirection.BOTTOM,PoseDirection.TOP}),
	AMITIE("amitie", PartyItemSet.HELPS_SELF, new PoseDirection[]{PoseDirection.TOP_LEFT, PoseDirection.BOTTOM_RIGHT, PoseDirection.TOP, PoseDirection.BOTTOM, PoseDirection.BOTTOM_RIGHT, PoseDirection.TOP, PoseDirection.TOP}),
	ECOLO("ecolo", PartyItemSet.SPECIAL_TYPE, null),
	DRACO_CENTAUROUS("draco", PartyItemSet.BALANCED, new PoseDirection[]{PoseDirection.BOTTOM_LEFT,PoseDirection.BOTTOM,PoseDirection.TOP_LEFT,PoseDirection.TOP,PoseDirection.BOTTOM}),
	FELI("feli", PartyItemSet.THROWS_GARBAGE, null),
	LEMRES("lemres", PartyItemSet.BALANCED, null),
	RAFFINA("raffina", PartyItemSet.HELPS_SELF, null),
	ARLE("arle", PartyItemSet.BALANCED, null);
	
	private SpriteSheet poseSheet = null;
	
	private String resString;
	
	private PartyItemSet itemSet;
	private PuyoType[][] icepuyos;
	private PoseDirection[] poseDirections;
	
	private GameSprite background;
	private GameSprite selectImg;
	private HashMap<CharacterSounds, Sound> normalSounds = new HashMap<>();
	private HashMap<CharacterSounds, Sound> altSounds = new HashMap<>();
	private HashMap<CharacterPose, PoseInfo> poses = new HashMap<>();
	
	PuyoCharacter(String res, PartyItemSet itemSet, PoseDirection[] dirs) {
		resString = res;
		loadIcePuyos();
		poseDirections = dirs;
		this.itemSet = itemSet;
	}
	
	//---sprites---//
	public GameSprite getBackground() {
		if (background == null) {
			background = new GameSprite("character/" + resString + "/background.png", false);
		}
		return background;
	}
	public GameSprite getSelectImage() {
		if (selectImg == null) {
			selectImg = new GameSprite("character/" + resString + "/select.png", true);
		}
		return selectImg;
	}
	public void loadPoses() {
		if (poseDirections == null) { return; }
		poseSheet = new SpriteSheet("character/" + resString + "/poses.png", 96, 192);
		int count = 0;
		for (CharacterPose pose : CharacterPose.values()) {
			PoseDirection dir = ((poseDirections == null || count >= poseDirections.length) ? PoseDirection.BOTTOM : poseDirections[count]);
			PoseInfo inf = new PoseInfo(dir, pose);
			inf.sprite = poseSheet.getSprite(count, 0, false);
			poses.put(pose, inf);
			count++;
		}
		
		poseSheet.close();
	}
	public PoseInfo getPose(CharacterPose pose) {
		if (!poses.containsKey(pose)) {
			loadPoses();
		}
		return poses.get(pose);
	}
	
	//---sound---//
	public Sound getSound(CharacterSounds sound, boolean alternate) {
		
		if (alternate) {
			if (altSounds.isEmpty()) { loadSounds(true); }
			return altSounds.get(sound);
		}
		
		if (normalSounds.isEmpty()) { loadSounds(false); }
		return normalSounds.get(sound);
	}
	public void loadSounds(boolean alternate) {
		for (CharacterSounds sounds : CharacterSounds.values()) {
			if (alternate) {
				altSounds.put(sounds, sounds.getSound(this, true));
			} else {
				normalSounds.put(sounds, sounds.getSound(this, false));
			}
		}
	}
	public void unloadSounds() {
		
		for (CharacterSounds sounds : CharacterSounds.values()) {
			Sound.unloadSound("character/" + resString + "/normal/" + sounds.getLocation());
			Sound.unloadSound("character/" + resString + "/alt/" + sounds.getLocation());
		}
		normalSounds.clear();
		altSounds.clear();
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
	public PartyItemSet getPartyItemSet() { return itemSet; }
	
	
	//---static---//
	public static enum CharacterPose {
		CHAIN_1(false),CHAIN_2(false),CHAIN_3(false),CHAIN_4(false),CHAIN_5(false),COUNTER(false),GARBAGE(false),WIN(true),LOSE(true);
		
		public boolean persist;
		CharacterPose(boolean persist) {
			this.persist = persist;
		}
	}
	public static enum PoseDirection {
		TOP_LEFT(-96, -192), TOP(0, -192), TOP_RIGHT(96, -192),
		BOTTOM_LEFT(-96, 360), BOTTOM(0, 360), BOTTOM_RIGHT(96, 360);
		
		
		public int startX, startY;
		PoseDirection(int startX, int startY) {
			this.startX = startX;
			this.startY = startY;
		}
	}
	public static class PoseInfo {
		public GameSprite sprite;
		public PoseDirection dir;
		
		public PoseInfo(PoseDirection dir, CharacterPose pose) {
			this.dir = dir;
		}
	}

}
