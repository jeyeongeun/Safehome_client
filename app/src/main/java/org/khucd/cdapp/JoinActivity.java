package org.khucd.cdapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class JoinActivity extends AppCompatActivity {

    private EditText nameText;
    private EditText phoneText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        nameText = (EditText) findViewById(R.id.nameText);
        phoneText = (EditText) findViewById(R.id.phoneText);
    }

    public void onJoinBtnClicked(View v) {
        Intent resultIntent = new Intent();
        String name = nameText.getText().toString();
        String phone = phoneText.getText().toString();
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("phone", phone);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
