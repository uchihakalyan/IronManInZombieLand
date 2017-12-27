package infosys.kalyan.IronMan;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Kalyan on 3/28/2017.
 */

public class Mummy extends GameObject{
    private long startTime;
    private Bitmap image[]=new Bitmap[5];
    private int width1[]=new int[5];
    private int i;

    public Mummy(Bitmap res1, Bitmap res2,Bitmap res3, Bitmap res4,Bitmap res5,
                 int w1, int w2, int w3, int w4, int w5, int h, int x, int y) {
        this.x = x;
        this.y = y;
        dy = 0;
        height = h;
        image[0] = res1;image[1] = res2;image[2] = res3;image[3] = res4;image[4] = res5;
        width1[0]=w1;width1[1]=w2;width1[2]=w3;width1[3]=w4;width1[4]=w5;
        i=0;
        width = w1;
        startTime = System.nanoTime();
    }

    public void update()
    {
        long elapsed = (System.nanoTime()-startTime)/1000000;
        if(elapsed>100)
        {
            i++;
            if(i==5){
                i=0;
            }
            width=width1[i];
            startTime = System.nanoTime();
        }
    }

    public void draw(Canvas canvas){canvas.drawBitmap(image[i],x,y,null);}
}