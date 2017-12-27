package infosys.kalyan.IronMan;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class Player extends GameObject{
    private int score;
    private boolean up;
    private boolean playing;
    private long startTime;
    private Bitmap image;

    public Player(Bitmap res, int w, int h) {

        x = 100;
        y = GamePanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;
        image = res;
        startTime = System.nanoTime();
    }

    public void setUp(boolean b){up = b;}

    public void update()
    {
        long elapsed = (System.nanoTime()-startTime)/1000000;
        if(elapsed>100)
        {
            score++;
            startTime = System.nanoTime();
        }

        if(up){
            dy -=1;
        }else{
            dy +=1;
        }

        if(dy>9)dy = 9;
        if(dy<-9)dy = -9;

        y += dy;

    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image,x,y,null);
    }
    public int getScore(){return score;}
    public boolean getPlaying(){return playing;}
    public void setPlaying(boolean b){playing = b;}
    public void resetDY(){dy = 0;}
    public void resetScore(){score = 0;}
}
