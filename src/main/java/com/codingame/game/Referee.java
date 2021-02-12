package com.codingame.game;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
//import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Referee extends AbstractReferee {
    // Uncomment the line below and comment the line under it to create a Solo Game
    // @Inject private SoloGameManager<Player> gameManager;
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    
    private char[][] board = new char[8][8];
    private int gameTurn;

    private int BOARD_X = 605 - 45 - 75;
    private int BOARD_Y = 160 - 45 - 75;
    private int BOARD_DX = 106;
    private int BOARD_DY = 108;
	private int BOARD_OFFSET_X = 75;
	private int BOARD_OFFSET_Y = 80;

    private Sprite[] board_piece = new Sprite[32];
    private Sprite[] player_avatar = new Sprite[2];
	private Text[] capture_message = new Text[2];
	private Text[] player_message = new Text[2];

    private String move_buffer;
    private String lastMove = "None";
    
    private ArrayList<String> getMoves()
    {
    	ArrayList<String> move_list = new ArrayList<String>();

		for(int row = 0;row<8;row++)
		{
			for(int col = 0;col<8;col++)
			{
				boolean p1turn = (gameTurn %2 == 0);

				if(
				(board[row][col] == 'b' && !p1turn) ||
				(board[row][col] == 'w' && p1turn)
				)
				{
					//up right move
					if(row > 0 && col < 7 && board[row][col] != 'b')
					{
						if(board[row-1][col+1] != 'w')
						{
							String move_string = "";
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - row + '0');
							move_string = move_string + (char)((col+1) + 'a');
							move_string = move_string + (char)(8 - (row-1) + '0');
							move_list.add(move_string);
						}
					}

					//up straight move
					if(row > 0 && board[row][col] != 'b')
					{
						if(board[row-1][col] == '.')
						{
							String move_string = "";
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - row + '0');
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - (row-1) + '0');
							move_list.add(move_string);
						}
					}

					//up left move
					if(row > 0 && col > 0 && board[row][col] != 'b')
					{
						if(board[row-1][col-1] != 'w')
						{
							String move_string = "";
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - row + '0');
							move_string = move_string + (char)((col-1) + 'a');
							move_string = move_string + (char)(8 - (row-1) + '0');
							move_list.add(move_string);
						}
					}

					//down right move
					if(row < 7 && col < 7 && board[row][col] != 'w')
					{
						if(board[row+1][col+1] != 'b')
						{
							String move_string = "";
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - row + '0');
							move_string = move_string + (char)((col+1) + 'a');
							move_string = move_string + (char)(8 - (row+1) + '0');
							move_list.add(move_string);
						}
					}

					//down straight move
					if(row < 7 && board[row][col] != 'w')
					{
						if(board[row+1][col] == '.')
						{
							String move_string = "";
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - row + '0');
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - (row+1) + '0');
							move_list.add(move_string);
						}
					}

					//down left move
					if(row < 7 && col > 0 && board[row][col] != 'w')
					{
						if(board[row+1][col-1] != 'b')
						{
							String move_string = "";
							move_string = move_string + (char)(col+'a');
							move_string = move_string + (char)(8 - row + '0');
							move_string = move_string + (char)((col-1) + 'a');
							move_string = move_string + (char)(8 - (row+1) + '0');
							move_list.add(move_string);
						}
					}
				}
			}
        }
        
        return move_list;
    }

    private void sendPlayerInputs(Player player) 
    {
 /*
        for(int row = 0;row<8;row++)
        {
            String str = "";
            for(int col = 0;col<8;col++)
            {
                str = str + board[row][col];
            }
            player.sendInputLine(str);
        }
        
        if(gameTurn %2==0)
        {
        	player.sendInputLine("w");
        }
        else
        {
        	player.sendInputLine("b");
        }
*/
		player.sendInputLine(lastMove);

        ArrayList<String> possible_moves = getMoves();
        int N = possible_moves.size();
        
        player.sendInputLine(String.valueOf(N));
        
        for(int i = 0;i<N;i++)
        {
        	player.sendInputLine(possible_moves.get(i));
        }
    }
    
    private void make_move(String move) //makes move on board without checking
    {
    	//convert string into coordinates
    	int r = 8 - (move.charAt(1) - '0');
    	int c = move.charAt(0) - 'a';
    	//pick up piece
    	char piece = board[r][c];
    	board[r][c] = '.';
    	
    	//find index of piece
    	int piece_index = -1;
    	for(int i = 0;i<32;i++)
    	{
    		if(board_piece[i].getX() == BOARD_X + BOARD_OFFSET_X + c*BOARD_DX && board_piece[i].getY() == BOARD_Y + BOARD_OFFSET_Y + r*BOARD_DY)
    		{
    			piece_index = i;
    		}
    	}
    	
		//translate next coordinate
		int next_r = 8 - (move.charAt(3) - '0');
		int next_c = move.charAt(2) - 'a';

		//check if piece must be removed
		if(board[next_r][next_c] != '.')
		{
			int remove_piece_index = -1;
			for(int i = 0;i<32;i++)
			{
				if(board_piece[i].getX() == BOARD_X + BOARD_OFFSET_X + next_c*BOARD_DX && board_piece[i].getY() == BOARD_Y + BOARD_OFFSET_Y + next_r*BOARD_DY)
				{
					remove_piece_index = i;
				}
			}

			//remove piece
			int offx = 0;
			int offy = 0;

			board_piece[remove_piece_index]
					.setVisible(false)
					.setX(offx)
					.setY(offy);
		}

		//move boardpiece
		board_piece[piece_index]
				.setX(BOARD_X + BOARD_OFFSET_X + next_c*BOARD_DX)
				.setY(BOARD_Y + BOARD_OFFSET_Y + next_r*BOARD_DY);


    	//put down piece on board
        board[next_r][next_c] = piece;
    }

    private boolean hasWinner()
	{
		if (gameTurn % 2 == 1)
		{
			for(int col = 0;col<8;col++)
				if (board[0][col] == 'w')
					return true;
			for(int row = 0;row<8;row++)
				for(int col = 0;col<8;col++)
					if (board[row][col] == 'b')
						return false;
		}
		else
		{
			for(int col = 0;col<8;col++)
				if (board[7][col] == 'b')
					return true;
			for(int row = 0;row<8;row++)
				for(int col = 0;col<8;col++)
					if (board[row][col] == 'w')
						return false;
		}
		return true;
	}

	private int getNumCapturedPawns(int player)
	{
		int numCapturedPawns = 0;
		if (player == 0)
		{
			for(int row = 0;row<8;row++)
				for(int col = 0;col<8;col++)
					if (board[row][col] == 'b')
						numCapturedPawns++;
		}
		else
		{
			for(int row = 0;row<8;row++)
				for(int col = 0;col<8;col++)
					if (board[row][col] == 'w')
						numCapturedPawns++;
		}
		return 16 - numCapturedPawns;
	}

	@Override
    public void init() 
    {
		gameManager.setMaxTurns(209);
		gameManager.setTurnMaxTime(100);
		gameManager.setFirstTurnMaxTime(1000);

    	move_buffer = "";

    	//background
		graphicEntityModule.createRectangle()
				.setWidth(1920)
				.setHeight(1080)
				.setFillColor(0x7f7f7f);
		graphicEntityModule.createSprite().setImage("chessboard.png")
				.setX(BOARD_X)
				.setY(BOARD_Y)
				.setBaseWidth(950)
				.setBaseHeight(1000)
				.setAnchor(0);
        
        //player avatar 0
        int player_num = 0;
        int avatarx = 100;
        int avatary = 80;
        
        graphicEntityModule.createSprite()
        		.setImage(gameManager.getPlayer(player_num).getAvatarToken())
        		.setX(avatarx+20)
        		.setY(avatary+30)
        		.setBaseWidth(200)
        		.setBaseHeight(200)
        		.setAnchor(0);
        graphicEntityModule.createText(gameManager.getPlayer(player_num).getNicknameToken())
                .setFontSize(50)
                .setStrokeThickness(2)
                .setX(avatarx+120)
                .setY(avatary+270)
                .setAnchor(0.5);
        
        graphicEntityModule.createSprite()
				.setImage("white_pawn.png")
				.setX(avatarx+120)
				.setY(avatary+350)
				.setAnchor(0.5);

		capture_message[player_num] = graphicEntityModule.createText("Captured pawns: 0")
				.setX(avatarx+120)
				.setY(avatary+470)
				.setFontSize(30)
				.setAnchor(0.5);

		player_message[player_num] = graphicEntityModule.createText("")
				.setX(avatarx+120)
				.setY(avatary+540)
				.setFontSize(30)
				.setAnchor(0.5);


		//player avatar 1
        player_num = 1;
        avatarx = 1550;
        avatary = 80;
        
        graphicEntityModule.createSprite()
			.setImage(gameManager.getPlayer(player_num).getAvatarToken())
			.setX(avatarx+20)
			.setY(avatary+30)
			.setBaseWidth(200)
			.setBaseHeight(200)
			.setAnchor(0);
        graphicEntityModule.createText(gameManager.getPlayer(player_num).getNicknameToken())
        	.setFontSize(50)
        	.setStrokeThickness(2)
        	.setX(avatarx+120)
        	.setY(avatary+270)
        	.setAnchor(0.5);

        graphicEntityModule.createSprite()
			.setImage("black_pawn.png")
			.setX(avatarx+120)
			.setY(avatary+350)
			.setAnchor(0.5);

		capture_message[player_num] = graphicEntityModule.createText("Captured pawns: 0")
				.setX(avatarx+120)
				.setY(avatary+470)
				.setFontSize(30)
				.setAnchor(0.5);

		player_message[player_num] = graphicEntityModule.createText("")
				.setX(avatarx+120)
				.setY(avatary+540)
				.setFontSize(30)
				.setAnchor(0.5);

      //fill board with starting locations
        int next_piece_index = 0;
        
        for(int row = 0;row<8;row++)
        {
            for(int col = 0;col<8;col++)
            {
                if(row < 2)
                {
                    board[row][col] = 'b';
                    board_piece[next_piece_index] = graphicEntityModule.createSprite()
                    		.setImage("black_pawn.png")
							.setX(BOARD_X + BOARD_OFFSET_X + col*BOARD_DX)
							.setY(BOARD_Y + BOARD_OFFSET_Y + row*BOARD_DY);
                    next_piece_index++;
                    		
                }
                else if(row > 5)
                {
                    board[row][col] = 'w';
                    board_piece[next_piece_index] = graphicEntityModule.createSprite()
                    		.setImage("white_pawn.png")
                    		.setX(BOARD_X + BOARD_OFFSET_X + col*BOARD_DX)
                    		.setY(BOARD_Y + BOARD_OFFSET_Y + row*BOARD_DY);
                    next_piece_index++;
                    
                }
                else
                {
                    board[row][col] = '.';
                }
            }
        }
        gameTurn = 0;
    }

	static final Pattern PLAYER_PATTERN = Pattern.compile(
			"^(?<move>\\w+)(\\s+(?<message>.+))?");

	@Override
    public void gameTurn(int turn)
    {
        // Code your game logic.
        // See README.md if you want some code to bootstrap your project.
        
		Player player = gameManager.getPlayer(gameTurn % 2);
		//send player inputs
		sendPlayerInputs(player);
		player.execute();

		try
		{
			//get input from player
			String player_input = player.getOutputs().get(0);

			Matcher match = PLAYER_PATTERN.matcher(player_input);
			if (match.matches())
			{
				String player_move = match.group("move");
				//check that move is legal
				ArrayList<String> possible_moves = getMoves();
				boolean is_legal_move = false;
				for(int i = 0;i<possible_moves.size();i++)
				{
					if(player_move.equals(possible_moves.get(i)))
					{
						is_legal_move = true;
					}
				}

				if(is_legal_move)
				{
					lastMove = player_move;

					//apply move to board
					make_move(player_move);

					capture_message[gameTurn%2].setText(String.format("Captured pawns: %d", getNumCapturedPawns(gameTurn%2)));

					String msg = match.group("message");
					if (msg == null) msg = "";
					else if (msg.length() > 30) msg = msg.substring(0, 27) + "...";

					player_message[gameTurn%2].setText(msg);

					//increment gameturn
					gameTurn++;

					//check winner
					if(hasWinner())
					{
						Player next_player = gameManager.getPlayer(gameTurn % 2);
						next_player.deactivate();
						gameManager.addTooltip(player, player.getNicknameToken() + " wins");
						gameManager.addToGameSummary(player.getNicknameToken() + " wins the game");
						gameManager.endGame();
					}
				}
				else
				{
					player.deactivate("Move:" + player_move + " is not a legal move");
					gameManager.addToGameSummary(player.getNicknameToken() + " played an illegal move: "+ player_move + ".");
					gameManager.endGame();
				}
			}
			else
			{
				player.deactivate("Invalid input");
				gameManager.addToGameSummary(player.getNicknameToken() + " provided invalid input: " + player_input + ".");
				gameManager.endGame();
			}
		}
		catch (TimeoutException e)
		{
			player.deactivate("Time limit exceeded!");
			gameManager.addToGameSummary(player.getNicknameToken() + " did not issue a command in due time.");
			gameManager.endGame();
		}
    }
    
    @Override
    public void onEnd() 
    {
        for (Player p : gameManager.getPlayers()) {
            p.setScore(p.isActive() ? 1 : 0);
        }
    }
}
