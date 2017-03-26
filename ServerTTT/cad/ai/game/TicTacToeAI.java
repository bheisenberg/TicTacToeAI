/*******************
 * Christian A. Duncan
 * AI Functionality by Brian Eisenberg, Nevo Mantel, Jake Frommer and Nick Pinero
 * CSC350: Intelligent Systems
 * Spring 2017
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 * 
 * Saves all game states to a hash table and appends the game result to them
 * When choosing a move, AI picks the move with the best record
 ********************/

package cad.ai.game;
import java.util.Random;
import java.util.Set;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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
    public LinkedHashMap<String, Record> Memory = new LinkedHashMap <String, Record>();
    public List<String> gameStates = new ArrayList<String>();
    private int verbose = 0;
    float heat = 1f;
    private String homeMemory = "HomeMemory.txt";
    private String awayMemory = "AwayMemory.txt";
    char symbol;

    public TicTacToeAI() {
	game = null;
	
	ReadInMemory();
	ran = new Random();
    }
    
    private String getMemory () {
    	return symbol == 'X' ? homeMemory : awayMemory;
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
    	
    	//Returns the score for a game state if the AI is 'O'
    	public float Oscore () {
    		return (float)(((float)Wins-(float)Losses)/((float)Wins+(float)Losses+(float)Ties));
    	}
    	
    	//Returns the score for a game state if the AI is 'X'
    	public float Xscore () {
    		return (float)(((float)Losses-(float)Wins)/((float)Wins+(float)Losses+(float)Ties));
    	}
    }
    
    //Finds the best move based on all of the possibilities for current game state
    private int BestMove (String state, int randomMove, char symbol) {
    	int bestMove = randomMove;
    	float bestRecord = -1000; //-1000 so that every state gets considered no matter how bad the record
    	String tempState = "";
    	String bestState = "";
    	if(verbose > 0) System.out.println("Checking all possibilities for state " +state);
    	char [] stateArray = state.toCharArray();
    		if(verbose>0)System.out.println("Calculated move");
			for (int i=0; i < stateArray.length; i++) {
				if(stateArray[i] == '-') {
					float score = 0;
					float randomValue = new Random().nextFloat();
					char [] tempStateArray = stateArray.clone();
					tempStateArray[i] = symbol;
					tempState = new String(tempStateArray);
					if(Memory.containsKey(tempState)) {
						//Since the AI was only trained on away, we have to flip the wins/losses for each game state for optimal moves
						if(symbol == 'O') {
							score = Memory.get(tempState).Oscore();
						} else if (symbol == 'X') {
							score = Memory.get(tempState).Xscore();
						}
						score+=randomValue*heat; //Add the heat on our move for randomness
					} else {
						if(verbose>0)System.out.println("State "+tempState+" not found");
						score = randomValue*heat; //If the game state is not yet saved, we just give it a random value
					}
    				if (score > bestRecord) {
    					//Sets the potential best move as best move
    					bestRecord = score;
    					bestMove = i;
    					bestState = tempState;
    					if(verbose>0)System.out.println("Potential best move: "+tempState+" has a score of " +score);
    				}
				}
			}
		gameStates.add(bestState);
		//Once all potential moves are iterated through, we return the best one
    	return bestMove;
    }
    
    //Adds the game result to the state's record
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
    
    //Loads each line of our text file and adds them to memory
    public void ReadInMemory() {
    	try (BufferedReader in = new BufferedReader(new FileReader(getMemory()))) {
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
    
    //Parses a line from our text file and adds it as a record into memory
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
    
    //Adds all of the game states to memory after a game
    public void AddGameToMemory (List<String> gameStates, String result) {
    	for(int i=0; i < gameStates.size(); i++) {
    		String state = gameStates.get(i);
	    	if(!Memory.containsKey(state)) {
	    		Memory.put(state, new Record());
	    		if(verbose > 1)System.out.println("Created new entry for state " +state);
	    	}
		    Memory.put(state, AddToRecord(Memory.get(state), result));
		    if(verbose > 1) System.out.println("State " +state+ " record is now: W: " +Memory.get(state).Wins+ " L: " +Memory.get(state).Losses+ " T: " +Memory.get(state).Ties);
	    }
    }
    
    //Replaces white space with dashes (originally a more complex method, but its functionality was simplified later on which is why it looks so strange)
    private char parsed (char item) {
	    	switch(item) {
		    case ' ':
		    	return '-';
	    	}
    	return item;
    }
    
    //Converts the board char[] into a parsed string for further usage
    public String getBoardState (char[] board, char symbol) {
    	String state = "";
    	for(int i=0; i < board.length; i++) {
    		state+=parsed(board[i]);
    	}
    	return state;
    }
    
    //Method to help check who won the game
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
    
    //Returns a symbol based on the current player
    public char PlayerToSymbol (int player) {
    	return player == 0 ? 'X' : 'O';
    }
    
    //Determines the result of the game based on your symbol and the winner
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
    
    //Old random move made into a method (unused)
    public int RandomMove (char[] board) {
		int openSlots = 0;
		int i = 0;
		
		for (i = 0; i < board.length; i++) {
		    if (board[i] == ' ') openSlots++;
		}
	
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
		return pos;
    }
    
    public synchronized String computeMove() {
		if (game == null) {
		    System.err.println("CODE ERROR: AI is not attached to a game.");
		    return "0,0";
		}
		
		//Gets the player's symbol
		symbol = PlayerToSymbol(game.getPlayer());
		
		//Get the board state and converts it to a string
		char[] board = (char[]) game.getStateAsObject();
		String boardState = getBoardState(board, symbol);
		
		//Adds the current board state to 
		gameStates.add(boardState);
		
		int bestMove = BestMove(boardState, RandomMove(board), symbol);
		if(verbose > 0)System.out.println("I am "+PlayerToSymbol(game.getPlayer()));
		if(verbose > 0) System.out.println("States: "+gameStates);
	
		return "" + bestMove;
    }	
   

    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    @Override
    public synchronized void postWinner(char result) {
		// This AI probably wants to store what it has learned
		// about this particular game    	
    	// Adds all game states to memory and clears the arraylist
    	AddGameToMemory(gameStates, getResultValue(ResultToInt(result)));
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
	    	try(PrintWriter out = new PrintWriter(getMemory())) {
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
