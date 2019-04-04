package me.ipodtouch0218.multiplayerpuyo.misc;

import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPartyBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoFeverBoard;

public final class Gamemodes {
	
	//---multiplayer gamemodes---//
	public static final Gamemode TSU = new Gamemode(false, new GamemodeSettings(false, false, true), "Tsu");
	public static final Gamemode FEVER = new Gamemode(false, new GamemodeSettings(true, true, true), "Fever");
	public static final Gamemode NONSTOP_FEVER = new Gamemode(false, new GamemodeSettings(true, true, true), "Nonstop Fever") {
		@Override
		public void createBoards(PuyoBoardManager manager, int amount, int width, int height) {
			for (int i = 0; i < amount; i++) {
				ObjPuyoFeverBoard board = (ObjPuyoFeverBoard) manager.createBoard(width, height, i);
				
				board.getFeverManager().setTimeRemaining(-1);
				board.getFeverManager().setCharge(7);
			}
		}
		@Override
		public void onRoundStart(PuyoBoardManager manager) {
			for (ObjPuyoBoard b : manager.getBoards()) {
				ObjPuyoFeverBoard fb = (ObjPuyoFeverBoard) b;
				fb.getFeverManager().setTimeRemaining(-1);
				fb.getFeverManager().setCharge(7);
				fb.feverFlip();
				fb.getGarbageIndicator().setOverallGarbage(GarbageSprites.MOON.getValue() + GarbageSprites.RED.getValue()*5);
			}
		}
	};
	public static final Gamemode ICE_PUYO = new Gamemode(false, new GamemodeSettings(false, false, true), "Ice Puyo");
	public static final Gamemode PARTY = new Gamemode(false, new GamemodeSettings(false, true, true), "Party") {
		public void onRoundStart(PuyoBoardManager manager) {
			for (ObjPuyoBoard b : manager.getBoards()) {
				b.setReadyForPuyo(false);
				
				((ObjPartyBoard) b).dropFirstItem();
			}
		}
		public void createBoards(PuyoBoardManager manager, int amount, int width, int height) {
			super.createBoards(manager, amount, width, height);
			manager.setWinsUntilVictory(1);
		}
	};
	
	//---singleplayer gamemodes---//
	public static final Gamemode ENDLESS_PUYO = new Gamemode(true, new GamemodeSettings(false, false, true), "Endless Puyo");
	public static final Gamemode ENDLESS_FEVER = new Gamemode(true, new GamemodeSettings(true, true, true), "Endless Fever") {
		@Override
		public void createBoards(PuyoBoardManager manager, int amount, int width, int height) {
			for (int i = 0; i < amount; i++) {
				ObjPuyoFeverBoard board = (ObjPuyoFeverBoard) manager.createBoard(width, height, i);
				board.getFeverManager().setTimeRemaining(60);
				board.getFeverManager().setCharge(7);
			}
		}
		@Override
		public void onRoundStart(PuyoBoardManager manager) {
			for (ObjPuyoBoard b : manager.getBoards()) {
				ObjPuyoFeverBoard fb = (ObjPuyoFeverBoard) b;
				fb.getFeverManager().setTimeRemaining(60);
				fb.getFeverManager().setCharge(7);
				fb.feverFlip();
				
			}
		}
		@Override
		public void onBoardFeverEnd(ObjPuyoFeverBoard board) {
			board.gameOver();
		}
	};

	
	//---gamemode class---//
	public static class Gamemode {

		private GamemodeSettings gamemodeSettings;
		private boolean singleplayer;
		private String dispName;
		
		public Gamemode(boolean singleplayer, GamemodeSettings gamemodeSettings, String name) {
			this.singleplayer = singleplayer;
			this.gamemodeSettings = gamemodeSettings;
			this.dispName = name;
		}
		
		//--EVENTS--//
		public void createBoards(PuyoBoardManager manager, int amount, int width, int height) {
			for (int i = 0; i < amount; i++) {
				manager.createBoard(width, height, i);
			}
		}
		
		/*
		 * When a board "gameover"s
		 */
		public void onBoardGameOver(ObjPuyoBoard board) {
			
		}
		
		/*
		 * Called right as the players get control of the boards
		 */
		public void onRoundStart(PuyoBoardManager manager) {
			for (ObjPuyoBoard b : manager.getBoards()) {
				b.setReadyForPuyo(true);
			}
		}
		
		/*
		 * Called immediately when the last board "gameover"s
		 */
		public void onRoundOver(PuyoBoardManager manager) {}
		
		/*
		 * Called when all boards should be reset for the next round.
		 */
		public void onRoundReset(PuyoBoardManager manager) {}
		public void onBoardFeverEnd(ObjPuyoFeverBoard board) {}
		public void onTimerEnd(PuyoBoardManager manager) {}
		
		//---getters---//
		public GamemodeSettings getGamemodeSettings() { return gamemodeSettings; }
		public boolean isSingleplayer() { return singleplayer; }
		@Override
		public String toString() { return dispName; }
	}
	public static class GamemodeSettings {
		
		public GamemodeSettings(boolean fever, boolean smooth, boolean verticalFlip) {
			this.fever = fever;
			this.smoothDropper = smooth;
			this.verticalFlip = verticalFlip;
		}
		
		public boolean fever; //if the board should have fever enabled
		public boolean smoothDropper; //if the dropper should be smooth instead of moving at 0.5 intervals.
		public boolean verticalFlip; //if the dropper is allowed to vertically flip.
		
	}
	
	
	
	
}