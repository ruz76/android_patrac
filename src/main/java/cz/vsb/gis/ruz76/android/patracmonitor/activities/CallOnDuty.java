package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import cz.vsb.gis.ruz76.android.patracmonitor.R;

public class CallOnDuty extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_on_duty);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String search = sharedPrefs.getString("searchName", "Bez popisu");

        TextView textViewSearch = findViewById(R.id.textViewSearch);
        textViewSearch.setText(search);

        Button buttonArrive = findViewById(R.id.buttonArrive);
        buttonArrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(CallOnDuty.this, R.string.thank_you_to_come, Toast.LENGTH_LONG);
                toast.show();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("status", "readytogo");
                editor.putString("arriveat", getSelectedTime());
                editor.putInt("arriveatchanged", 1);
                editor.commit();
                finish();
            }
        });

        Button buttonNotArrive = findViewById(R.id.buttonNotArrive);
        buttonNotArrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(CallOnDuty.this, R.string.thank_you__not_to_come, Toast.LENGTH_LONG);
                toast.show();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("status", "cannotarrive");
                editor.putString("arriveat", "NKD");
                editor.putInt("arriveatchanged", 1);
                editor.commit();
                finish();
            }
        });

    }

    private String getSelectedTime() {
        RadioButton rb = (RadioButton) findViewById(R.id.radioButton60);
        if (rb.isChecked()) {
            return "60m";
        }
        rb = (RadioButton) findViewById(R.id.radioButton120);
        if (rb.isChecked()) {
            return "120m";
        }
        rb = (RadioButton) findViewById(R.id.radioButton180);
        if (rb.isChecked()) {
            return "180m";
        }
        rb = (RadioButton) findViewById(R.id.radioButton240);
        if (rb.isChecked()) {
            return "240m";
        }
        rb = (RadioButton) findViewById(R.id.radioButton300);
        if (rb.isChecked()) {
            return "300m";
        }
        rb = (RadioButton) findViewById(R.id.radioButtonGt300);
        if (rb.isChecked()) {
            return "v300m";
        }
        return "60m";
    }
}
