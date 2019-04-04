package me.ipodtouch0218.multiplayerpuyo.manager;

import java.awt.Color;
import java.util.ArrayList;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.misc.FeverBoardSet;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjTextDisplay;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoFeverBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticleFallingPuyo;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticleFeverIcon;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class FeverManager extends GameObject {
	
	private static boolean loaded = false;
	private static String[] chainFiles = {"avalanche.pfc", "stairwich.pfc", "stairs.pfc", "key.pfc", "gtr.pfc"};
	private static final ArrayList<FeverBoardSet> feverBoards = new ArrayList<>();
	{
		if (!loaded) {
			loaded = true;
			for (String str : chainFiles) {
				feverBoards.add(new FeverBoardSet(str));
			}
		}
	}
	
	private ObjPuyoFeverBoard board;
	
	private int feverCharge = 0;
	private double timeRemaining = 15;
	
	private PuyoInfo[][] otherBoardLayout;
	private int chainLength = 5;
	private boolean inFever = false;
	
	public FeverManager(ObjPuyoFeverBoard board) {
		this.board = board;
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		if (board.isReadyForPuyo() && board.getBoardManager().hasStarted()) {
			if (feverCharge >= 7 && !inFever) {
				board.feverFlip();
			}
		}

		if (inFever && timeRemaining >= 0) {
			int prevSecond = (int) timeRemaining;
			timeRemaining -= delta;
			if (prevSecond > (int) timeRemaining && timeRemaining <= 7) {
				if (timeRemaining <= 1) {
					GameSounds.TIMER_STOP.play();
				} else {
					GameSounds.TIMER.play();
				}
			}
		}
	}
	
	public void startFever() {
		if (board.getDropper() != null) {
			board.getDropper().disable();
		}
		otherBoardLayout = board.getBoard().clone();
		for (int x = 0; x < board.getWidth(); x++) {
			otherBoardLayout[x] = board.getBoard()[x].clone();
		}
		board.clearBoard(false);
		inFever = true;
		
		GameEngine.addGameObject(new ParticleFeverIcon(board));
		
		board.getGarbageIndicator().setGrayscale(true);
		board.getGarbageIndicator().setY(board.getGarbageIndicator().getY()-12);
		
		CharacterSounds.FEVER_START.getSound(board.getCharacter(), board.isCharacterAlt()).play();
		GameSounds.FEVER_START.play();
		
		PuyoGameMain.getThreadPool().execute(() -> {
			ArrayList<ParticleFallingPuyo> falling = dropBoard(board, getRandomBoard(chainLength));
			while (!falling.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			board.setReadyForPuyo(true);
		});
	}
	
	public void reset() {
		chainLength = 5;
		timeRemaining = 15;
		feverCharge = 0;
		
		if (board.getFeverIndicator() != null) board.getFeverIndicator().setOverallGarbage(0);
		if (board.getGarbageIndicator() != null) {
			board.getGarbageIndicator().setGrayscale(false);
			if (inFever) {
				board.getGarbageIndicator().setY(board.getGarbageIndicator().getY()+12);
			}
		}
		inFever = false;
	}
	
	public void endFever() {
		if (timeRemaining <= -1) { return; }
		board.feverFlip();
	}
	
	public void endFeverFully() {
		inFever = false;
		timeRemaining = 15;
		feverCharge = 0;
		
		board.clearBoard(true);
		board.setBoard(otherBoardLayout);
		board.getGarbageIndicator().setOverallGarbage(board.getGarbageIndicator().getOverallGarbage() + board.getFeverIndicator().getOverallGarbage());
		board.getFeverIndicator().setOverallGarbage(0);
		board.getGarbageIndicator().setGrayscale(false);
		board.setReadyForPuyo(true);
		GameSounds.FEVER_FLIP.play();
		
		board.getBoardManager().getGamemode().onBoardFeverEnd(board);
	}
	
	public void setTimeRemaining(int i) { timeRemaining = i; }
	public void addTime(int time) { 
		if (timeRemaining <= -1) { return; }
		int max = (board.getBoardManager().getPlayers() == 1 ? 60 : 30);
		timeRemaining = Math.min(max, timeRemaining+time); 
		GameEngine.addGameObject(new ObjTextDisplay("+" + (double) time, 0, -0.5, 2*60, 14, Color.WHITE), (int) board.getX()+(board.getWidth()*16)+5, (int) board.getY()+(board.getHeight()*16)-60-20);
	}
	public void addCharge() { feverCharge = Math.min(7, feverCharge + 1); }
	public void addChainLength(int amount) { chainLength = Math.max(3, Math.min(14, chainLength+amount)); }
	public void setCharge(int i) { feverCharge = i; }
	public void setInFever(boolean b) { inFever = b; }
	
	public int getTimeRemaining() { return (int) timeRemaining; }
	public int getChainLength() { return chainLength; }
	public boolean isInFever() { return inFever; }
	public int getCharge() { return feverCharge; }
	
	
	
	public static PuyoInfo[][] getRandomBoard(int chainlength) {
		return feverBoards.get((int) (Math.random()*feverBoards.size())).getBoardOfChainLength(chainlength);
	}
	
	public static ArrayList<ParticleFallingPuyo> dropBoard(ObjPuyoBoard board, PuyoInfo[][] puyoInfos) {		
		ArrayList<ParticleFallingPuyo> puyos = new ArrayList<>();
		for (int x = 0; x < puyoInfos.length; x++) {
			for (int y = puyoInfos[0].length-1; y >= 0; y--) {
				PuyoInfo type = puyoInfos[x][y];
				if (type.type == null) { continue; }
				
				LandingPuyoManager.turnIntoFallingPuyo(board, type, x, y+2, (y-11d)*1.25d, puyos, 2);
			}
		}
		return puyos;
	}
}
