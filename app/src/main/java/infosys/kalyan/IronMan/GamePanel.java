package infosys.kalyan.IronMan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 997,HEIGHT = 561, MOVESPEED = -5;
    private long smokeStartTime,missileStartTime,endTime=0;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<Mummy> mummy;
    private Random rand = new Random();
    private int maxBorderHeight,minBorderHeight;
    private boolean topDown = true;
    private boolean out=false,mummyOut=false;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;
    private Explosion explosion;
    private BrickFall brickFall=null;
    private int best;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public GamePanel(Context context) {
        super(context);
        this.context=context;
        preferences = context.getSharedPreferences("COLOR_PREF", context.MODE_PRIVATE);
        editor = preferences.edit();
        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);
        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.ironman1), 106,35);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        mummy = new ArrayList<Mummy>();
        smokeStartTime=  System.nanoTime();
        missileStartTime = System.nanoTime();

        best=preferences.getInt("Score",0);

        for(int i=0;i<24;i++) {
                mummy.add(new Mummy(BitmapFactory.decodeResource(getResources(), R.drawable.mum1), BitmapFactory.decodeResource(getResources(), R.drawable.mum2),
                        BitmapFactory.decodeResource(getResources(), R.drawable.mum3), BitmapFactory.decodeResource(getResources(), R.drawable.mum4),
                        BitmapFactory.decodeResource(getResources(), R.drawable.mum5), 84, 68, 60, 64, 71, 106, 42*i, 430));
        }

        for(int i = 0; i*20<WIDTH+40;i++){
            //first top border create
            if(i==0){
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, 10));
            }
            else{
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, topborder.get(i-1).getHeight()+2));
            }
        }

        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!out) {
                if (!player.getPlaying()) {
                    player.setPlaying(true);
                    player.setUp(true);
                } else if (player.getPlaying()) {
                    player.setUp(true);
                }
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            if(!out)
                player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update(){
        if(player.getPlaying()) {
            if(topborder.isEmpty()){
                player.setPlaying(false);
                return;
            }

            if(brickFall==null){
                brickFall=new BrickFall(BitmapFactory.decodeResource(getResources(),R.drawable.brick),997,0,100,2);
            }else{
                if(brickFall.getY()>=561 || brickFall.getX()<=-20){
                    brickFall=null;
                }else{
                    brickFall.update();
                }
            }

            bg.update();
            player.update();

            for(Mummy mummy1:mummy){
                mummy1.update();
                if(collision(mummy1,player)) {
                    mummyOut=true;
                    player.setPlaying(false);
                    break;
                }
            }

            maxBorderHeight = 50+player.getScore()/progressDenom;
            if(maxBorderHeight > 200)maxBorderHeight = 200;

            minBorderHeight = 5+player.getScore()/progressDenom;

            //check top border collision
            for(int i = 0; i <topborder.size(); i++){
                if(collision(topborder.get(i),player)) {
                    player.setPlaying(false);
                    break;
                }
            }

            //update top border
            this.updateTopBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            long max=1500 - player.getScore()/4;
            if(max<100){
                max=100;
            }
            if(missileElapsed >max){
                //first missile always goes down the middle
                if(missiles.size()==0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }else{
                    int height1=500;
                    while(height1>430){
                        height1=(int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2)))+maxBorderHeight;
                    }
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile), WIDTH+10,
                            height1,45,15, player.getScore(),13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for(int i = 0; i<missiles.size();i++)
            {
                //update missile
                missiles.get(i).update();

                if(collision(missiles.get(i),player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missile if it is way off the screen
                if(missiles.get(i).getX()<-100){
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 100){
                smoke.add(new Smokepuff(player.getX()+5, player.getY()+30));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++){
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10){
                    smoke.remove(i);
                }
            }

            endTime=System.nanoTime();
        }else if(out && !mummyOut){
            if(explosion==null){
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX()+40,
                        player.getY()-30, 100, 100, 25);
            }
            explosion.update();

            if(endTime!=0) {
                long elapsed = (System.nanoTime() - endTime)/1000000;
                if (elapsed > 4000) {
                    if(preferences.getInt("Score",0)<best){
                        editor.putInt("Score", best);
                        editor.commit();
                    }
                    thread.setRunning(false);
                    Intent intent=new Intent(context, Menu.class);
                    context.startActivity(intent);
                }
            }
        }else if(mummyOut){
            //Eaten by mummies

            long elapsed = (System.nanoTime() - endTime)/1000000;
            if (elapsed > 4000) {
                if(preferences.getInt("Score",0)<best){
                    editor.putInt("Score", best);
                    editor.commit();
                }
                thread.setRunning(false);
                Intent intent=new Intent(context, Menu.class);
                context.startActivity(intent);
            }
        }
    }

    public boolean collision(GameObject a, GameObject b){
        int x=b.getX();
        int y=b.getY();
        int width=b.getWidth();
        int height=b.getHeight();

        Rect rect1=new Rect(x, y+18, x+90, y+35);
        Rect rect2=new Rect(x+94, y, x+106, y+18);
        Rect rect3=new Rect(x+53, y+5, x+90, y+18);
        Rect rect4=new Rect(x+90, y+18, x+95, y+26);



        if(Rect.intersects(a.getRectangle(), rect1) || Rect.intersects(a.getRectangle(), rect2) ||
                Rect.intersects(a.getRectangle(), rect3) || Rect.intersects(a.getRectangle(), rect4)){
            out=true;
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas){
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!out) {
                player.draw(canvas);
                //draw smokepuffs
                for (Smokepuff sp : smoke) {
                    sp.draw(canvas);
                }

                //draw missiles
                for (Missile m : missiles) {
                    m.draw(canvas);
                }

                if(brickFall!=null){
                    brickFall.draw(canvas);
                }
            }
            //draw topborder
            for (TopBorder tb : topborder) {
                tb.draw(canvas);
            }

            if(player.getPlaying() || out){
                for(Mummy mummy1:mummy) {
                    mummy1.draw(canvas);
                }
            }

            //draw explosion
            if(out){explosion.draw(canvas);}

            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder(){
        if(player.getScore()%500 ==0){
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
            ),topborder.get(topborder.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxBorderHeight
            ))+1)));
        }
        for(int i = 0; i<topborder.size(); i++)
        {
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight){
                    topDown = false;
                }else if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight){
                    topDown = true;
                }

                if(topDown)
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()+2));
                }else{
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()-2));
                }

            }
        }

    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        if(player.getScore()*3>best){
            best=player.getScore()*3;
        }
        canvas.drawText("DISTANCE: " + (player.getScore()*3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if(!player.getPlaying() && !out)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
    }
}