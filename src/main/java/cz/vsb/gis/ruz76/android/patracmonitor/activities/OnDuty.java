package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cz.vsb.gis.ruz76.android.patracmonitor.R;

public class OnDuty extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_duty);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String status = sharedPrefs.getString("status", "onduty");
        String searchName = sharedPrefs.getString("searchName", "Bez popisu");
        String searchDesc = sharedPrefs.getString("searchDesc", "Bez popisu");

        /*String searchId = sharedPrefs.getString("searchId", "Bez popisu");
        Log.i("ONDUTY", "YYYYY" + searchId);
        */

        TextView textViewStatus = findViewById(R.id.textViewStatus);
        textViewStatus.setText(status);

        TextView textViewSearchName = findViewById(R.id.textViewSearchName);
        textViewSearchName.setText(searchName);

        TextView textViewSearchDesc = findViewById(R.id.textViewSearchDesc);
        textViewSearchDesc.setText(searchDesc);

        Button buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(OnDuty.this, Map.class);
                startActivity(map);
            }
        });

        Button buttonMessages = findViewById(R.id.buttonMessages);
        buttonMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messages = new Intent(OnDuty.this, Messages.class);
                startActivity(messages);
            }
        });

    }
}
