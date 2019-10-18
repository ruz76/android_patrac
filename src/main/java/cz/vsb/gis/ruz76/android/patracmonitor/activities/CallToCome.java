package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cz.vsb.gis.ruz76.android.patracmonitor.R;

public class CallToCome extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_to_come);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String searchName = sharedPrefs.getString("searchName", "Bez názvu");
        String searchDesc = sharedPrefs.getString("searchDesc", "Bez popisu");

        TextView textViewSearchName = findViewById(R.id.textViewSearchName);
        textViewSearchName.setText(searchName);

        TextView textViewSearchDesc = findViewById(R.id.textViewSearchDesc);
        textViewSearchDesc.setText(searchDesc);

        Button buttonOnTheWay = findViewById(R.id.buttonOnTheWay);
        buttonOnTheWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(CallToCome.this, R.string.thank_you_on_the_way, Toast.LENGTH_LONG);
                toast.show();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("status", "onduty");
                editor.commit();
                finish();
            }
        });

    }
}
