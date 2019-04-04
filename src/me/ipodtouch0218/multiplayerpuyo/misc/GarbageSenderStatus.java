package me.ipodtouch0218.multiplayerpuyo.misc;

import java.util.ArrayList;

import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.senders.ParticleGarbageSender;

public class GarbageSenderStatus {

	private ObjPuyoBoard board;
	public boolean boardChainOver;
	public ArrayList<ParticleGarbageSender> senders = new ArrayList<>();
	private boolean verified;
	
	public GarbageSenderStatus(ObjPuyoBoard board) {
		this.board = board;
	}
	
	public void checkForFinished() {
		if (!allFinished()) { return; }
		if (verified) { return; }
		board.getBoardManager().verifyGarbage(board);
		verified = true;
	}
	private boolean allFinished() {
		if (!boardChainOver) { return false; }
		for (ParticleGarbageSender sender : senders) {
			if (!sender.done()) { return false; }
		}
		return true;
	}
	
	public ObjPuyoBoard getBoard() { return board; }
	
}
