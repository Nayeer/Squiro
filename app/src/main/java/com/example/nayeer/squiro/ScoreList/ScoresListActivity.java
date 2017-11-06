package com.example.nayeer.squiro.ScoreList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.nayeer.squiro.MainActivity;
import com.example.nayeer.squiro.Maps.MapsActivity;
import com.example.nayeer.squiro.R;
import org.json.JSONArray;
import java.util.ArrayList;

public class ScoresListActivity extends AppCompatActivity {

    ArrayList<Score> scores;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefs = getApplicationContext().getSharedPreferences(MainActivity.sharedPreferencesName, MODE_PRIVATE);

        scores = new ArrayList<>();

        //On essaie de lire les sharedPreferences afin de remplir notre array_list
        try {
            JSONArray js_scores = new JSONArray(prefs.getString("scores", "[]"));
            for (int i = 0; i <js_scores.length(); i++) {
                int score = js_scores.getJSONArray(i).getInt(0);
                String pseudo = js_scores.getJSONArray(i).getString(1);
                Double longitude = js_scores.getJSONArray(i).isNull(2) ? null : js_scores.getJSONArray(i).getDouble(2);
                Double latitude = js_scores.getJSONArray(i).isNull(3) ? null : js_scores.getJSONArray(i).getDouble(3);
                scores.add(new Score(score, pseudo, longitude, latitude));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        final ListView myList = (ListView)findViewById(R.id.list);
        myList.setAdapter(new MyArrayAdapter(this, scores));

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Score s = (Score) myList.getItemAtPosition(position);

                if (s.getLatitude() != null && s.getLongitude() != null) {
                    Intent intent = new Intent(ScoresListActivity.this, MapsActivity.class);
                    intent.putExtra("score", s.getScore());
                    intent.putExtra("pseudo", s.getPseudo());
                    intent.putExtra("longitude", s.getLongitude());
                    intent.putExtra("latitude", s.getLatitude());
                    //based on item add info to intent
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Désolé il n'y a pas de location pour ce score :(", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                restartGame();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restartGame() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("action", MainActivity.REPLAY);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        restartGame();
        finish();
    }
}
