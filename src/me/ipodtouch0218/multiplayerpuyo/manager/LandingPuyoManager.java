package me.ipodtouch0218.multiplayerpuyo.manager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.misc.Gamemodes;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter.CharacterPose;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjDropper;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoFeverBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjTextDisplay;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticleFallingPuyo;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticleIceChip;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticlePuyoPop;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.senders.ParticleFeverSender;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.senders.ParticleGarbageCancelSender;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class LandingPuyoManager implements Runnable {
	
	protected static HashMap<ObjPuyoBoard, Integer> oldScore = new HashMap<>();
	
	private static final CharacterSounds[] soundOrder = {CharacterSounds.CHAIN_1,CharacterSounds.CHAIN_2,CharacterSounds.SPELL_1,CharacterSounds.SPELL_2,CharacterSounds.SPELL_3,CharacterSounds.SPELL_4,CharacterSounds.SPELL_5};
	private static final double[][] METER_OFFSETS = {{3.5,40.5},{3.5,33.5},{4.5,28.5},{7.5,24.5},{10,19},{9,13},{5,5}};
	protected static final Color textCol = new Color(252, 210, 25);
	
	public ArrayList<Integer[][]> matches;
	protected ObjPuyoBoard board;
	protected int scoreToSend = 0;
	protected int consecutive = 0;
	
	private int usedGarbage = 0;
	
	public LandingPuyoManager(ObjPuyoBoard board) {
		this.board = board;
	}
	
	public void run() {
		
		ParticleFeverSender sender = null;
		
		squishDropperPositions();
		
		oldScore.put(board, board.getScore());
		animateDrop(0.2);
		
		board.setDropper(null);
		
		
		if (board.getBoardManager().getGamemode() == Gamemodes.ICE_PUYO) {
			handleIce();
		}
		
		
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		while (board.checkForMatches(this)) {
			sleep(200);
			if (board.hasAllClear()) {
				board.setAllClear(false);
				scoreToSend += 30*70;
			}
			
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
				startY = ((finalAveY / matches.size())-2)*16 + board.getY();
			}
			
			PuyoGameMain.getGameEngine().addGameObject(new ObjTextDisplay(consecutive + " chain!", 0, -0.2, 60, 12, textCol), startX, startY);
			
			boolean counter = board.getGarbageIndicator().getOverallGarbage() > 0;
			
			int calculatedGarbage = calculateGarbage(scoreToSend);
			int garbage = calculatedGarbage - usedGarbage;
			if (board.getGarbageIndicator().getOverallGarbage() > 0) {

				ObjGarbageIndicator indi = board.getGarbageIndicator();
				PuyoGameMain.getGameEngine().addGameObject(new ParticleGarbageCancelSender(board, indi, garbage, consecutive, type), startX, startY);

				if (board.getBoardManager().getGamemode().isFever()) {
					ObjPuyoFeverBoard fboard = (ObjPuyoFeverBoard) board;
					double targetX = board.getX()+board.getWidth()*16+8+(METER_OFFSETS[Math.min(6, fboard.getFeverManager().getCharge())][0]*2);
					double targetY = board.getY()+board.getHeight()*16-100+(METER_OFFSETS[Math.min(6, fboard.getFeverManager().getCharge())][1]*2);
					sender = PuyoGameMain.getGameEngine().addGameObject(new ParticleFeverSender(fboard.getFeverManager(), (int) targetX, (int) targetY), board.getX()+8, board.getY()-16);

					for (ObjPuyoBoard boards : board.getBoardManager().getBoards()) {
						if (boards == board) { continue; }
						((ObjPuyoFeverBoard) boards).getFeverManager().addTime(1);
					}
				}
			} else {
				if (board.getBoardManager().getGamemode().isFever()) {
					((ObjPuyoFeverBoard) board).getFeverManager().addTime(1);
				}
				board.getBoardManager().sendGarbage(board, garbage, true, type, consecutive);
			}
			usedGarbage = calculatedGarbage;
			
			//character sound handling
			CharacterSounds nextSound = soundOrder[Math.min(consecutive-1,soundOrder.length-1)];
			if (counter && board.getGarbageIndicator().getOverallGarbage() <= 0) {
				nextSound = CharacterSounds.COUNTER;
			}
			nextSound.getSound(board.getCharacter()).play();
			
			while (PuyoBoardManager.isPaused()) { sleep(1); } 
			animateDrop(0.2);
		}
		if (consecutive > 3) {
			board.poseCharacter(CharacterPose.CHAIN);
		}
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		
		
		if (board instanceof ObjPuyoFeverBoard) {
			while (sender != null && !sender.done() && ((ObjPuyoFeverBoard) board).getFeverManager().getCharge() + 1 >= 7) {
				sleep(1);
			}
		}
		board.getBoardManager().verifyGarbage(board);
		
		//if (!board.getBoardManager().dontDrop()) {
			if (!board.getBoardManager().isOffsetEnabled() ||  board.getGarbageIndicator().getOverallGarbage() == 0) { 
				dropGarbage(board.getGarbageIndicator()); 
			} else {
				if (scoreToSend <= 10) {
					dropGarbage(board.getGarbageIndicator());
				}
			}
		//}
		
		
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		animateDrop(0.2);
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		
		boolean allclear = true;
		xLoop:
		for (int x = 0; x < board.getWidth(); x++) {
			for (int y = 0; y < board.getHeight(); y++) {
				if (board.getPuyoAt(x, y) != null) { allclear = false; break xLoop; }
			}
		}
		if (allclear) {
			if (board instanceof ObjPuyoFeverBoard) {
				ArrayList<ParticleFallingPuyo> b = FeverManager.dropBoard(board, FeverManager.getRandomBoard(5));
				while(!b.isEmpty()) {
					sleep(50);
				}
			} else {
				board.setAllClear(true);
			}
			GameSounds.ALL_CLEAR.play();
		}
		
		
		while (PuyoBoardManager.isPaused()) { sleep(1); }
		
		sleep(200);
		board.setReadyForPuyo(true);
	}
	
	private void handleIce() {
		boolean broken = false;
		boolean chipped = false;
		for (int x = 0; x < board.getWidth(); x++) {
			for (int y = 0; y < board.getHeight(); y++) {
				PuyoInfo info = board.getPuyoAt(x, y);
				if (info.ice && info.icetimer >= 0) {
					if (info.icetimer == 0) {
						info.ice = false;
						broken = true;
						
						if (PuyoGameMain.quality == RenderQuality.HIGH) {
							for (int i = 0; i < 2; i++) {
								PuyoGameMain.getGameEngine().addGameObject(new ParticleIceChip(Math.random()*3d-1.5), board.getX()+x*16, board.getY()+(y-2)*16);
							}
						}
					}
					
					info.icetimer--;
					chipped = true;
				}
			}
		}
		
		if (broken) {
			GameSounds.ICE_BREAK.play();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		} else if (chipped) {
			GameSounds.ICE_CHIP.play();
		}
		
	}
	
	protected void squishDropperPositions() {
		ObjDropper dropper = board.getDropper();
		if (dropper == null) { return; }
		int[][] dropLocs = dropper.dropLocations();
		ArrayList<Integer> squished = new ArrayList<Integer>();
		for (int[] loc : dropLocs) {
			int x = loc[0];
			if (squished.contains(x)) continue;
			squished.add(x);
			board.squishRow(x, 0.15, 0.1);
		}
	}
	
	protected int getTopPuyoLocAtIncludingOccupied(int x, ArrayList<Integer> occupiedSpaces) {
		PuyoInfo[][] b = board.getBoard();
		for (int i = board.getHeight()-1; i >= 0; i--) {
			if (b[x][i].type == null && !occupiedSpaces.contains(i)) {
				return i;
			}
		}
		return board.getHeight()-1;
	}
	
	ArrayList<ParticleFallingPuyo> falling = new ArrayList<>();
	protected void animateDrop(double yVel) {
		
		for (int x = board.getWidth()-1; x >= 0; x--) {
			HashMap<Integer, PuyoType> blank = new HashMap<>();
			ArrayList<Integer> occupiedSpaces = new ArrayList<>();
			yLoop:
			for (int y = board.getHeight()-1; y >= 0; y--) {
				PuyoInfo type = board.getPuyoAt(x, y);
				
				if (type.type == null) { continue yLoop; } 
				if (!board.isInBounds(x,y+1) || board.getPuyoAt(x,y+1).type != null) { continue yLoop; }
				
				blank.put(y, type.type);
				int pos = getTopPuyoLocAtIncludingOccupied(x, occupiedSpaces);
				
				occupiedSpaces.add(pos);
				int fx = x;
				int fy = y;
				turnIntoFallingPuyo(board, type.type, x, pos, y, falling, yVel, new Runnable() {
					public void run() {
						board.setPuyoAt(fx, fy, null);
					}
				}, type.ice, type.icetimer);
				type.type = null;
				type.ice = false;
				type.icetimer = -1;
			}
			for (Entry<Integer, PuyoType> en : blank.entrySet()) {
				board.setPuyoAt(x, en.getKey(), en.getValue());
			}
		}
		while (!falling.isEmpty()) { sleep(1); }
	}
	
	public static void turnIntoFallingPuyo(ObjPuyoBoard board, PuyoType type, int targetX, int targetY, double startY, ArrayList<ParticleFallingPuyo> otherPuyos, double yVel, Runnable torun, boolean ice, int icehealth) {
		ParticleFallingPuyo newPuyo = new ParticleFallingPuyo(board, type, targetX, targetY, otherPuyos, yVel, torun, ice, icehealth);
		GameEngine.getInstance().addGameObject(newPuyo, (targetX*16)+board.getX(), (startY*16)+board.getY());
	}
	
	protected void dropGarbage(ObjGarbageIndicator in) {
		ArrayList<ParticleFallingPuyo> garbage = new ArrayList<>();
		if (board.getDroppableGarbage() > in.getOverallGarbage()) {
			board.setDroppableGarbage(in.getOverallGarbage());
		}
		if (in.getOverallGarbage() == 0) { return; }
		int remainingToDrop = Math.min(30, board.getDroppableGarbage());
		int garbageDropped = remainingToDrop;
		in.setOverallGarbage(in.getOverallGarbage()-remainingToDrop);
		board.addDroppableGarbage(-garbageDropped);
		if (remainingToDrop == 0) { return; }
		
		int heightCounter = 0;
		ArrayList<ArrayList<Integer>> occupiedSpaces = new ArrayList<>();
		while (remainingToDrop > 0) { //30 limit per drop.
			if (remainingToDrop >= board.getWidth()) {
				for (int x = 0; x < board.getWidth(); x++) {
					while (occupiedSpaces.size() <= x) { occupiedSpaces.add(new ArrayList<Integer>()); }
					if (board.getPuyoAt(x, 0).type != null || occupiedSpaces.get(x).contains(0)) { continue; }
					int pos = getTopPuyoLocAtIncludingOccupied(x, occupiedSpaces.get(x));
					
					boolean ice = false;
					PuyoType type = PuyoType.GARBAGE;
					if (board.getBoardManager().getGamemode() == Gamemodes.ICE_PUYO) {
						type = board.getCharacter().getIcePuyos()[x][heightCounter];
						ice = true;
					}
					
					turnIntoFallingPuyo(board, type, x, pos, -heightCounter, garbage, 0.2, null, ice, 2);
					
					occupiedSpaces.get(x).add(pos);
				}
				remainingToDrop-=board.getWidth();
				heightCounter++;
			} else {
				ArrayList<Integer> takenXs = new ArrayList<>();
				for (int i = 0; i < remainingToDrop; i++) {
					int x = (int) Math.floor(Math.random()*6d);
					while (takenXs.contains(x)) {
						x = (int) Math.floor(Math.random()*6d);
					}
					takenXs.add(x);
					
					while (occupiedSpaces.size() <= x) { occupiedSpaces.add(new ArrayList<Integer>()); }
					if (board.getPuyoAt(x, 0).type != null || occupiedSpaces.get(x).contains(0)) { continue; }
					int pos = getTopPuyoLocAtIncludingOccupied(x, occupiedSpaces.get(x));
					
					boolean ice = false;
					PuyoType type = PuyoType.GARBAGE;
					if (board.getBoardManager().getGamemode() == Gamemodes.ICE_PUYO) {
						type = board.getCharacter().getIcePuyos()[x][heightCounter];
						ice = true;
					}
					
					turnIntoFallingPuyo(board, type, x, pos, -heightCounter, garbage, 0.2, null, ice, 2);
					
					occupiedSpaces.get(x).add(pos);
				}
				remainingToDrop = 0;
			}
		}
		if (garbageDropped > 6*3) {
			CharacterSounds.GARBAGE_BIG.getSound(board.getCharacter()).play();
		} else if (garbageDropped > 6*1) {
			CharacterSounds.GARBAGE_SMALL.getSound(board.getCharacter()).play();
		}
		if (board.getControls() instanceof JoyconControls) {
			((JoyconControls) board.getControls()).getJoycon().vibrate(false,(byte)2,true,(byte)garbageDropped);
		}

		while (!garbage.isEmpty()) {
			sleep(1);
		}
		if (garbageDropped > 6*2) {
			GameSounds.GARBAGE_DROP_1.play();
		} else {
			GameSounds.GARBAGE_DROP_0.play();
		}
	}
	
	protected int popTimer = 10;
	public void popPuyos(List<Integer[]> toPop, boolean fever) {
		consecutive++;
		popTimer = 10;
		while (popTimer > 0) {
			while (PuyoBoardManager.isPaused()) { sleep(1); }
			popTimer--;
			if (popTimer % 2 == 0) {
				board.applyhide(true);
				board.hide(toPop);
			} else {
				board.applyhide(false);
			}
			sleep(50);
		}
		int comboSize = 0;
		HashMap<PuyoType, Integer> colors = new HashMap<>();
		ArrayList<Integer[]> popped = new ArrayList<>();
		biggloop:
		for (Integer[] coords : toPop) {
			for (Integer[] pops : popped) {
				if (pops[0] == coords[0] && pops[1] == coords[1]) {
					continue biggloop;
				}
			}
			PuyoInfo type = board.getPuyoAt(coords[0], coords[1]);
			if (type.type != PuyoType.GARBAGE && !type.ice) {
				comboSize++;
				colors.put(type.type, colors.getOrDefault(type, 0)+1);
			}
			if (type.ice) {
				board.setIcy(coords[0], coords[1], false);
			} else {
				board.setPuyoAt(coords[0], coords[1], null);
			}
			
			if (PuyoGameMain.quality == RenderQuality.HIGH) {
				for (int i = 0; i < 2; i++) {
					PuyoGameMain.getGameEngine().addGameObject(new ParticlePuyoPop((Math.random()*3d)-1.5, type.type), board.getX()+(coords[0]*16), board.getY()+((coords[1]-2)*16));
				}
			}
			popped.add(coords);
		}
		
		int scoreDifference = calculateScore(comboSize, consecutive, colors, fever);
		board.addScore(scoreDifference);
		scoreToSend += scoreDifference;

		board.hide(null);
		if (board.getControls() instanceof JoyconControls) {
			((JoyconControls) board.getControls()).getJoycon().vibrate(false, (byte) Math.max(1,10-consecutive), false, (byte) 0);
		}
		GameSounds.valueOf("CHAIN_" + (consecutive <= 7 ? consecutive : 7)).play();
	}
	
	private static final int consecScores[] = {3,10,20,27,40,82,137,206,277,348,438,528,623,716,810,905,999};
	private static final int groupScores[] = {0,2,3,4,5,6,7,10};
	private static final int colorScores[] = {0,3,6,12,24};
	
	private static final int consecScoresFever[] = {4,11,20,25,34,55,92,139,186,281,329,339,405,476,526,576,624,672,720,768,816,864,912,960,999};
	private static final int groupScoresFever[] = {0,1,2,3,4,5,6,8};
	private static final int colorScoresFever[] = {0,2,4,8,16};
	protected int calculateScore(int puyoCount, int consecutive, HashMap<PuyoType,Integer> matchColors, boolean fever) {
		
		int[] chainPArray = (fever ? consecScoresFever : consecScores);
		int[] colorPArray = (fever ? colorScoresFever : colorScores);
		int[] groupPArray = (fever ? groupScoresFever : groupScores);
		
		int groupBonus = 0;
		int chainPower = chainPArray[Math.min(chainPArray.length-1, consecutive-1)];
		int colorBonus = colorPArray[Math.min(colorPArray.length-1, matchColors.size()-1)];
		for (Entry<PuyoType,Integer> match : matchColors.entrySet()) {
			if (match == null) { continue; }
			if (match.getValue() < 4) { continue; }
			groupBonus += groupPArray[Math.min(groupPArray.length-1, match.getValue()-4)];
		}
		int score = (10 * puyoCount) * Math.max(1, Math.min((chainPower + colorBonus + groupBonus), 999));
		return score;
	}
	
	protected int calculateGarbage(int score) {
		double gPoints = ((double) score / (board instanceof ObjPuyoFeverBoard ? 120 : 70));
		int gAmount = (int) Math.floor(gPoints);
		return gAmount;
	}	
	
	
	protected void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<ParticleFallingPuyo> getFallingPuyos() {
		return falling;
	}
}
