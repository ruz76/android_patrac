package cz.vsb.gis.ruz76.patrac.android.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.vsb.gis.ruz76.patrac.android.R;
import cz.vsb.gis.ruz76.patrac.android.domain.RequestMode;

public class CallOnDutyActivity extends Activity {

    // array of active searches
    private String[] searches;
    // list of active searches
    private List<String> searchesList;
    // array adapter for list of searches
    private ArrayAdapter<String> arrayAdapter;
    // list view with searches
    private ListView listViewSearches;
    // user has selected the search
    private boolean searchWasSelected = false;


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Adds back button handling.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_on_duty);
        Bundle bundle = getIntent().getExtras();
        searches = bundle.getStringArray("searches");
        searchesList = new ArrayList<>();
        for (int i = 0; i < searches.length; i++) {
            if (searches[i].split(";").length > 1) {
                searchesList.add(searches[i].split(";")[1]);
            }
        }
        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, searchesList);
        listViewSearches = (ListView) findViewById(R.id.listViewSearches);

        // DataBind ListView with items from ArrayAdapter
        listViewSearches.setAdapter(arrayAdapter);

        listViewSearches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.searchid = searches[position].split(";")[0];
                searchWasSelected = true;
            }
        });

        Button buttonArrive = findViewById(R.id.buttonArrive);
        buttonArrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // it there are not any searches in the list we
                if (searches.length < 1) {
                    Toast toast = Toast.makeText(CallOnDutyActivity.this, R.string.error_unexpected, Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
                // if the search was not previously selected we select first
                if (!searchWasSelected) {
                    MainActivity.searchid = searches[0].split(";")[0];
                }
                MainActivity.mode = RequestMode.SELECTED;
                MainActivity.arrive = getSelectedTime();
                Toast toast = Toast.makeText(CallOnDutyActivity.this, R.string.call_on_duty_selected, Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        });

        Button buttonNotArrive = findViewById(R.id.buttonNotArrive);
        buttonNotArrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mode = RequestMode.SELECTED;
                MainActivity.arrive = "NKD";
                Toast toast = Toast.makeText(CallOnDutyActivity.this, R.string.call_on_duty_not_selected, Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        });

        TextView textViewDutyDescription = (TextView) findViewById(R.id.textViewDutyDescription);
        textViewDutyDescription.setText(getString(R.string.activity_call_on_duty_description));

        setupActionBar();
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
