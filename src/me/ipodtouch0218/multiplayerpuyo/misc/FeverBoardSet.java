package me.ipodtouch0218.multiplayerpuyo.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.manager.FeverManager;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;

public class FeverBoardSet {

	private ArrayList<PuyoInfo[][]> presetBoards = new ArrayList<>();
	
	
	public FeverBoardSet(String dir) {
		String contents = getFileAsString("/res/data/fever/" + dir);
		
		String[] boards = contents.split("\n\n");
		
		for (int i = 3; i <= 14; i++) {
			PuyoInfo[][] newBoard = ObjPuyoBoard.createNewBoard(6,12);
			String fullBoard = boards[i-3];
			String[] rows = fullBoard.split("\n");
			int y = 0;
			for (String row : rows) {
				int x = 0;
				symbols:
				for (int symbol : row.chars().toArray()) {
					if (newBoard.length <= x || newBoard[0].length <= y) { continue symbols; }
					newBoard[x][y].type = PuyoType.getPuyoTypeFromSymbol((char) symbol);
					x++;
				}
				y++;
			}

			presetBoards.add(newBoard);
		}
	}
	
	public PuyoInfo[][] getBoardOfChainLength(int length) {
		return presetBoards.get(length-3);
	}
	
	public static String getFileAsString(String filePath) {
	    StringBuilder contentBuilder = new StringBuilder();
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(FeverManager.class.getResourceAsStream(filePath)))) {
	 
	        String sCurrentLine;
	        while ((sCurrentLine = br.readLine()) != null) {
	            contentBuilder.append(sCurrentLine).append("\n");
	        }
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    }
	    return contentBuilder.toString();
	}
}
