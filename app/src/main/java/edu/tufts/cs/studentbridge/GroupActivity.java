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

        Intent intent = getIntent();
        final String group = intent.getStringExtra(MainActivity.GROUP_NAME);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);
        assert listView != null;
        listView.setAdapter(adapter);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child(group);
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

        final Button button = (Button) findViewById(R.id.create_thread);

        final AlertDialog.Builder thread_name = new AlertDialog.Builder(this);
        final EditText thread_alert = new EditText(this);
        thread_name.setView(thread_alert);
        thread_name.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread = thread_alert.getText().toString();

                AlertDialog.Builder post = new AlertDialog.Builder(thread_name.getContext());
                final EditText post_alert = new EditText(thread_name.getContext());
                post.setView(post_alert);
                post.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = DateFormat.getDateTimeInstance().format(new Date());
                        post1 = post_alert.getText().toString();
                        myRef.child(thread).child(id).child("Time").setValue(Calendar.getInstance().toString());
                        myRef.child(thread).child(id).child("Text").setValue(post1);
                        myRef.child(thread).child(id).child("User").setValue("USER_NAME");
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

        assert button != null;
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                thread_create.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(GroupActivity.this, PostActivity.class);
                intent.putExtra(THREAD_NAME, ((TextView) view).getText());
                intent.putExtra(MainActivity.GROUP_NAME, group);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
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
    }
}
