package edu.tufts.cs.studentbridge;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child("Users");

        final EditText username = (EditText) findViewById(R.id.editText1);
        final EditText pass = (EditText) findViewById(R.id.editText2);

        Button login = (Button) findViewById(R.id.login);
        assert login != null;
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query = myRef.orderByKey();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(username.getText().toString()).exists()){
                            if (pass_authent(dataSnapshot, username, pass)){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TAG:", "Failed to read value.", databaseError.toException());
                    }
                });
            }
        });
        AlertDialog.Builder newusername = new AlertDialog.Builder(LoginActivity.this);
        final EditText new_user = new EditText(getApplicationContext());
        new_user.setTextColor(Color.BLACK);
        newusername.setView(new_user);
        newusername.setTitle("Username");
        newusername.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder password = new AlertDialog.Builder(LoginActivity.this);
                final EditText new_pass = new EditText(getApplicationContext());
                new_pass.setTextColor(Color.BLACK);
                password.setView(new_pass);
                password.setTitle("Password");
                password.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef.child(new_user.getText().toString()).setValue(new_pass.getText().toString());
                        dialogInterface.cancel();
                    }
                });
                password.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                final AlertDialog pass_alert = password.create();
                pass_alert.show();
                dialogInterface.cancel();
            }
        });
        newusername.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog user_alert = newusername.create();

        Button create = (Button) findViewById(R.id.Create);
        assert create != null;
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_alert.show();
            }
        });
    }
    private boolean pass_authent(DataSnapshot dataSnapshot, EditText username, EditText pass){
        String user = username.getText().toString();
        String password = pass.getText().toString();
        if(dataSnapshot.child(user).exists()){
            if (dataSnapshot.child(user).getValue().toString().equals(password)){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}
