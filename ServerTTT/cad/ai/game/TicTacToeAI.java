/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2017
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 ********************/

package cad.ai.game;

import java.util.Random;
import java.util.Set;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.io.BufferedReader;

/***********************************************************
 * The AI system for a TicTacToeGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class TicTacToeAI extends AbstractAI {
    public TicTacToeGame game;  // The game that this AI system is playing
    Random ran;
    public Hashtable<String, Record> Memory = new Hashtable <String, Record>();
    public List<String> gameStates = new ArrayList<String>();
    private int verbose = 2;

    public TicTacToeAI() {
	game = null;
	ReadInMemory();
	ran = new Random();
	
    }
    
    public class Record {
    	private int Wins = 0;
    	private int Losses = 0;
    	private int Ties = 0;
    	
    	public void AddWin() {
    		Wins+=1;
    	}
    	
    	public void AddLoss() {
    		Losses+=1;
    	}
    	
    	public void AddTie() {
    		Ties+=1;
    	}
    }
    
    private int BestMove (String state, char symbol) {
    	int bestMove = 0;
    	int bestRecord = 0;
    	char [] stateArray = state.toCharArray();
    		for (int i=0; i < stateArray.length; i++) {
    			if(stateArray[i] == '0') {
    				char [] tempStateArray = stateArray.clone();
    				tempStateArray[i] = symbol;
    				String tempState = new String(tempStateArray);
    				int score = Memory.get(tempState).Wins;
    				if (score > bestRecord) {
    					bestRecord = score;
    					bestMove = i;
    					System.out.println("Potential best move: "+tempState+" has a score of " +score);
    				}
    			}
    		}
    	return bestMove;
    }
    
    public Record AddToRecord(Record record, String result) {
    	switch (result) {
    	case "W":
    		record.AddWin();
    		break;
    	case "L":
    		record.AddLoss();
    		break;
    	case "T":
    		record.AddTie();
    		break;
    	}
    	return record;
    }
    

    public void attachGame(Game g) {
    	game = (TicTacToeGame) g;
    }
    
    public void ReadInMemory() {
    	System.out.println("Remembering...");
    	try (BufferedReader in = new BufferedReader(new FileReader("Memory.txt"))) {
    		String line;
    		while ((line = in.readLine()) != null) {
    			LineToMemory(line);
    		}
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void LineToMemory (String line) {
    	Record record = new Record();
    	String[] split = line.split(" ");
    	String state = split[0];
    	record.Wins = Integer.parseInt(split[1]);
    	record.Losses = Integer.parseInt(split[2]);
    	record.Ties = Integer.parseInt(split[3]);
    	Memory.put(state, record);
    	if (verbose > 2) System.out.println("Added state "+state+ " to memory");
    }
    
    public void AddGameToMemory (List<String> gameStates, Hashtable<String, Record> tempMemory, String result) {
    	for(int i=0; i < gameStates.size(); i++) {
    		String state = gameStates.get(i);
	    	if(!tempMemory.containsKey(state)) {
	    		tempMemory.put(state, new Record());
	    		if(verbose > 1)System.out.println("Created new entry for state " +state);
	    	}
		    tempMemory.replace(state, AddToRecord(tempMemory.get(state), result));
		    if(verbose > 1) System.out.println("State " +state+ " record is now: W: " +tempMemory.get(state).Wins+ " L: " +tempMemory.get(state).Losses+ " T: " +tempMemory.get(state).Ties);
	    }
    	Memory = tempMemory;
    }
    
    public void SaveMemory () {
    	
    }
    	
    private char parsed (char item) {
    	switch(item) {
    	case ' ':
    		return '0';
    	/*case 'X':
    		return '1';
    	case 'O':
    		return '2';*/
    	}
    	return item;
    }
    
    public String getBoardState (char[] board) {
    	String state = "";
    	for(int i=0; i < board.length; i++) {
    		state+=parsed(board[i]);
    	}
    	return state;
    }
    
    public int ResultToInt(char result) {
    	switch(result) {
    	case 'H':
    		return 0;
    	case 'A':
    		return 1;
    	case 'T':
    		return 2;
    	}
    	return 3;
    }
    
    public char PlayerToSymbol (int player) {
    	return player == 0 ? 'X' : 'O';
    }
    
    public String getResultValue (int intresult) {
    	if(intresult == game.getPlayer()) {
    		if(verbose > 0) System.out.println("AI: I won!");
    		return "W";
    	} else {
    		if(intresult == 2) {
        		if(verbose > 0) System.out.println("AI: I tied.");
    			return "T";
    		} else {
        		if(verbose > 0) System.out.println("AI: I lost.");
    			return "L";
    		}
    	}
    }
    
    
    

    /**
     * Returns the Move as a String "R,S"
     *    R=Row
     *    S=Sticks to take from that row
     **/
    public synchronized String computeMove() {
	if (game == null) {
	    System.err.println("CODE ERROR: AI is not attached to a game.");
	    return "0,0";
	}
	
	
	

	
	char[] board = (char[]) game.getStateAsObject();
	String boardState = getBoardState(board);
	
	
	//System.out.println("AI: "+getBoardState(board));
	gameStates.add(boardState);
	
	if(verbose > 0) System.out.println("States: "+gameStates);
	System.out.println("I am "+PlayerToSymbol(game.getPlayer()));
	System.out.println("Best move: "+BestMove(boardState, PlayerToSymbol(game.getPlayer())));

	// First see how many open slots there are
	int openSlots = 0;
	int i = 0;
	for (i = 0; i < board.length; i++)
	    if (board[i] == ' ') openSlots++;

	// Now pick a random open slot
	int s = ran.nextInt(openSlots);

	// And get the proper row
	i = 0;
	while (s >= 0) {
	    if (board[i] == ' ') s--;  // One more open slot down
	    i++;
	}

	// The position to use is the previous position
	int pos = i - 1;

	return "" + pos;
    }	
   

    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    @Override
    public synchronized void postWinner(char result) {
	// This AI probably wants to store what it has learned
	// about this particular game.
    //System.out.println(game.getState(true));
    	//AddStates(gameStates, dict, )
    	//System.out.println("Result: "+getResultValue(ResultToInt(result)));
    	AddGameToMemory(gameStates, Memory, getResultValue(ResultToInt(result)));
    	gameStates.clear();
    	
    	
	game = null;  // No longer playing a game though.
    }

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    @Override
    public synchronized void end() {
	// This AI probably wants to store (in a file) what
	// it has learned from playing all the games so far...
    	try(PrintWriter out = new PrintWriter("Memory.txt")) {
	        Set<String> keys = Memory.keySet();
    	    for (String key: keys) {
    	        out.println(key+" "+Memory.get(key).Wins+ " " +Memory.get(key).Losses+ " " +Memory.get(key).Ties);
    	    }
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}
