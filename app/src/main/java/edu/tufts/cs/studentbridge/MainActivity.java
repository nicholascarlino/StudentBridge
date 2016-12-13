package edu.tufts.cs.studentbridge;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String group;
    public final static String GROUP_NAME = "edu.tufts.cs.studentbridge.GROUP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Start Crashlytics
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        setTitle("Groups");

        //Get intent from login and get user
        Intent intent = getIntent();
        final String USER = intent.getStringExtra(LoginActivity.USER_NAME);

        //Sets up ListView that shows list of groups and assigns an adapter
        final ListView listView = (ListView) findViewById(R.id.listView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
               android.R.layout.simple_list_item_1, android.R.id.text1);
        assert listView != null;
        listView.setAdapter(adapter);

        //Get the reference from the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get reference from "Groups" section of database
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child("Groups");
        //Set event listeners so that adapter can be updated appropriately
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getKey();
                adapter.add(value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                adapter.remove(value);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG:", "Failed to read value.", databaseError.toException());
            }
        });

        //Button to create a group
        final Button button = (Button) findViewById(R.id.addButton);

        //Builds the UI so that the user can input a group name
        AlertDialog.Builder group_name = new AlertDialog.Builder(this);
        final EditText group_alert = new EditText(this);
        group_name.setView(group_alert);
        group_name.setTitle("Group Name");
        group_name.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get name of group and current time
                group = group_alert.getText().toString();
                String id = Long.toString(System.currentTimeMillis());
                //Creates a welcome thread
                myRef.child(group).child("Welcome").child(id).child("Time").setValue(Long.toString(System.currentTimeMillis()));
                myRef.child(group).child("Welcome").child(id).child("User").setValue("Admin");
                myRef.child(group).child("Welcome").child(id).child("Text").setValue("Welcome to group " + group);
                group_alert.setText("");
                dialog.cancel();
            }
        });
        final AlertDialog group_create = group_name.create();

        //Connects button to AlertDialog
        assert button != null;
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                group_create.show();
            }
        });

        //Connects an activity change to each group in the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                //Connect the name of the clicked group and the current username to the new intent
                intent.putExtra(GROUP_NAME, ((TextView) view).getText());
                intent.putExtra(LoginActivity.USER_NAME, USER);
                startActivity(intent);
            }
        });

    }

    //Hides keyboard when anything but EditText is clicked including the button
    // got from http://stackoverflow.com/questions/4005728/hide-default-keyboard-on-click-in-android/7241790#7241790
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            assert w != null;
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                //Could not fix with assert -> try did not fix lint error so suppressed
                try {
                    //noinspection ConstantConditions
                    imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                }
                catch (NullPointerException e){
                    Log.v("Exception: ", e.toString());
                }
            }
        }
        return ret;
    }
}
