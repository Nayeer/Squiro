package com.example.nayeer.squiro.GameOver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.nayeer.squiro.MainActivity;
import com.example.nayeer.squiro.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import static android.graphics.Color.rgb;

public class GameOverActivity extends AppCompatActivity {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    JSONArray scores;
    EditText pseudoInput;
    String pseudo;
    int addingPos = -1;
    Double latitude = null, longitude = null;
    // Request code to use when requesting location permission
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    // Localization:
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean hasBeenSaved = false;

    private boolean notHandledAction = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        getWindow().setBackgroundDrawable(new ColorDrawable(0)); //transparence de l'activity

        //services de location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //récupération des données de l'intent
        Intent intent = getIntent();
        int actualScore = intent.getExtras().getInt("actualScore");

        //récupération des éléments graphiques pour y faire des modifications en fonction du score
        TextView bestLabel = (TextView) findViewById(R.id.bestLabel);
        TextView bestScorePseudo = (TextView) findViewById(R.id.bestScorePseudo);
        TextView scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        ImageView scoreMedal = (ImageView) findViewById(R.id.scoreMedal);
        ImageView bestScoreMedal = (ImageView) findViewById(R.id.bestScoreMedal);
        pseudoInput = (EditText) findViewById(R.id.pseudoInput);
        TextView bestScoreTextView = (TextView) findViewById(R.id.bestScoreTextView);

        //un peu de Design impossible à faire en xml. (même shape pour plusieurs views)
        GradientDrawable bgShape = (GradientDrawable)scoreTextView.getBackground();
        bgShape.setStroke(5, rgb(0, 0, 0));
        bgShape = (GradientDrawable)bestScoreTextView.getBackground();
        bgShape.setStroke(3, rgb(161, 139, 18));
        setWindowSize();

        //Le score actuel peut déjà être montré.
        scoreTextView.setText(""+actualScore);

        //Obtention des sharedPreferences déjà en place
        scores = new JSONArray();
        prefs = getApplicationContext().getSharedPreferences(MainActivity.sharedPreferencesName, MODE_PRIVATE);
        editor = prefs.edit();

