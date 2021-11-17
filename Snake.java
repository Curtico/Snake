/*
 * Author: Curtice Gough, cgough2019@my.fit.edu
 * Course: CSE 1002, Section 1, Fall 2021
 * Project: Snake Game
*/
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
public class Snake {
   private static final Random RNG = new Random(Long.getLong("seed", System.nanoTime()));
   private static int delay;
   private static class Apple {
      public int x, y;
      
      public Apple () {
         relocate();
      }
      
      public void relocate () {
         x = RNG.nextInt(42) + 4;
         y = RNG.nextInt(42) + 4;
      }
   }

   private static class Player implements Runnable {
      public int x, y;
      public int direction; // N - 1 | E - 2 | S - 3 | W - 4
      public boolean isAlive;
      private ArrayList<Segment> snake;

      record Segment (int x, int y) { }

      public Player () {
         this.x = 25;
         this.y = 25;
         this.direction = 0;
         this.isAlive = true;
         snake = new ArrayList<Segment>();

         // Start with length of 3
         for (int i = 0; i < 3; i++) {
            snake.add(new Segment(x, y));
         }
      }

      @Override
      public void run () { // Thread to listen for key input
         while (isAlive) {
            try {
               if (StdDraw.isKeyPressed(KeyEvent.VK_UP)) {
                  if (direction != 3 && direction != 1) {
                     direction = 1;
                     Thread.sleep(delay); // Don't turn too fast
                  }
               }
               if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) {
                  if (direction != 4 && direction != 2) {
                     direction = 2;
                     Thread.sleep(delay);
                  }
               }
               if (StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) {
                  if (direction != 1 && direction != 3) {
                     direction = 3;
                     Thread.sleep(delay);
                  }
               }
               if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)) {
                  if (direction != 2 && direction != 4) {
                     direction = 4;
                     Thread.sleep(delay);
                  }
               }

