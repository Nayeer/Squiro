package com.example.nayeer.squiro;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.nayeer.squiro.GameEngine.GameView;
import com.example.nayeer.squiro.ScoreList.ScoresListActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private Sensor mAccelerometer;
    private SensorManager manager;
    private GameView mGameView;

    //Actions enclenchées par intent
    public static final boolean REPLAY = true;
    public static final boolean QUIT = false;

    public static final String sharedPreferencesName = "database";

    //Nombre de score autorisé dans la base (peut être modifié).
    public static final int NB_BACKUPS = 10;
    //Sert à implementer un dimBackground lors du gameOver,
    //puisque celui-ci n'est pas disponible pour certaines API.
    View dimBackground;
    //Evite les appels à onResume conflictuels lors d'un restart
    private boolean restart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Capteur acceleromètre
        manager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        mAccelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Ajout du gameView
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout);
        mGameView = new GameView(this);
        layout.addView(mGameView);
        //Ajout du dimBackground sur toute la surface de l'écran et pas uniquement sur le layout.
        ViewGroup vg = (ViewGroup)(getWindow().getDecorView().getRootView());
        LayoutInflater myinflater = getLayoutInflater();
        dimBackground = myinflater.inflate(R.layout.dim_panel, null);
        vg.addView(dimBackground);
        //Récupération de la progressBar qui est mise au dessus de tout les autres élements.
        View progressBarView = findViewById(R.id.progressBarView);
        progressBarView.bringToFront();
        layout.invalidate();

    }

    protected void onResume() {
        super.onResume();
        if (!restart) { //empêche l'appel après un mGameView.restart().
            manager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
            mGameView.resume();
        }
        restart = false;
        dimBackground.setVisibility(View.GONE);
    }

    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
        mGameView.pause();
        dimBackground.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                //Transmets les evenements à mGameView
                mGameView.onSensorEvent(event);
                break;
        }
    }

    @Override //doit être implémentée
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onNewIntent(Intent intent) {
        //la classe est en singleInstance, on ne peut donc pas récuperer les intents dans onCreate
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            boolean action = bundle.getBoolean("action");
            if (action == REPLAY) {
                restart = true;
                manager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
                mGameView.restart();
            } else if (action == QUIT) {
                finish();
            }
        }
    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.scores:
                Intent intent = new Intent(this, ScoresListActivity.class);
                startActivity(intent);
                return true;
            case R.id.Rejouer:
                mGameView.restart();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
