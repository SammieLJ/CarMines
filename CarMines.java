/** 
 * @author Samir Subasic, 2007
 */

/* TODO:
  - zmanjšaj zaèetne y koordinate pri startu avta - done
  - ADD to applet no. of mines from web form! - done
  */
  
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Event;

public class CarMines extends Applet implements Runnable{

  // bool logic variables, that I can store "states" of game - Samir 
  boolean game = false, crash=false, game_over=false, playing=false, madeIt=false, ShowMines=false;
  // static variables, that are memory resident - Samir
  static boolean Keypressed=false;
  static int noOfMadeit = 0, noOfGameOvers = 0;
  
  // 15 - rows, 25 - columns
  int pWidth=14, pHeight=24, numberOfMines=3;

  // that is 15 * 25 - 1 places available for playing
  Point[] places = new Point[374];
  
  // direction
  int dir = 0;  // 0 - right
                // 1 - left
                // 2 - up
                // 3 - down

  // defining Mines, later are assigned in procedure fillWithMines()
  Point[] mines;

  // defining fonts, because default are ugly! - Samir
  private Font font;
  
  // defining thread for Game
  Thread runner;

  // class Applet's init, start, stop, 
  public void init() {
          // the number of mines are specified in html file, from param
          String parameterName = "noOfMines";
          String numOfMines = this.getParameter(parameterName);
          numberOfMines = Integer.valueOf( numOfMines ).intValue();
          
          // defining Mines 
          mines = new Point[numberOfMines];
          
          // fill coordinating system
          for(int i=0; i<374; i++)
                  places[i] = new Point(0,0);
          
          // let's fill mines on our minefields! - Samir
          fillWithMines();
          
          // starting position for out veicle! It's located on left side of wall,
          // with random y coordinate!
          places[0].move(1, radnomNoCoor(2));

  }

  public void start() {
          if(runner==null) {
                  runner = new Thread(this);
                  runner.start();
          }
  }

  public void stop() {
          if(runner!=null) {
                  runner.stop();
                  runner = null;
          }
  }
  
  // from runner object
  public void run() {
          
          // always true condition for running game
          while(true) {
                                    
                  // All possible game overs - wall crashings or reaching other side of wall!                  
                  // Wall crashing! Erased bug in "  || (dir==1 && places[0].x==1)" for left side of wall flickering!
                  // If touched left side of wall, it started to flicker (always setting on leftside with random y) - Samir
                  if(( (game_over) || (dir==0 && places[0].x==25) || (dir==2 && places[0].y==1) || (dir==3 && places[0].y==15))) 
                  {
                          // Setting all starting points again
                          game = false;
                          game_over = false;
                          
                          // this is for "made it" counter
                          if (places[0].x==25 && dir==0 )
                          {
                              if (!madeIt)
                                madeIt=true;
                          }
                          
                          // after end (both conditions), we still what to continue with game, from start
                          if (playing)
                          {
                            // starting position                          
                            places[0].move(1, radnomNoCoor(2));
                          
                            // nastavitve min
                            fillWithMines();
                          }  
                  }
                  
             // on every frame we must repaint our "Canvas" ! 
             repaint();      
              
          // we have to pause our game, that repaint, can finish, outherwise,
          // we would have frame dropouts, or not smooth play! Ofcourse code, for
          // updates per second is missing, I didn't had time to implementit! Browser
          // is doing that for me. It would have sense, if it was Swing app. - Samir
          try{ Thread.sleep(70); }
          catch(InterruptedException e) { }

          }
  }

