package com.example.nayeer.squiro.ScoreList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.nayeer.squiro.R;
import java.util.ArrayList;


public class MyArrayAdapter extends ArrayAdapter<Score> {

    private final Context context;

    public MyArrayAdapter(Context context, ArrayList<Score> values) {
        super(context, R.layout.cell_layout, values);
        this.context = context;
    }
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View cellView = convertView;
        if (cellView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cellView = inflater.inflate(R.layout.cell_layout, parent, false);
        }

        TextView pseudoTextView = (TextView)cellView.findViewById(R.id.pseudo);
        TextView scoreTextView = (TextView)cellView.findViewById(R.id.score);


        String pseudo = getItem(position).getPseudo();
        pseudoTextView.setText(pseudo);

        int score = getItem(position).getScore();
        scoreTextView.setText("Score : " + score);

        return cellView;
    }
}


