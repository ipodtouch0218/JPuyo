package me.ipodtouch0218.multiplayerpuyo.misc;

import me.ipodtouch0218.multiplayerpuyo.PuyoType;

public class PuyoInfo implements Cloneable {

	public PuyoType type;
	
	public PartyItem partyItem;
	
	public int icetimer, icehealth = 2;
	public boolean ice;
	
	@Override
	public PuyoInfo clone() {
		try {
			return (PuyoInfo) super.clone();
		} catch (Exception e) {}
		return null;
	}
}
