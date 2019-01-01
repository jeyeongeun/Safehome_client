package org.khucd.cdapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    String[] spinnerItems = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};

    private Spinner startSpin;
    private Spinner endSpin;
    private CheckBox movingCheck;
    private CheckBox collisionDetectCheck;
    private Button btnOK;
    private TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        int start = intent.getExtras().getInt("startTime");
        int end = intent.getExtras().getInt("endTime");
        boolean mov = intent.getExtras().getBoolean("moving");
        boolean coll = intent.getExtras().getBoolean("collision");

        startSpin = (Spinner) findViewById(R.id.StartTimeSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startSpin.setAdapter(adapter);

        endSpin = (Spinner) findViewById(R.id.EndTimeSpinner);
        endSpin.setAdapter(adapter);

        btnOK = (Button) findViewById(R.id.btnOK);

        movingCheck = (CheckBox) findViewById(R.id.movingCheck);
        collisionDetectCheck = (CheckBox) findViewById(R.id.collisionDetectCheck);

        txt = (TextView) findViewById(R.id.timeSet);

        txt.setText(start + "시 ~ " + end + "시");
        movingCheck.setChecked(mov);
        collisionDetectCheck.setChecked(coll);
    }

    public void onOKbtnClicked(View v) {
        String time = startSpin.getSelectedItem().toString() + "시 ~ " + endSpin.getSelectedItem().toString() + "시";
        txt.setText(time);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("startTime", Integer.valueOf(startSpin.getSelectedItem().toString()));
            resultIntent.putExtra("endTime", Integer.valueOf(endSpin.getSelectedItem().toString()));
            resultIntent.putExtra("movingCheck", movingCheck.isChecked());
            resultIntent.putExtra("collisionDetectCheck", collisionDetectCheck.isChecked());
            setResult(RESULT_OK, resultIntent);
        }
        return super.onKeyDown(keyCode, event);
    }

}
