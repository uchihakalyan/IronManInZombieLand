package infosys.kalyan.IronMan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

public class IronMan extends Activity {

    private VideoView mVideoView2;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences preferences = getSharedPreferences("COLOR_PREF", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(preferences.getInt("PlayVideo",0)==1){
                Intent goToMenu = new Intent();
                goToMenu.setClass(context, Menu.class);
                startActivity(goToMenu);
        }else{
            setContentView(R.layout.activity_menu);

            mVideoView2 = (VideoView) findViewById(R.id.videoView);
            String uriPath2 = "android.resource://" + getPackageName() + "/" + R.raw.ironmaninzombieland;
            Uri uri2 = Uri.parse(uriPath2);
            mVideoView2.setVideoURI(uri2);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mVideoView2.getLayoutParams();
            params.width = metrics.widthPixels+metrics.widthPixels/10;
            params.height = metrics.heightPixels;
            params.leftMargin = 0;
            mVideoView2.setLayoutParams(params);

            try {
                mVideoView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Intent goToMenu = new Intent();
                        goToMenu.setClass(context, Menu.class);
                        startActivity(goToMenu);
                    }
                });
                mVideoView2.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            editor.putInt("PlayVideo", 1);
            editor.commit();
        }
    }
}
