package infosys.kalyan.IronMan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import static infosys.kalyan.IronMan.IronMan.context;

public class Menu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);
    }

    public void gameStart(View v){
        setContentView(new GamePanel(this));
    }

    public void story(View v){
        SharedPreferences preferences = getSharedPreferences("COLOR_PREF", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("PlayVideo", 0);
        editor.commit();
        Intent goToMenu=new Intent();
        goToMenu.setClass(this, IronMan.class);
        startActivity(goToMenu);
    }

    public void exit(View v){
        finishAffinity();
    }
}
