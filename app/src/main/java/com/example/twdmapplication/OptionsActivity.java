package com.example.twdmapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener {

    CardView firstCard, secondCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        // defining the cards
        firstCard = findViewById(R.id.firstCard);
        secondCard = findViewById(R.id.secondCard);

        // set the listeners for the cards
        firstCard.setOnClickListener(this);
        secondCard.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.firstCard:    //wifi
                intent = new Intent(this, WifiActivity.class);
                startActivity(intent);
                break;
            case R.id.secondCard:   //image processing
                intent = new Intent(this, ImageProcessingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
