package me.ipodtouch0218.multiplayerpuyo.objects.particle.senders;

import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoFeverBoard;

public class ParticleGarbageCancelSender extends ParticleSender {

	private PuyoType color;
	private ObjPuyoBoard sender;
	private ObjGarbageIndicator indicator;
	private int amount;
	private int consecutive;
	
	public ParticleGarbageCancelSender(ObjPuyoBoard sender, ObjGarbageIndicator indi, int cancelAmount, int consecutive, PuyoType color) {
		super((int) (sender.getX()+16), (int) (sender.getY()-8), GarbageSprites.LARGE.getSprite(), color.getColor(), 0.44, 7);
		this.color = color;
		this.sender = sender;
		this.amount = cancelAmount;
		this.indicator = indi;
		this.consecutive = consecutive;
	}
	
	public void arrived() {
		int garbage = indicator.getOverallGarbage()-amount;
		indicator.setOverallGarbage(Math.max(0, garbage));
		sender.addDroppableGarbage(-amount);
		
		if (consecutive != -1) {
			ParticleGarbageSender.soundOrder[Math.min(ParticleGarbageSender.soundOrder.length-1, consecutive-1)].play();
		}
		if (garbage >= 0) { return; }
		if (sender instanceof ObjPuyoFeverBoard) {
			ObjPuyoFeverBoard fs = (ObjPuyoFeverBoard) sender;
			if (fs.getFeverManager().isInFever()) {
				if (indicator == fs.getFeverIndicator()) {
					if (fs.getGarbageIndicator().getOverallGarbage() > 0) {
						PuyoGameMain.getGameEngine().addGameObject(new ParticleGarbageCancelSender(sender, sender.getGarbageIndicator(), -garbage, -1, color), x, y);
						return;
					}
				}
			}
		}
		sender.getBoardManager().sendGarbage(sender, -garbage, true, color, consecutive);
	}
}
