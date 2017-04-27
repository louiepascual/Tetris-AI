package tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Robot;
import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.util.Arrays;
import java.util.Vector;
import java.util.*;

import java.lang.Math;

import tetris.Shape.Tetrominoes;

public class Board extends JPanel implements ActionListener {


    final int BoardWidth = 10;
    final int BoardHeight = 22;
	java.util.List<int[]> suc = new ArrayList<int[]>();
	java.util.List<String> moves = new ArrayList<String>();

    Timer timer;
    boolean isFallingFinished = false;
    boolean isStarted = false;
    boolean isPaused = false;
    int numLinesRemoved = 0;
    int curX = 0;
    int curY = 0;
    JLabel statusbar;
    Shape curPiece;
    Tetrominoes[] board;
	int globalIndexOfMax = 0;

    /* AI Stuff */
    int[] _board;
	int[] _temp ;

    public void printBoard() {
	System.out.println("Current Board: ");
        int c = 9;
        for(int i=BoardHeight*BoardWidth-1; i>=0; i--) 
		{
                System.out.print(_board[i-c]);
                c -= 2;
                System.out.print(" ");
                
                if(((i) % 10) == 0) {
                    c=9;
                    System.out.print("\n");
                }
				
			
		
		
            
       }
	   System.out.println("");
    }

    public void _clearBoard() {
        for(int i=0; i<BoardHeight*BoardWidth; i++) {
                _board[i] = 0;    
       }
    }
	