        //On essaie de lire les sharedPreferences pour obtenir le meileur score
        try {
            scores = new JSONArray(prefs.getString("scores", "[]"));
            //Si le tableau n'est pas vide on affiche le best_score qui est le premier élément
            // car on ajoute de façon à ce que ça soit toujours trié.
            if (scores.length() > 0) {
                bestScoreTextView.setText(""+scores.getJSONArray(0).getInt(0));
                bestScorePseudo.setText(""+scores.getJSONArray(0).getString(1));
            }
            //Si on en a plus que MainActivity.NB_BACKUPS cela veut dire qu'on a changé le paramètre
            //initial, ce code ne sera donc exécuté que dans ce cas, il évite juste de supprimer les
            //SharedPreferences pour tout réinitialiser.
            if (scores.length() > MainActivity.NB_BACKUPS) {
                scores = getSubJSONArray(scores, MainActivity.NB_BACKUPS);
                //On push directement pour éviter de modifier la logique de la classe
                //cela est exceptionnel.
                editor.putString("scores", scores.toString());
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Conteneur des infos sur cette partie
        JSONArray actualScoreInfos = new JSONArray();
        actualScoreInfos.put(actualScore);

        //On essaie d'ajouter son score dans la liste des bestscores
        if (scores.length() < MainActivity.NB_BACKUPS) {
            addingPos = addInOrder(actualScoreInfos, true);
        } else {
            addingPos = addInOrder(actualScoreInfos, false);
        }

        //Si son score a été ajouté, on récupère la location dans un premier temps
        // car le pseudo lui est non disponible pour le moment
        if (addingPos != -1) {
            setUpLocation();
            //Si le score fait partie du top 3 on affiche une petite médaille
            if (addingPos == 0) {
                scoreMedal.setImageResource(R.drawable.gold);
                bestScoreMedal.setImageResource(R.drawable.silver);
                bestLabel.setPaintFlags(bestLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else if (addingPos == 1) {
                scoreMedal.setImageResource(R.drawable.silver);
            }
            else if (addingPos == 2) {
                scoreMedal.setImageResource(R.drawable.bronze);
            }
            else {
                scoreMedal.setVisibility(View.GONE);
            }

        } else {
            //pas d'input field et pas de medaille
            pseudoInput.setEnabled(false);
            pseudoInput.setHint("UnknownPlayer");
            scoreMedal.setVisibility(View.GONE);
        }

    }

    protected void setUpLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // We request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        } else {
            setLastKnownPosition();
        }
    }

    protected void setLastKnownPosition() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    });
        } catch (SecurityException e) {

        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    setLastKnownPosition();
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    //Sert à récupérer les données et mettre à jour le JSONArray avant le push final.
    //pseudo
    protected void setScoreInfos() {
        try {
            pseudo = "" + pseudoInput.getText();
            if (pseudo.equals("")) {
                pseudo = "UnknownPlayer";
            }
            scores.getJSONArray(addingPos).put(pseudo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //longitude et lattitude
        try {
            if (longitude != null && latitude != null) {
                scores.getJSONArray(addingPos).put(longitude);
                scores.getJSONArray(addingPos).put(latitude);
            } else {
                scores.getJSONArray(addingPos).put(JSONObject.NULL);
                scores.getJSONArray(addingPos).put(JSONObject.NULL);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void pushDataToSharedPreferences() {
        //Le score sera sauvegardé quand :
        // - appui sur replay
        // - appui sur quit
        // - appui en dehors de cette activity

        if (addingPos != -1 && !hasBeenSaved) {
            setScoreInfos();
            editor.putString("scores", scores.toString());
            editor.commit();
            hasBeenSaved = true;
            Toast.makeText(getApplicationContext(), "Votre score a été sauvegardé sous le pseudo : "+ pseudo
                    , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onUserLeaveHint() //lorsque l'on quitte l'écran par le bouton home ou autre
    {
        if (notHandledAction) { //si aucune de nos fonctions n'a précédemment été choisie par l'utilisateur
            super.onUserLeaveHint();
            //On simule l'appui sur le bouton replay pour un relançage automatique du jeu lors du retour
            onReplayButton(null);
            finish();
         }
    }

    //resize la fenêtre pour qu'elle soit adaptée à une delimiterView
    protected void setWindowSize() {
        final View delimiterView = findViewById(R.id.delimiterView);

        //On attend de pouvoir récuperer les width et height
        delimiterView.post(new Runnable() {
            @Override
            public void run() {
                //les dimensions sont prêtes
                //getWindow().setLayout(delimiterView.getWidth() , delimiterView.getHeight());

                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.x = 0;
                params.y = 0;
                params.height = delimiterView.getHeight();
                params.width = delimiterView.getWidth();


                getWindow().setAttributes(params);
            }
        });

    }

    public void onReplayButton(View v) {
        notHandledAction = false;
        pushDataToSharedPreferences();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("action", MainActivity.REPLAY);
        startActivity(intent);
    }

    public void onQuitButton(View v) {
        notHandledAction = false;
        pushDataToSharedPreferences();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this will clear all the stack
        intent.putExtra("action", MainActivity.QUIT);
        startActivity(intent);
        //finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //sert à detecter les touch en dehors de l'activity
        super.dispatchTouchEvent(ev);
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);

        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            notHandledAction = false;
            pushDataToSharedPreferences();
        }
        return super.dispatchTouchEvent(ev);
    }

    //Ajoute un element JSONArray dans notre JSONArray principal à la bonne place en fonction du score
    protected int addInOrder(JSONArray actualPlayerInfos, boolean extensible) { //retourne la position d'ajout ou -1.
        try {
            if (scores.length() == 0) {
                scores.put(actualPlayerInfos);
                return (scores.length() - 1);
            }
            else {
                for (int i = 0; i < scores.length(); i++) {
                    if (actualPlayerInfos.getInt(0) /* son score */ >= scores.getJSONArray(i).getInt(0) /* score elemnent i*/) {
                        if (extensible) { //Pour éviter de perdre le dernier élement
                            scores.put(scores.get(scores.length()-1));
                        }
                        for (int j = scores.length() - 1; j > i; j--) {
                            scores.put(j, scores.get(j-1));
                        }
                        scores.put(i, actualPlayerInfos);
                        return (i);
                    }
                }
                //Le score est plus petit que tous les best_scores mais on a encore de la place pour l'ajouter
                if (extensible) {
                    scores.put(actualPlayerInfos);
                    return (scores.length() - 1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //sert lors de la modification de MainActivity.NB_BACKUPS
    protected JSONArray getSubJSONArray(JSONArray ja, int nb_elements) throws JSONException {
        //On n'utilise pas remove() pour des raisons de compatibilité avec les API < 19.
        JSONArray ja_result = new JSONArray();
        for (int i=0; i< nb_elements; i++) {
            ja_result.put(ja.get(i));
        }
        return ja_result;
    }

}
