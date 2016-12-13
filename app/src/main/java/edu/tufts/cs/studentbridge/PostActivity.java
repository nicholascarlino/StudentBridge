package edu.tufts.cs.studentbridge;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private String post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Get extra values from previous intent
        Intent intent = getIntent();
        final String thread = intent.getStringExtra(GroupActivity.THREAD_NAME);
        final String group = intent.getStringExtra(MainActivity.GROUP_NAME);
        final String USER = intent.getStringExtra(LoginActivity.USER_NAME);

        setTitle("Posts in " + thread);

        //Set up ListView and connect custom adapter so as to show all info from a post
        final ListView listView = (ListView) findViewById(R.id.list_view);
        final PostAdapter adapter = new PostAdapter(this, new ArrayList<PostItem>());
        assert listView != null;
        listView.setAdapter(adapter);

        //Get reference to database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get reference to correct thread child
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child("Groups").child(group).child(thread);
        //Set event listener to update adapter properly
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Add an item to the adapter when the database gets a new value
                String text = dataSnapshot.child("Text").getValue().toString();
                String user = dataSnapshot.child("User").getValue().toString();
                String time = getFormattedDateFromTimestamp(Long.parseLong(dataSnapshot.child("Time").getValue().toString()));
                PostItem new_post = new PostItem(text, user, time);
                adapter.add(new_post);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Remove a value from the adapter when the database loses a value
                String text = dataSnapshot.child("Text").getValue().toString();
                String user = dataSnapshot.child("User").getValue().toString();
                String time = getFormattedDateFromTimestamp(Long.parseLong(dataSnapshot.child("Time").getValue().toString()));
                //Iterate through the children of the adapter to get the actual object we want to delete
                for (int i = 0; i < adapter.getCount(); i++){
                    PostItem post = adapter.getItem(i);
                    //Check all three values (the time will be unique)
                    if (post.get_user().equals(user) && post.get_text().equals(text) && post.get_time().equals(time)){
                        adapter.remove(post);
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG:", "Failed to read value.", databaseError.toException());
            }
        });

        //Get button to create posts
        final Button button = (Button) findViewById(R.id.create_post);

        //Create dialog to make new posts
        final AlertDialog.Builder post_name = new AlertDialog.Builder(this);
        final EditText post_alert = new EditText(this);
        post_name.setView(post_alert);
        post_name.setTitle("Post Contents");
        post_name.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id = Long.toString(System.currentTimeMillis());
                post = post_alert.getText().toString();
                //Store all values into a HashMap so that no race conditions will occur in the OnChildAdded function
                Map newPostData = new HashMap();
                newPostData.put("Text", post);
                newPostData.put("Time", Long.toString(System.currentTimeMillis()));
                newPostData.put("User", USER);
                //Add map to database
                myRef.child(id).updateChildren(newPostData);
                post_alert.setText("");
                dialog.cancel();
            }
        });
        final AlertDialog post_create = post_name.create();

        assert button != null;
        //Connect button to dialog
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                post_create.show();
            }
        });

        //http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android source
        //Connects a dialog to each item in the ListView so that it can be deleted if necessary
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id){
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final PostItem post = (PostItem)parent.getItemAtPosition(position);
                        //Make it so that a user cannot delete admin posts
                        if (post.get_user().equals("Admin")){
                            Toast.makeText(getApplicationContext(), "Cannot delete an Admin post.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            final String item = post.get_text();
                            //Query the database for correct post and delete it if the user presses Yes
                            //otherwise do nothing
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    Query query = myRef.orderByKey();
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            //Don't allow the last post to be deleted because it messes
                                            //up the database and activities
                                            if (!(dataSnapshot.getChildrenCount() == 1)) {
                                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                    if (child.child("Text").getValue().toString().equals(item)) {
                                                        child.getRef().removeValue();
                                                        break;
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Cannot delete last post.", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.w("TAG:", "Failed to read value.", databaseError.toException());
                                        }
                                    });
                                    break;
                                //Do nothing if no button is clicked
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    }
                };

                //Create alert dialog that asks if the user wants to delete a post
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setMessage("Do you want to delete this post?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    //http://stackoverflow.com/questions/13667966/how-to-get-date-from-milliseconds-in-android
    //Get a regular date from a time that is in the format of milliseconds
    private static String getFormattedDateFromTimestamp(long timestampInMilliSeconds)
    {
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(timestampInMilliSeconds);  //here your time in miliseconds
        SimpleDateFormat date = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
        return date.format(cl.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            //If it is the options menu, ask if the user wants to logout
            case R.id.options:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //If user says they want to logout, clear the shared preferences and
                                //close all other activities and go back to login activity
                                SharedPreferences sharedpreferences = getSharedPreferences(LoginActivity.PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.clear();
                                editor.commit();

                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder logout = new AlertDialog.Builder(PostActivity.this);
                logout.setMessage("Do you want to logout?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
