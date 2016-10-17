package edu.tufts.cs.studentbridge;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.sql.Types.NULL;

public class PostActivity extends AppCompatActivity {

    public String post;
    public final static String POST_NAME = "edu.tufts.cs.studentbridge.POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent intent = getIntent();
        final String thread = intent.getStringExtra(GroupActivity.THREAD_NAME);
        final String group = intent.getStringExtra(MainActivity.GROUP_NAME);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);
        listView.setAdapter(adapter);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child(group).child(thread);
        final AlertDialog.Builder post_name = new AlertDialog.Builder(this);
        final EditText post_alert = new EditText(this);
        post_name.setView(post_alert);
        post_name.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                post = post_alert.getText().toString();
                DatabaseReference new_post = myRef.push();
                new_post.child("Time").setValue(Calendar.getInstance().toString());
                new_post.child("Text").setValue(post);
                new_post.child("User").setValue("USER_NAME");
            }
        });
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //TODO: FIGURE OUT HOW TO GET THIS TO NOT THROW AN ERROR
                String value = dataSnapshot.child("Text").getValue().toString();
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
        final Button button = (Button) findViewById(R.id.create_post);

        final AlertDialog post_create = post_name.create();

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                post_create.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(PostActivity.this, PostActivity.class);
                intent.putExtra(POST_NAME, ((TextView) view).getText());
                startActivity(intent);
            }
        });
    }
}