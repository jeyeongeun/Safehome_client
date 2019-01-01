package org.khucd.cdapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    EditText phoneNumber;
    ListView friendsList;
    CheckBox checkflag;
    ArrayList<String> list;
    ArrayList<String> help;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);//

        phoneNumber = (EditText) findViewById(R.id.phoneNumber);

        friendsList = (ListView) findViewById(R.id.FriendsListView);
        friendsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        checkflag = (CheckBox) findViewById(R.id.checkHelp);

        Intent intent = getIntent();
        list = intent.getExtras().getStringArrayList("list");
        help = intent.getExtras().getStringArrayList("help");

        adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_multiple_choice, list);

        friendsList.setAdapter(adapter);

        int toFind = 0;
        Log.d("로그 친구액태비티 사이즈 : ", String.valueOf(help.size()));
        for(int i=0; i<list.size(); i++) {
            if(toFind < help.size()) {
                String tmp = help.get(toFind);
                Log.d("로그 친구액티비티", tmp);
                if (list.get(i).contains(tmp)) {
                    friendsList.performItemClick(friendsList, i, 0);
                    toFind++;
                }
            } else {
                break;
            }
        }

        friendsList.setOnItemClickListener(listener);
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    public void onAddFriendClicked(View v) {
        Intent resultIntent = new Intent();
        String phone = phoneNumber.getText().toString();
        resultIntent.putExtra("phone", phone);
        resultIntent.putExtra("statusFlag", checkflag.isChecked());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void onOKbtnClicked(View v) {
        SparseBooleanArray selectedList = friendsList.getCheckedItemPositions();
        help.clear();

        for( int i=0; i<selectedList.size(); i++) {
            String[] splitLine = null;
            if(selectedList.valueAt(i)) {
                int index = selectedList.keyAt(i);
                String text = (String) friendsList.getItemAtPosition(index);
                splitLine = text.split("\\:");
                help.add(splitLine[1]);
            }
        }
        Intent resultIntent = new Intent();
        String phone = phoneNumber.getText().toString();
        resultIntent.putExtra("phone", "");
        resultIntent.putExtra("status", help);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