  // repaint is calling update method, which class Applet inherited from class AWT
  public void update(Graphics g) {
  
          // set up message font
          font = new Font("SansSerif", Font.BOLD, 14);
          
          if(game) {
                    for (int i=0; i < numberOfMines; i++)
                      if(places[0].x==mines[i].x && places[0].y==mines[i].y) 
                          {
                                  if (!crash)
                                    crash=true;
                                  game_over = true;                               
                          }
                    
                    // clear former state of screen
                    g.clearRect(0,0,size().width,size().height);
                     
                if(playing) { 
                  // pavzirali bomo, dokler se ne pritisne ponoven keypress
                  UpdateKeypressedState();
                  }
                    
                  g.fillOval(places[0].x*20-20,places[0].y*20-20,20,20);
                  if (ShowMines)
                    for (int i=0; i < mines.length; i++)
                        g.drawRect(mines[i].x*20-20,mines[i].y*20-20,19,19);
                  g.drawString("No. of mines: "+numberOfMines+"  Victorys: "+noOfMadeit+"  Game overs: "+noOfGameOvers+"  Car pos.:  X "+places[0].x+", Y "+places[0].y,10,10);
                          //g.drawString(" V zivo X koord: "+places[0].x+" Y koord: "+places[0].y,300,10);
                  
          } else {
                                  
                  // Clears screen, and draws again mines and new body
                  g.clearRect(0,0,size().width,size().height);

                  g.fillOval(places[0].x*20-20,places[0].y*20-20,20,20);
                          
                  if (ShowMines)       
                    for (int i=0; i < mines.length; i++)
                      g.drawRect(mines[i].x*20-20,mines[i].y*20-20,19,19);
                   
                   g.drawString("No. of mines: "+numberOfMines+"  Victorys: "+noOfMadeit+"  Game overs: "+noOfGameOvers+"  Car pos.:  X "+places[0].x+", Y "+places[0].y,10,10); 
                 // g.drawString("No. of mines: "+numberOfMines+"           X koord: "+places[0].x+" Y koord: "+places[0].y,10,10);

                  if(madeIt)
                  {
                    playing = false;
                    noOfMadeit++;
                    g.setColor(Color.blue);
                    g.setFont(font);
                    g.drawString("YOU HAVE MADE IT! WITH OUT BOOM!",100,160);
                    madeIt = false;
                    try{ Thread.sleep(1500); }
                    catch(InterruptedException e) { }                    
                  }
                  
                  if(crash)
                  {
                    playing = false;
                    noOfGameOvers++;
                    g.setColor(Color.red);
                    g.setFont(font);
                    g.drawString("YOU HAVE DROVE ON MINE! SO, IT'S GAME OVER!",80,160);
                    crash=false;
                    try{ Thread.sleep(1500); }
                    catch(InterruptedException e) { }      
                  } 
          }                  
  }

  public boolean keyDown(Event evt, int key) {
        if(key == Event.RIGHT && places[1].x!=places[0].x+1) { dir=0; game=true; playing=true; Keypressed=true; return true; }
        else if(key == Event.LEFT && places[1].x!=places[0].x-1) { dir=1; game=true; playing=true; Keypressed=true; return true; }
        else if(key == Event.UP && places[1].y!=places[0].y-1) { dir=2; game=true; playing=true; Keypressed=true; return true; }
        else if(key == Event.DOWN && places[1].y!=places[0].y+1) { dir=3; game=true; playing=true; Keypressed=true; return true; }
        else if (key == 109) { ShowMines = true; return true; } // key m
        else if (key == 77) { ShowMines = false; return true; } // key M
        else if (key == Event.F2) { game=true; playing=true; return true; } // key M
        else if(key == 112 || key == 1508) { // key F1
                playing=false;
                return true;
        }
        return false;
  }
  
  // in-game logic: 
  // issue number for x or y
  private int radnomNoCoor (int someNumber)
  {
    // Ok, let's explain what this magic numbers mean. 1 stands for x, and 2 for y coords!
    // to set range of randome numbers, from 1 to Width or Height
    int result=0;
    if (someNumber == 2)
      result = (int)(Math.random()* pWidth +1);
      
    if (someNumber == 1)
      result = (int)(Math.random()* pHeight +1);

      return result;
  }
  
  // let's fill minefield with mines
  private void fillWithMines()
  {
    for (int i=0; i < mines.length; i++)
    {
      mines[i] = new Point(radnomNoCoor(1), radnomNoCoor(2));
      if ( (i < 0) && (mines[i-1].x == mines[i].x || mines[i-1].y == mines[i].y))
      {
        mines[i] = new Point(radnomNoCoor(1), radnomNoCoor(2));
      }
    }
  }
    
    // pavzirali bomo, dokler se ne pritisne ponoven keypress
    private void UpdateKeypressedState()
    {
        if (Keypressed)
            {
                  switch(dir) 
                  {
                       case 0: places[0].move(places[0].x+1, places[0].y); break;
                       case 1: places[0].move(places[0].x-1, places[0].y); break;
                       case 2: places[0].move(places[0].x, places[0].y-1); break;
                       case 3: places[0].move(places[0].x, places[0].y+1); break;
                  }

                 Keypressed=false;
            } 
    } 
}
