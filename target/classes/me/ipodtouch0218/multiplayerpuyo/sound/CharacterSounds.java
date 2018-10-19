package me.ipodtouch0218.multiplayerpuyo.sound;

import me.ipodtouch0218.java2dengine.sound.Sound;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter;

public enum CharacterSounds {

	CHAIN_1("chain/chain1.wav"),
	CHAIN_2("chain/chain2.wav"),
	CHAIN_3("chain/chain3.wav"),
	CHAIN_4("chain/chain4.wav"),
	CHAIN_5("chain/chain5.wav"),
	SPELL_1("chain/spell1.wav"),
	SPELL_2("chain/spell2.wav"),
	SPELL_3("chain/spell3.wav"),
	SPELL_4("chain/spell4.wav"),
	SPELL_5("chain/spell5.wav"),
	COUNTER("chain/counter.wav"),
	
	FEVER_FAIL("fever/failure.wav"),
	FEVER_SUCCESS("fever/successful.wav"),
	FEVER_START("fever/start.wav"),
	
	GARBAGE_SMALL("garbage/small.wav"),
	GARBAGE_BIG("garbage/big.wav"),
	
	SELECT("select.wav"),
	WIN("win.wav"),
	LOSE("lose.wav");
	
	private String location;
	
	CharacterSounds(String loc) {
		location = loc;
	}
	
	public Sound getSound(PuyoCharacter chara) {
		return Sound.getSound("character/" + chara.getResStr() + "/" + location, true);
	}
	public String getLocation() {
		return location;
	}
}
