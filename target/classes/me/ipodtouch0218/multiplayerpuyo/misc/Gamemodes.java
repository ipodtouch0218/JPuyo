package me.ipodtouch0218.multiplayerpuyo.misc;

import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoFeverBoard;

public final class Gamemodes {
	
	//---multiplayer gamemodes---//
	public static final Gamemode TSU = new Gamemode(false, false, "Tsu");
	public static final Gamemode FEVER = new Gamemode(false, true, "Fever");
	public static final Gamemode NONSTOP_FEVER = new Gamemode(false, true, "Nonstop Fever") {
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
	public static final Gamemode ICE_PUYO = new Gamemode(false, false, "Ice Puyo");
	
	//---singleplayer gamemodes---//
	public static final Gamemode ENDLESS_PUYO = new Gamemode(true, false, "Endless Puyo");
	public static final Gamemode ENDLESS_FEVER = new Gamemode(true, true, "Endless Fever") {
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

		private boolean singleplayer;
		private boolean fever;
		private String dispName;
		
		public Gamemode(boolean singleplayer, boolean fever, String name) {
			this.singleplayer = singleplayer;
			this.fever = fever;
			this.dispName = name;
		}
		
		//events
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
			
		}
		
		/*
		 * Called immediately when the last board "gameover"s
		 */
		public void onRoundOver(PuyoBoardManager manager) {
			
		}
		
		/*
		 * Called when all boards should be reset for the next round.
		 */
		public void onRoundReset(PuyoBoardManager manager) {
			
		}
		
		//---getters---//
		public boolean isSingleplayer() {
			return singleplayer;
		}
		public boolean isFever() {
			return fever;
		}

		public void onBoardFeverEnd(ObjPuyoFeverBoard board) {

			
		}
		
		@Override
		public String toString() {
			return dispName;
		}
	}

}