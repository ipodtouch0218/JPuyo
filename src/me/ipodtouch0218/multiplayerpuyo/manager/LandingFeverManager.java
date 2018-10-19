package me.ipodtouch0218.multiplayerpuyo.manager;

import java.util.ArrayList;

import me.ipodtouch0218.java2dengine.sound.Sound;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard.FeverBackground;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoFeverBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjTextDisplay;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticleFallingPuyo;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.senders.ParticleGarbageCancelSender;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class LandingFeverManager extends LandingPuyoManager {
	
	private static final CharacterSounds[] soundOrder7 = {CharacterSounds.CHAIN_1,CharacterSounds.CHAIN_2,CharacterSounds.CHAIN_3,CharacterSounds.CHAIN_4,CharacterSounds.CHAIN_5};
	private static final CharacterSounds[] soundOrder11 = {CharacterSounds.CHAIN_1,CharacterSounds.CHAIN_2,CharacterSounds.CHAIN_3,CharacterSounds.CHAIN_4,CharacterSounds.SPELL_2,
			CharacterSounds.CHAIN_3,CharacterSounds.CHAIN_4,CharacterSounds.CHAIN_5};
	private static final CharacterSounds[] soundOrder15 = {CharacterSounds.CHAIN_1,CharacterSounds.CHAIN_2,CharacterSounds.CHAIN_3,CharacterSounds.CHAIN_5,CharacterSounds.SPELL_2,
			CharacterSounds.CHAIN_2,CharacterSounds.CHAIN_3,CharacterSounds.CHAIN_4,CharacterSounds.CHAIN_5,CharacterSounds.SPELL_3,CharacterSounds.CHAIN_4,CharacterSounds.CHAIN_5};
	
	
	private ObjGarbageIndicator indicator;
	private FeverManager manager;
	
	public LandingFeverManager(FeverManager manager, ObjPuyoFeverBoard board, ObjGarbageIndicator garbage) {
		super(board);
		indicator = garbage;
		this.manager = manager;
	}

	
	private int usedGarbage;
	
	public void run() {
		ObjPuyoFeverBoard fBoard = (ObjPuyoFeverBoard) board;
		squishDropperPositions();
		
		oldScore.put(board, board.getScore());
		animateDrop(2);
		board.setDropper(null);
		
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		boolean matched = false;
		int chain = 0;
		boolean longdelay = false;
		while (board.checkForMatches(this)) {
			matched = true;
			fBoard.setFeverInChain(true);
			chain++;
			
			sleep(75);
			
			
			PuyoType type = PuyoType.values()[(int) (Math.random()*PuyoType.values().length)];
			double startX = board.getX()+((board.getWidth()*16)/2);
			double startY = board.getY()+((board.getHeight()*16)/2);
			if (matches != null) {
				double finalAveX = 0, finalAveY = 0;
				double averX = 0, averY = 0;
				for (Integer[][] popped : matches) {
					averX = 0;
					averY = 0;
					for (Integer[] poppedMore : popped) {
						averX += poppedMore[0];
						averY += poppedMore[1];
					}
					averX /= (double) popped.length;
					averY /= (double) popped.length;
					
					finalAveX += averX;
					finalAveY += averY;
				}
				startX = (finalAveX / matches.size())*16 + board.getX();
				startY = (finalAveY / matches.size())*16 + board.getY();
			}
			
			PuyoGameMain.getGameEngine().addGameObject(new ObjTextDisplay(consecutive + " chain!", 0, -0.2, 60, 12, textCol), startX, startY);
			
			int garbage = calculateGarbage(scoreToSend) - usedGarbage;
			if (fBoard.getFeverIndicator().getOverallGarbage() > 0) {
				ObjGarbageIndicator indi = fBoard.getFeverIndicator();
				PuyoGameMain.getGameEngine().addGameObject(new ParticleGarbageCancelSender(board, indi, garbage, consecutive, type), startX, startY);
			} else if (board.getGarbageIndicator().getOverallGarbage() > 0) {
				ObjGarbageIndicator indi = board.getGarbageIndicator();
				PuyoGameMain.getGameEngine().addGameObject(new ParticleGarbageCancelSender(board, indi, garbage, consecutive, type), startX, startY);
			} else {
				board.getBoardManager().sendGarbage(board, garbage, true, type, consecutive);
			}
			
			if (Math.random() < 0.7) {
				((ObjPuyoFeverBoard) board).setFeverBackgroundColor(FeverBackground.values()[(int) (Math.random()*FeverBackground.values().length)]);
			}
			usedGarbage = calculateGarbage(scoreToSend);
			
			CharacterSounds[] array = null;
			if (manager.getChainLength() < 7) {
				array = soundOrder7;
			} else if (manager.getChainLength() < 11) {
				array = soundOrder11;
			} else {
				array = soundOrder15;
			}
			
			CharacterSounds nextSound = array[Math.min(consecutive-1,array.length-1)];
			if (consecutive >= manager.getChainLength()) {
				if (consecutive < 6) {
					nextSound = CharacterSounds.SPELL_2;
				} else if (consecutive < 10) {
					nextSound = CharacterSounds.SPELL_3;
				} else if (consecutive < 13) {
					nextSound = CharacterSounds.SPELL_4;
				} else {
					nextSound = CharacterSounds.SPELL_5;
				}
				CharacterSounds finalNextSound = nextSound;
				longdelay = !(finalNextSound == CharacterSounds.SPELL_2 || finalNextSound == CharacterSounds.SPELL_3);
				new Thread() {
					public void run() {
						Sound sound = finalNextSound.getSound(board.getCharacter());
						int times = (finalNextSound == CharacterSounds.SPELL_2 || finalNextSound == CharacterSounds.SPELL_3 ? 1 : 4);
						for (;times>0;times--) {
							if (sound.isPlaying()) {
								sound.getClip().setFramePosition(0);
							} else {
								sound.play();
							}
							try {
								sleep(125);
							} catch (InterruptedException e) {}
						}
					}
				}.start();
			} else {
				nextSound.getSound(board.getCharacter()).play();
			}
			
			while (PuyoBoardManager.isPaused()) { sleep(1); }
			animateDrop(2);
		}	
		
		sleep(75);
		
		boolean allclear = true;
		xLoop:
		for (int x = 0; x < board.getWidth(); x++) {
			for (int y = 0; y < board.getHeight(); y++) {
				if (board.getPuyoAt(x, y) != null) { allclear = false; break xLoop; }
			}
		}
		
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		animateDrop(2);
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		
		sleep(75);
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		
		//----drop new board-----//
		if (matched || (manager.getTimeRemaining() <= 0 && manager.getTimeRemaining() > -1)) {
			board.clearBoard(true);
		}
		if (matched && (manager.getTimeRemaining() > 0 || manager.getTimeRemaining() <= -1)) {
			
			boolean longdelayfinal = longdelay;
			if (consecutive+1 >= manager.getChainLength()) {
				new Thread() {
					public void run() {
						try {
							sleep((longdelayfinal ? 1000 : 500));
						} catch (InterruptedException e) {}
						CharacterSounds.FEVER_SUCCESS.getSound(board.getCharacter()).play();
					}
				}.start();
			} else {
				CharacterSounds.FEVER_FAIL.getSound(board.getCharacter()).play();
			}
			
			manager.addChainLength(Math.max(-2,(chain+1-manager.getChainLength())));
			if (board.getBoardManager().getPlayersRemaining() == 1) {
				manager.addTime(chain-2);
			} else {
				manager.addTime(chain/3);
			}

			if (allclear) {
				GameSounds.ALL_CLEAR.play();
				manager.addChainLength(1);
				if (board.getBoardManager().getPlayersRemaining() == 1) {
					manager.addTime(3);
				} else {
					manager.addTime(5);
				}
			}
			

			ArrayList<ParticleFallingPuyo> falling = FeverManager.dropBoard(board, FeverManager.getRandomBoard(manager.getChainLength()));
			while (!falling.isEmpty()) {
				sleep(1);
			}
			((ObjPuyoFeverBoard) board).setFeverInChain(false);
			((ObjPuyoFeverBoard) board).setFeverBackgroundColor(FeverBackground.values()[(int) (Math.random()*FeverBackground.values().length)]);
		}
		
		if (manager.getTimeRemaining() <= 0 && manager.getTimeRemaining() > -1) {
			manager.endFever();
		}
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		
		
		if (!(manager.getTimeRemaining() <= 0 && manager.getTimeRemaining() > -1)) {
			if (scoreToSend <= 10) {
				dropGarbage(indicator);
			}
		}
		
		sleep(200);
		
		board.getBoardManager().verifyGarbage(board);
		board.setReadyForPuyo(true);
	}
}
