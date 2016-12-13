package edu.tufts.cs.studentbridge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GroupActivity extends AppCompatActivity {

    private String thread;
    private String post1;
    public final static String THREAD_NAME = "edu.tufts.cs.studentbridge.THREAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        //Get group name and user from previous intent
        Intent intent = getIntent();
        final String group = intent.getStringExtra(MainActivity.GROUP_NAME);
        final String USER = intent.getStringExtra(LoginActivity.USER_NAME);

        setTitle("Threads in " + group);

        //Gets ListView that will show the list of threads in the current group and connects an adapter
        final ListView listView = (ListView) findViewById(R.id.list_view);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);
        assert listView != null;
        listView.setAdapter(adapter);

        //Get reference to database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get reference from the child associated with the group name
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child("Groups").child(group);
        //Add event listener so that adapter (ergo the ListView) will be filled with values
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

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG:", "Failed to read value.", databaseError.toException());
            }
        });

        //Button to create threads
        final Button button = (Button) findViewById(R.id.create_thread);

        //Creates alert dialogs to get name of new thread and the first post of the thread
        final AlertDialog.Builder thread_name = new AlertDialog.Builder(this);
        final EditText thread_alert = new EditText(this);
        thread_name.setView(thread_alert);
        thread_name.setTitle("Thread Name");
        thread_name.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get thread name
                thread = thread_alert.getText().toString();
                //Create post dialog to get value of first post
                AlertDialog.Builder post = new AlertDialog.Builder(thread_name.getContext());
                final EditText post_alert = new EditText(thread_name.getContext());
                post.setView(post_alert);
                post.setTitle("Post Contents");
                post.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Get current time and text value of post
                        String id = Long.toString(System.currentTimeMillis());
                        post1 = post_alert.getText().toString();
                        //Add to database
                        myRef.child(thread).child(id).child("Time").setValue(Long.toString(System.currentTimeMillis()));
                        myRef.child(thread).child(id).child("Text").setValue(post1);
                        myRef.child(thread).child(id).child("User").setValue(USER);
                        post_alert.setText("");
                        dialog.cancel();
                    }
                });
                final AlertDialog post_create = post.create();
                post_create.show();
                thread_alert.setText("");
                dialog.cancel();
            }
        });
        final AlertDialog thread_create = thread_name.create();

        //Connect button to dialogs
        assert button != null;
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                thread_create.show();
            }
        });

        //Connect each item in the ListView to go to the post activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(GroupActivity.this, PostActivity.class);
                //Put name of thread, group name, and user into intent
                intent.putExtra(THREAD_NAME, ((TextView) view).getText());
                intent.putExtra(MainActivity.GROUP_NAME, group);
                intent.putExtra(LoginActivity.USER_NAME, USER);
                startActivity(intent);
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate();
        //TODO: Add functionality
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();
                //TODO: Add functionality
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/
}