    public Board(Tetris parent) {

       setFocusable(true);
       curPiece = new Shape();
       timer = new Timer(25, this);
       timer.start(); 

       statusbar =  parent.getStatusBar();
       board = new Tetrominoes[BoardWidth * BoardHeight];

       _board = new int[BoardWidth * BoardHeight];
       
       _clearBoard();
       
       

       addKeyListener(new TAdapter());
       clearBoard();  
    }

    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }


    int squareWidth() { return (int) getSize().getWidth() / BoardWidth; }
    int squareHeight() { return (int) getSize().getHeight() / BoardHeight; }
    Tetrominoes shapeAt(int x, int y) { return board[(y * BoardWidth) + x]; }


    public void start()
    {
        if (isPaused)
            return;

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();

        newPiece();
        timer.start();

    }

    private void pause()
    {
        if (!isStarted)
            return;

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusbar.setText("paused");
        } else {
            timer.start();
            statusbar.setText(String.valueOf(numLinesRemoved));
        }
        repaint();
    }

    public void paint(Graphics g)
    { 
        super.paint(g);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();


        for (int i = 0; i < BoardHeight; ++i) {
            for (int j = 0; j < BoardWidth; ++j) {
                Tetrominoes shape = shapeAt(j, BoardHeight - i - 1);
                if (shape != Tetrominoes.NoShape) {
                    drawSquare(g, 0 + j * squareWidth(),
                               boardTop + i * squareHeight(), shape);
                    
                }
            }
        }
        


        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(),
                           boardTop + (BoardHeight - y - 1) * squareHeight(),
                           curPiece.getShape());
            }
        }
    }

    private void dropDown()
    {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1))
                break;
            --newY;
        }
        pieceDropped();
    }

    private void oneLineDown()
    {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }


    private void clearBoard()
    {
        for (int i = 0; i < BoardHeight * BoardWidth; ++i)
            board[i] = Tetrominoes.NoShape;

        _clearBoard();



    }

    private void pieceDropped()
    {
        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BoardWidth) + x] = curPiece.getShape();

            _board[(y * BoardWidth) + x] = 1;
        }
    
        removeFullLines();
		printBoard();

        if (!isFallingFinished) {
 
 	       newPiece();
        }
           
    }

    private void newPiece()
    {
        curPiece.setRandomShape();
		try {
		successor();

		} catch (Exception e) {
			curPiece.setShape(Tetrominoes.NoShape);
			timer.stop();
            isStarted = false;
            statusbar.setText("Game Over. Your score is " +numLinesRemoved);	
		}
		
		if (curPiece.getShape() == Tetrominoes.TShape||(curPiece.getShape() == Tetrominoes.LShape) || (curPiece.getShape() == Tetrominoes.ZShape))
		{
		curX = 1;
		}
		else
		{		
        curX = 0;
        
		}
		curY = BoardHeight - 3 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoes.NoShape);
            timer.stop();
            isStarted = false;
            statusbar.setText("Game Over. Your score is " +numLinesRemoved);
        }
		
		try
		{
		doMove(moves.get(globalIndexOfMax));
		}
		catch (Exception e)
		{	
		}
		
    }

    private boolean tryMove(Shape newPiece, int newX, int newY)
    {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)
                return false;
            if (shapeAt(x, y) != Tetrominoes.NoShape)
                return false;
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void removeFullLines()
    {
		
        int numFullLines = 0;
		
        for (int i = BoardHeight - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BoardWidth; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BoardHeight - 1; ++k) {
                    for (int j = 0; j < BoardWidth; ++j)
					{
                         board[(k * BoardWidth) + j] = shapeAt(j, k + 1);
						_board[(k * BoardWidth) + j] = _board[((k+1) * BoardWidth) + j];
					}
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            statusbar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Tetrominoes.NoShape);
            repaint();
        }
     }
	 
	 private void successor()
	 {
		 suc.clear();
		 moves.clear();
		 if (curPiece.getShape() ==  Tetrominoes.SquareShape)
		 {
			 for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) -1;

					if ((loc+1) % 10  == 0)
					{
						//do nothing because edge
					}
					else if (loc < 10|| _temp[loc - BoardWidth] == 1 || _temp[loc-BoardWidth+1] == 1 )
					{
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth] = 7;
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						suc.add(_temp.clone());
						moves.add("0 "+(i-1));
						 
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth] = 0;
						_temp[loc] = 0;
						_temp[loc+1] = 0;	
						break;
					}
					
				}
				
				
			 }
			  
		 }
		
		 
		 else if (curPiece.getShape() ==  Tetrominoes.LineShape)
		 {
					  for (int i = BoardWidth-1; i>=0; i--) //10
					 {
						 _temp = _board;
						for (int j = BoardHeight-1; j >=0; j--) //22 
						{
							
							int loc = i + (j*10) ;
							
							if (loc < 10 || _temp[loc - BoardWidth] == 1 )
							{
								_temp[loc] = 7;
								_temp[loc+BoardWidth] = 7;
								_temp[loc+BoardWidth*2] = 7;
								_temp[loc+BoardWidth*3] = 7;
								
								suc.add(_temp.clone());
								moves.add("0 "+(i));
								 
								
								_temp[loc] = 0;
								_temp[loc+BoardWidth] = 0;
								_temp[loc+BoardWidth*2] = 0;
								_temp[loc+BoardWidth*3] = 0;
								break;
							}
							
						}	
					}
			 			// sideways
					  for (int i = BoardWidth-1; i>=0; i--) //10
					 {
						 _temp = _board;
						for (int j = BoardHeight-1; j >=0; j--) //22 
						{
							
							int loc = i + (j*10) -3 ;
							if ((loc+1) % 10  == 0 || (loc+2) % 10 == 0 || (loc+3)% 10 == 0)
							{
								//do nothing because edge
							}
							else if (loc < 10 || _temp[loc - BoardWidth] == 1  || _temp[loc - BoardWidth+1] == 1  || _temp[loc - BoardWidth+2] == 1  || _temp[loc - BoardWidth+3] == 1 )
							{
								_temp[loc] = 7;
								_temp[loc+1] = 7;
								_temp[loc+2] = 7;
								_temp[loc+3] = 7;
								
								suc.add(_temp.clone());
								moves.add("1 "+(i-3));
								 
								
								_temp[loc] = 0;
								_temp[loc+1] = 0;
								_temp[loc+2] = 0;
								_temp[loc+3] = 0;
								break;
							}
						
						}	
					}
		 }			
		 	
		
		else if (curPiece.getShape() ==  Tetrominoes.TShape)
			
		 {
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2) % 10 == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc] == 1  || _temp[loc+2] == 1 || _temp[loc+1 - BoardWidth] == 1)
					{
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth+2] = 7;
						_temp[loc+1] = 7;
						
						
						suc.add(_temp.clone());
						moves.add("0 "+(i));
						 
						
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth+2] = 0;
						_temp[loc+1] = 0;
						break;
					}
					
				}	
			 }
			 
			 for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 ||  _temp[loc-BoardWidth] == 1  || _temp[loc+1] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth*2] = 7;
						_temp[loc+BoardWidth+1] = 7;
						
						
						
						suc.add(_temp.clone());
						moves.add("1 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth*2] = 0;
						_temp[loc+BoardWidth+1] = 0;
						break;
					}
					
				}	
			 }
			 
			 for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc] == 1  || _temp[loc+1-BoardWidth] == 1)
					{
						_temp[loc+1] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth*2+1] = 7;
						_temp[loc+BoardWidth] = 7;
												
						suc.add(_temp.clone());
						moves.add("3 "+(i));
						 
						
						_temp[loc+1] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth*2+1] = 0;
						_temp[loc+BoardWidth] = 0;
						break;
					}
					
				}	
			 }
			 
			 for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2) % 10 == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 ||  _temp[loc-BoardWidth] == 1  || _temp[loc+2-BoardWidth] == 1 || _temp[loc+1 - BoardWidth] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						_temp[loc+2] = 7;
						_temp[loc+BoardWidth+1] = 7;
						
						
						suc.add(_temp.clone());
						moves.add("2 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+1] = 0;
						_temp[loc+2] = 0;
						_temp[loc+BoardWidth+1] = 0;
						break;
					}
					
				}	
			 }
			 
			 
			 
		 }
		 else if (curPiece.getShape() ==  Tetrominoes.LShape)
		 {
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1 - BoardWidth] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth*2] = 7;
						
						suc.add(_temp.clone());
						moves.add("2 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+1] = 0;
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth*2] = 0;
						break;
					}
				
				}	
			 }
			 
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2) % 10  == 0)
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1] == 1|| _temp[loc+2] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth+2] = 7;
						
						suc.add(_temp.clone());
						moves.add("1 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth+2] = 0;
						break;
					}
				
				}	
			}
			
			for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc>205)
					{
						//do nothing because too high
					}
					else if (loc < 10 || _temp[loc+BoardWidth] == 1  || _temp[loc+1-BoardWidth] == 1 )
					{
						_temp[loc+BoardWidth*2] = 7;
						_temp[loc+BoardWidth*2+1] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+1] = 7;
						
						suc.add(_temp.clone());
						moves.add("0 "+(i));
						 
						
						_temp[loc+BoardWidth*2] = 0;
						_temp[loc+BoardWidth*2+1] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+1] = 0;
						break;
					}
					
				}	
			}
			
			for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2) % 10  == 0)
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1-BoardWidth] == 1 || _temp[loc+2-BoardWidth] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						_temp[loc+2] = 7;
						_temp[loc+2+BoardWidth] = 7;
						
						suc.add(_temp.clone());
						moves.add("3 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+1] = 0;
						_temp[loc+2] = 0;
						_temp[loc+2+BoardWidth] = 0;
						break;
					}
					
				}	
			}
			
	
	}
	else if (curPiece.getShape() ==  Tetrominoes.MirroredLShape)
		 {
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1 - BoardWidth] == 1)
					{
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth*2+1] = 7;
						
						suc.add(_temp.clone());
						moves.add("2 "+(i));
						
						 
						
						_temp[loc] = 0;
						_temp[loc+1] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth*2+1] = 0;
						break;
					}
					
				}	
			 }
			 
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 ||(loc+2) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1 - BoardWidth] == 1 || _temp[loc+2 - BoardWidth] == 1)
					{
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						_temp[loc+2] = 7;
						_temp[loc+BoardWidth] = 7;
						
						suc.add(_temp.clone());
						moves.add("1 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+1] = 0;
						_temp[loc+2] = 0;
						_temp[loc+BoardWidth] = 0;
						break;
					}
					
					
				}	
			 }
			 
			 for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc > 205)
					{
						//do nothing because too high
					}
					else if (loc < 10 ||  _temp[loc-BoardWidth] == 1  || _temp[loc+1 + BoardWidth] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth*2] = 7;
						_temp[loc+BoardWidth*2+1] = 7;
						
						suc.add(_temp.clone());
						moves.add("0 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth*2] = 0;
						_temp[loc+BoardWidth*2+1] = 0;
						break;
					}
					
				}	
			 }
			 
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2)%10 == 0)
					{
						//do nothing because edge
					}
					else if (loc < 10 ||  _temp[loc] == 1  || _temp[loc+1] == 1  || _temp[loc+2-BoardWidth] == 1 )
					{
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth+2] = 7;
						_temp[loc+2] = 7;
						
						suc.add(_temp.clone());
						moves.add("3 "+(i));
						 
						
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth+2] = 0;
						_temp[loc+2] = 0;
						break;
					}
					
				}	
			 }
			 		 
	}
	
	else if (curPiece.getShape() ==  Tetrominoes.SShape)
		 {
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc] == 1  || _temp[loc+1 - BoardWidth] == 1)
					{
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth*2] = 7;
						_temp[loc+1] = 7;
						_temp[loc+BoardWidth+1] = 7;
						
						suc.add(_temp.clone());
						moves.add("0 "+(i));
						 
						
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth*2] = 0;
						_temp[loc+1] = 0;
						_temp[loc+BoardWidth+1] = 0;
						break;
					}
					
				}	
			 }
			 
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2)%10 == 0)
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1 - BoardWidth] == 1 || _temp[loc+2] == 1 )
					{
						_temp[loc] = 7;
						_temp[loc+1] = 7;
						_temp[loc+1+BoardWidth] = 7;
						_temp[loc+BoardWidth+2] = 7;
						
						suc.add(_temp.clone());
						moves.add("1 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+1] = 0;
						_temp[loc+1+BoardWidth] = 0;
						_temp[loc+BoardWidth+2] = 0;
						break;
					}
				
				}	
			 }
	}
	
	else if (curPiece.getShape() ==  Tetrominoes.ZShape)
		 {
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 )
					{
						//do nothing because edge
					}
					else if (loc < 10 || _temp[loc-BoardWidth] == 1  || _temp[loc+1 ] == 1)
					{
						_temp[loc] = 7;
						_temp[loc+BoardWidth] = 7;
						_temp[loc+BoardWidth+1] = 7;
						_temp[loc+BoardWidth*2+1] = 7;
						
						suc.add(_temp.clone());
						moves.add("0 "+(i));
						 
						
						_temp[loc] = 0;
						_temp[loc+BoardWidth] = 0;
						_temp[loc+BoardWidth+1] = 0;
						_temp[loc+BoardWidth*2+1] = 0;
						break;
					}
				}	
			 }
			 
			  for (int i = BoardWidth-1; i>=0; i--) //10
			 {
				 _temp = _board;
				for (int j = BoardHeight-1; j >=0; j--) //22 
				{
					
					int loc = i + (j*10) ;
					if ((loc+1) % 10  == 0 || (loc+2)%10 == 0)
					{
						//do nothing because edge
					}
					else if (loc < 10 ||  _temp[loc] == 1  || _temp[loc+1 - BoardWidth] == 1 || _temp[loc+2-BoardWidth] == 1 )
					{
						_temp[loc+BoardWidth] = 7;
						_temp[loc+1] = 7;
						_temp[loc+1+BoardWidth] = 7;
						_temp[loc+2] = 7;
						
						suc.add(_temp.clone());
						moves.add("1 "+(i));
						 
						
						_temp[loc+BoardWidth] = 0;
						_temp[loc+1] = 0;
						_temp[loc+1+BoardWidth] = 0;
						_temp[loc+2] = 0;
						break;
					}
					
					
				}	
			 }
	}
	
	//printList();
	computeH();
		 	 
}
	 
	 
	private void computeH()
	{
		int indexOfMax = 0;
		
			double bestScore = -99999999.0;
			for (int z = 0; z < suc.size(); z++)
			{
				int[] holder = suc.get(z);
				int numLinesCleared = 0;
				
				for (int i = 0; i < BoardWidth*BoardHeight ; i=i+BoardWidth)
				{
					for (int j=0;j<10;j++)
					{
						if ( holder[i+j] == 0 )
							break;
						else if ( j == 9)
						{
							numLinesCleared ++;
							
						}
					}
				}
			
			
				int holes = 0;										
				for (int i = 0; i < BoardWidth*BoardHeight ; i++)
				{
					if (holder[i] == 0) // if the slot is empty check above
					{
						for (int j = i+BoardWidth; j < 205; j=j+BoardWidth)
						{
							if (holder[j]>0)
							{
								holes++;
								break;
							}
						}
						
					}
				}
				
				
				int bumpiness = 0;
				int previousHeight = 0;
				
				
				double totalHeight = 0.0;
				for (int i = BoardHeight*BoardWidth-1; i > BoardHeight*BoardWidth-11; i--)
				{
					
					for (int j = i; j > 0; j-=BoardWidth)
					{
						if (holder[j]>0)
						{
							if (i == BoardHeight*BoardWidth-1) 			// if first run
							{
								previousHeight = (j/10) +1;
							}
							
							
							
							bumpiness += Math.abs((j/10 + 1 ) - previousHeight); 	// compute bumpinnes
							previousHeight = (j/10)+1;
							totalHeight += j/10+1;					// add to total height
							break;
						}
					}
				}
				
				double currentScore = (-1.5*totalHeight) + (2.25*numLinesCleared) + (-1.0*holes) + (-0.6*bumpiness);
				
				if (currentScore > bestScore)	
				{
					bestScore = currentScore;
					indexOfMax = z;
				}
			}
				
				
		
		int holder[] = suc.get(indexOfMax);
			
		System.out.println("Suggestion:");
		int c = 9;
        for(int i=BoardHeight*BoardWidth-1; i>=0; i--) 
		{
                System.out.print(holder[i-c]);
                c -= 2;
                System.out.print(" ");
                
                if(((i) % 10) == 0) {
                    c=9;
                    System.out.print("\n");
                }
          
		}
		
	   	System.out.println("");
		System.out.println(moves.get(indexOfMax));
		System.out.println(curPiece.getShape());
		System.out.println("");
		globalIndexOfMax = indexOfMax;
		
		
		
	
}

	private void doMove(String t)
	{
		
		Scanner myReader = new Scanner(t);
		int rotate = myReader.nextInt();
		int right = myReader.nextInt();
		
		tryMove(curPiece, curX + 1, curY);
		tryMove(curPiece, curX + 1, curY);
		
		for (int m = 0;m < rotate; m++)
		{
			tryMove(curPiece.rotateLeft(), curX, curY);
		}
		
		
		tryMove(curPiece, curX - 1, curY);
		tryMove(curPiece, curX - 1, curY);
		tryMove(curPiece, curX - 1, curY);
		tryMove(curPiece, curX - 1, curY);
		tryMove(curPiece, curX - 1, curY);
		
		for (int m = 0;m < right; m++)
		{
			 tryMove(curPiece, curX + 1, curY);
		}
		//dropDown();
		
		
	} 
	 
	

	private void printTemp()
	{
					System.out.println("TEMP\tTEMP\tTEMP");
					
				
		int c = 9;
        for(int i=BoardHeight*BoardWidth-1; i>=0; i--) 
		{
                System.out.print(_temp[i-c]);
                c -= 2;
                System.out.print(" ");
                
                if(((i) % 10) == 0) {
                    c=9;
                    System.out.print("\n");
                }
            
       }
	   	System.out.println("");
	}
	
	
	public void printList()
	{
		
		for (int z=suc.size()-1;z>=0;z--)
		{
			int[] holder = suc.get(z);
			System.out.println("TEMP\tTEMP\tTEMP");
			int c = 9;
			for(int i=BoardHeight*BoardWidth-1; i>=0; i--) 
			{
					
					System.out.print(holder[i-c]);
					c -= 2;
					System.out.print(" ");
					
					if(((i) % 10) == 0) {
						c=9;
						System.out.print("\n");
					}
		   }
		System.out.println("");
		try
		{
		System.out.println(moves.get(z));
		}
		catch (Exception e)
		{	}
		}
	}
	
    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape)
    {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102), 
            new Color(102, 204, 102), new Color(102, 102, 204), 
            new Color(204, 204, 102), new Color(204, 102, 204), 
            new Color(102, 204, 204), new Color(218, 170, 0)
        };


        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                         x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                         x + squareWidth() - 1, y + 1);
    }

    class TAdapter extends KeyAdapter {
         public void keyPressed(KeyEvent e) {

             if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape) {  
                 return;
             }

             int keycode = e.getKeyCode();

             if (keycode == 'p' || keycode == 'P') {
                 pause();
                 return;
             }

             if (isPaused)
                 return;

             switch (keycode) {
             case KeyEvent.VK_LEFT:
                 tryMove(curPiece, curX - 1, curY);
                 break;
             case KeyEvent.VK_RIGHT:
                 tryMove(curPiece, curX + 1, curY);
                 break;
             case KeyEvent.VK_DOWN:
                 tryMove(curPiece.rotateRight(), curX, curY);
                 break;
             case KeyEvent.VK_UP:
                 tryMove(curPiece.rotateLeft(), curX, curY);
                 break;
             case KeyEvent.VK_SPACE:
                 dropDown();
                 break;
             case 'd':
                 oneLineDown();
                 break;
             case 'D':
                 oneLineDown();
                 break;
             }

         }
     }
}