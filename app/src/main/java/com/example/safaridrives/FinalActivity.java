package com.example.safaridrives;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class FinalActivity extends AppCompatActivity {
    private Intent getIntent;
    private TextView rental;
    private Button dashboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        rental = findViewById(R.id.textViewRentalId);
        getIntent = getIntent();
        dashboard = findViewById(R.id.btnDashboard);

        //this is to gather the information sent from one activity to another instead of querying again. its faster than querying
        Button displayButton = findViewById(R.id.displayButton);
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent != null && getIntent.hasExtra("car_data")) {
                    HashMap<String, Object> receivedData = (HashMap<String, Object>) getIntent.getSerializableExtra("car_data");
                    if (receivedData.containsKey("rentalId")) {
                        String rId = receivedData.get("rentalId").toString();
                        rental.setText(rId);
                    }
                }
            }
        });

        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FinalActivity.this, InformationActivity.class));
            }
        });

    }

}