               Thread.sleep(10);
            }
            catch (InterruptedException e) {
               e.printStackTrace();
               Thread.currentThread().interrupt();
            }
         }
      }

      public void grow (final int num) {
         StdAudio.playInBackground("pop.wav");
         for (int i = 0; i < num; i++) {
            snake.add(new Segment(get(size() - 1).x,
                                  get(size() - 1).y));
         }
      }

      public void move () {
         // Move in the appropriate direction
         switch (direction) {
         case 1:
            y++;
            break;
         case 2:
            x++;
            break;
         case 3:
            y--;
            break;
         case 4:
            x--;
            break;
         default:
            break;
         }

         /*
          * Remove the last segment and put a new one in front.
          * This allows all segments to keep their respective
          * positions.
          */
         snake.remove(snake.size() - 1);
         snake.add(0, new Segment(x, y));

         // Border control
         if (x > 46 || x < 4) {
            if (y > 26 || y < 24) {
               isAlive = false;
            }
         }
         if (y > 46 || y < 4) {
            isAlive = false;
         }
         if (x > 50) {
            x -= 51;
         }
         if (x < 0) {
            x += 51;
         }

         // Stop touching yourself
         for (int i = 1; i < size(); i++) {
            if (x == get(i).x && y == get(i).y && size() > 3) {
               isAlive = false;
               break;
            }
         }
      }

      public Segment get (final int i) {
         return snake.get(i);
      }

      public int size () {
         return snake.size();
      }
   }

   public static void draw (final Player snake,
                            final Apple apple,
                            final int score,
                            final Apple yellowApple,
                            final Apple blueApple) {
      StdDraw.enableDoubleBuffering();
      StdDraw.clear();

      // Draw background
      StdDraw.setPenColor(StdDraw.DARK_GRAY);
      StdDraw.filledSquare(0, 0, 1);

      // Draw border
      StdDraw.setPenColor(StdDraw.WHITE);
      StdDraw.square(0.5, 0.5, 0.45);
      StdDraw.rectangle(0.02, 0.5, 0.03, 0.033);
      StdDraw.rectangle(0.98, 0.5, 0.03, 0.033);

      // Draw tunnels
      StdDraw.setPenColor(StdDraw.DARK_GRAY);
      StdDraw.filledSquare(0.02, 0.5, 0.032);
      StdDraw.filledSquare(0.98, 0.5, 0.032);
      
      // Draw snake
      StdDraw.setPenColor(0, 255, 80);
      for (int i = 0; i < snake.size(); i++) {
         StdDraw.filledSquare(snake.get(i).x / 50.0, snake.get(i).y / 50.0, 0.011);
      }

      // Draw apple
      StdDraw.setPenColor(255, 80, 80);
      StdDraw.filledSquare(apple.x / 50.0, apple.y / 50.0, 0.011);

      // Draw yellowApple
      StdDraw.setPenColor(255, 255, 80);
      StdDraw.filledSquare(yellowApple.x / 50.0, yellowApple.y / 50.0, 0.011);

      // Draw blueApple
      StdDraw.setPenColor(145, 145, 255);
      StdDraw.filledSquare(blueApple.x / 50.0, blueApple.y / 50.0, 0.011);

      // Draw score
      StdDraw.setPenColor(StdDraw.WHITE);
      StdDraw.textRight(0.95, 0.97, String.format("Score: %4d", score));

      StdDraw.show();
   }

   public static void main (final String[] args) throws InterruptedException {
      final ExecutorService exec = Executors.newCachedThreadPool();
      StdAudio.loopInBackground("music.wav");

      while (true) {
         // Set all variables to initial values
         final Apple apple = new Apple();
         final Apple yellowApple = new Apple();
         final Apple blueApple = new Apple();
         final Player snake = new Player();
         int score = 0;
         delay = 75;

         draw(snake, apple, score, yellowApple, blueApple);

         // Draw welcome window
         StdDraw.disableDoubleBuffering();
         StdDraw.setPenColor(StdDraw.GRAY);
         StdDraw.filledRectangle(0.5, 0.6, 0.325, 0.3);
         StdDraw.filledRectangle(0.5, 0.2, 0.275, 0.075);
         StdDraw.setPenColor(StdDraw.WHITE);
         StdDraw.rectangle(0.5, 0.6, 0.3, 0.275);
         StdDraw.setFont(new Font("Courier New", Font.BOLD, 15));
         StdDraw.text(0.5, 0.8, "Welcome to Snake!");
         StdDraw.rectangle(0.5, 0.75, 0.1, 0);
         StdDraw.text(0.5, 0.7, "There are 3 types of apples:");
         StdDraw.textLeft(0.32, 0.625, "Red    |  +2 points");
         StdDraw.rectangle(0.5, 0.5875, 0.25, 0);
         StdDraw.textLeft(0.32, 0.55, "Blue   |  +1 point");
         StdDraw.textLeft(0.495, 0.5, "-1 speed");
         StdDraw.rectangle(0.5, 0.4625, 0.25, 0);
         StdDraw.textLeft(0.32, 0.425, "Yellow |  +5 points");
         StdDraw.textLeft(0.495, 0.375, "+1 speed");
         StdDraw.text(0.5, 0.2, "Press 'SPACE' to start");
         StdDraw.rectangle(0.5, 0.2, 0.25, 0.05);
         
         while (!StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
            continue;
         }

         exec.execute(snake); // Start thread to listen for key input
         while (snake.isAlive) {
            snake.move();

            // Collecting apples
            if (snake.x == apple.x && snake.y == apple.y) {
               score += 2;
               snake.grow(1);
               apple.relocate();
               snake.grow(2);
            }
            if (snake.x == yellowApple.x && snake.y == yellowApple.y) {
               delay -= 25;
               score += 5;
               yellowApple.relocate();
               snake.grow(5);
            }
            if (snake.x == blueApple.x && snake.y == blueApple.y) {
               delay += 25;
               score++;
               blueApple.relocate();
               snake.grow(1);
            }

            // Set max speed
            if (delay < 25) {
               delay = 25;
            }

            // Update graphics
            draw(snake, apple, score, yellowApple, blueApple);
            Thread.sleep(delay);
         }

         // Game over
         StdAudio.playInBackground("scream.wav");
         StdDraw.disableDoubleBuffering();
         StdDraw.setPenColor(StdDraw.GRAY);
         StdDraw.filledRectangle(0.5, 0.5, 0.225, 0.15);
         StdDraw.setPenColor(StdDraw.WHITE);
         StdDraw.rectangle(0.5, 0.5, 0.2, 0.125);
         StdDraw.text(0.5, 0.575, "Game Over");
         StdDraw.text(0.5, 0.525, "Final Score: " + score);
         StdDraw.rectangle(0.5, 0.4875, 0.00875, 0);
         StdDraw.text(0.5, 0.45, "Press 'R' to restart");
         
         // Listen for 'R' key
         while (!StdDraw.isKeyPressed(KeyEvent.VK_R)) {
            continue;
         }
      }
   }
}
