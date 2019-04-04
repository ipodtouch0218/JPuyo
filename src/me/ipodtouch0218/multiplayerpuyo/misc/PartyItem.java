package me.ipodtouch0218.multiplayerpuyo.misc;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.util.RandomUtils;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPartyBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticlePuyoCleared;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public enum PartyItem {

	NO_ROTATION(false, (ObjPartyBoard collector)->{
		//disable rotation
		for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
			if (others == collector) { continue; }
			ObjPartyBoard b = (ObjPartyBoard) others;
			b.setNoRotation(true);
		}
		//enable rotation
		GameEngine.scheduleSyncTask(()->{
			for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
				if (others == collector) { continue; }
				ObjPartyBoard b = (ObjPartyBoard) others;
				b.setNoRotation(false);
			}
		}, 5);
		
	}, GameSounds.PARTY_ITEM_GENERIC, 0, 0), 
	DROP_SPEED(false, (ObjPartyBoard collector)->{
		for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
			if (others == collector) { continue; }
			ObjPartyBoard b = (ObjPartyBoard) others;
			b.setCustomDropSpeed(1);
		}
		//enable rotation
		GameEngine.scheduleSyncTask(()->{
			for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
				if (others == collector) { continue; }
				ObjPartyBoard b = (ObjPartyBoard) others;
				b.setCustomDropSpeed(-1);
			}
		}, 7.5);
		
	},GameSounds.PARTY_ITEM_GENERIC, 1,0), 
	SEARCHLIGHT(false,(ObjPartyBoard collector)->{
		for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
			if (others == collector) { continue; }
			ObjPartyBoard b = (ObjPartyBoard) others;
			b.setSearchlight(true);
		}
		//enable rotation
		GameEngine.scheduleSyncTask(()->{
			for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
				if (others == collector) { continue; }
				ObjPartyBoard b = (ObjPartyBoard) others;
				b.setSearchlight(false);
			}
		}, 7.5);
	},GameSounds.PARTY_ITEM_GENERIC,2,0),
	ATTACK_UP(true,(ObjPartyBoard collector)->{
		collector.setAttackPower(1.5);
		
		GameEngine.scheduleSyncTask(()->{
			collector.setAttackPower(1);
		},10);
	},GameSounds.PARTY_ITEM_ATTACK,0,1), 
	GARBAGE_CLEANUP(true,(ObjPartyBoard collector)->{
		for (int x = 0; x < collector.getWidth(); x++) {
			for (int y = 0; y < collector.getHeight(); y++) {
				if (collector.getPuyoAt(x,y).type == PuyoType.GARBAGE && collector.getPuyoAt(x,y).partyItem == null) {
					GameEngine.addGameObject(new ParticlePuyoCleared(x, y-2, collector, PuyoType.GARBAGE));
					collector.resetPuyoAt(x,y);
				}
			}
		}
		collector.getGarbageIndicator().setOverallGarbage(0);
		collector.setDroppableGarbage(0);
	},GameSounds.PARTY_ITEM_GARBAGE_CLEAR,1,1), 
	GARBAGE_DEFLECTOR(true,(ObjPartyBoard collector)->{
		collector.setGarbageDeflector(true);
		
		GameEngine.scheduleSyncTask(()->{
			collector.setGarbageDeflector(false);
		},10);
	},GameSounds.PARTY_ITEM_SHIELD,2,1),
	SINGLE_COLOR(true,(ObjPartyBoard collector)->{
		collector.addSingleColor(5);
	},GameSounds.PARTY_ITEM_GENERIC,0,2), 
	SCORE_VACCUUM(true,(ObjPartyBoard collector)->{
		int count = collector.getBoardManager().getBoards().size()-1;
		for (ObjPuyoBoard board : collector.getBoardManager().getBoards()) {
			if (board == collector) { continue; }
			collector.addScore(Math.min(1500d/count, board.getScore()));
			board.addScore(-1500d/count);
			//TODO: popup -XXX thingy
		}
	},GameSounds.PARTY_ITEM_GENERIC,1,2), 
	ICE_PUYOS(false,(ObjPartyBoard collector)->{
		for (ObjPuyoBoard others : collector.getBoardManager().getBoards()) {
			if (others == collector) { continue; }
			ObjPartyBoard b = (ObjPartyBoard) others;
			b.setFreeze(true);
		}
	},GameSounds.PARTY_ITEM_GENERIC,2,2);
	
	private GameSprite sprite;
	private PartyItemRunnable runn;
	private boolean selfitem;
	private GameSounds sound;
	PartyItem(boolean selfitem, PartyItemRunnable runner, GameSounds sound, int imgX, int imgY) {
		this.selfitem = selfitem;
		this.runn = runner;
		this.sound = sound;
		this.sprite = new SpriteSheet("puyo/party_items.png", 16, 16).getSprite(imgX, imgY); //should load cached
	}
	
	public boolean isSelfItem() {
		return selfitem;
	}
	public GameSprite getSprite() {
		return sprite; 
	}
	public GameSounds getSound() { 
		return sound;
	}
	public void onCollect(ObjPuyoBoard board) {
		if (!(board instanceof ObjPartyBoard)) { return; }
		runn.onCollect(((ObjPartyBoard) board));
	}
	
	
	///---static---///
	public static PartyItem getRandomItem(boolean first, PartyItemSet set) {
		PartyItem pickedItem = null;
		if (first) {
			while (pickedItem == null || pickedItem == PartyItem.SCORE_VACCUUM) {
				pickedItem = set.getRandomItem();
			}
		} else {
			pickedItem = set.getRandomItem();
		}
		return pickedItem;
	}
	
	public static interface PartyItemRunnable {
		public void onCollect(ObjPartyBoard collector);
	}
	
	public static enum PartyItemSet {
		HELPS_SELF(RandomUtils.addArrays(PartyItem.values(), new PartyItem[]{PartyItem.GARBAGE_CLEANUP, PartyItem.GARBAGE_DEFLECTOR, PartyItem.SINGLE_COLOR})),
		THROWS_GARBAGE(RandomUtils.addArrays(PartyItem.values(), new PartyItem[]{PartyItem.ATTACK_UP, PartyItem.ICE_PUYOS, PartyItem.DROP_SPEED})),
		SPECIAL_TYPE(RandomUtils.addArrays(PartyItem.values(), new PartyItem[]{PartyItem.SEARCHLIGHT, PartyItem.NO_ROTATION})),
		BALANCED(PartyItem.values());
		
		private PartyItem[] set;
		PartyItemSet(PartyItem[] items) {
			set = items;
		}
		public PartyItem[] getItemSet() {
			return set;
		}
		public PartyItem getRandomItem() {
			return set[(int) (Math.random()*set.length)];
		}
	}
}
