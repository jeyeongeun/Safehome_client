package org.khucd.cdapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText nameText;
    private EditText phoneText;
    private Button loginBtn;
    private String current_user_for;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameText = (EditText)findViewById(R.id.nameText);
        phoneText = (EditText)findViewById(R.id.phoneText);

        loginBtn = (Button) findViewById(R.id.loginOKBtn);

    }
    public void setUser(String _user){
        current_user_for = _user;
        Log.d("현재 사용자:", current_user_for);
    }
    public String getUser(){
        return current_user_for;
    }
    public void onLoginOKbtnClicked(View v) {
        Intent resultIntent = new Intent();
        String name = nameText.getText().toString();
        String phone = phoneText.getText().toString();
        setUser(phone);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("phone", phone);
        resultIntent.putExtra("user_number", phone);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
