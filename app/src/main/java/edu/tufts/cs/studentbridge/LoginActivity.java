package edu.tufts.cs.studentbridge;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    public final static String USER_NAME = "edu.tufts.cs.studentbridge.USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get reference to database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get reference to the "Users" part of database
        final DatabaseReference myRef = database.getReference("studentbridge-ba599").child("Users");

        //Get the edittexts for the username and password
        final EditText username = (EditText) findViewById(R.id.editText1);
        final EditText pass = (EditText) findViewById(R.id.editText2);

        //The button to try to login
        Button login = (Button) findViewById(R.id.login);
        assert login != null;
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Query the database to see if the credentials given are correct
                Query query = myRef.orderByKey();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //If the username is stored in the database, check the password
                        if (dataSnapshot.child(username.getText().toString()).exists()){
                            if (pass_authent(dataSnapshot, username, pass)){
                                //If successful, move to next activity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra(USER_NAME, ((TextView) username).getText().toString());
                                startActivity(intent);
                            }
                            else{
                                //If unsuccessful, tell them so
                                Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_LONG).show();
                                pass.setText("");
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG).show();
                            username.setText("");
                            pass.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TAG:", "Failed to read value.", databaseError.toException());
                    }
                });
            }
        });

        //Create alert dialog for a new user
        AlertDialog.Builder newusername = new AlertDialog.Builder(LoginActivity.this);
        final EditText new_user = new EditText(getApplicationContext());
        new_user.setTextColor(Color.BLACK);
        newusername.setView(new_user);
        newusername.setTitle("Username");
        newusername.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                //Check to make sure that the username does not already exist within the database
                Query query = myRef.orderByKey();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child(new_user.getText().toString()).exists()) {
                            //If the username is not already taken, make a new dialog to get the new password
                            AlertDialog.Builder password = new AlertDialog.Builder(LoginActivity.this);
                            final EditText new_pass = new EditText(getApplicationContext());
                            new_pass.setTextColor(Color.BLACK);
                            //Get current time
                            new_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            password.setView(new_pass);
                            password.setTitle("Password");
                            password.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Hash and salt the password, then store the username and password in the database
                                    String passHash = securePass(new_pass.getText().toString(), "ERROR");
                                    myRef.child(new_user.getText().toString()).setValue(passHash);
                                    dialogInterface.cancel();
                                    //Move into next activity, passing the username
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra(USER_NAME, ((TextView) new_user).getText().toString());
                                    startActivity(intent);
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
                        } else {
                            //If username already exists, tell the user
                            Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_LONG).show();
                            new_user.setText("");
                            dialogInterface.cancel();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        newusername.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog user_alert = newusername.create();

        //Button to make new profile and connect above dialog to it
        Button create = (Button) findViewById(R.id.Create);
        assert create != null;
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_alert.show();
            }
        });
    }

    //Checks to see if the given password is the correct one for the given username
    private boolean pass_authent(DataSnapshot dataSnapshot, EditText username, EditText pass){
        String user = username.getText().toString();
        //Unsalt and unhash the password
        String passHash = securePass(pass.getText().toString(), " ");
        //Check to see if it exists and is equal to the database one, if it is return true, otherwise return false
        if(dataSnapshot.child(user).exists()){
            if (dataSnapshot.child(user).getValue().toString().equals(passHash)){
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

    //////////////////////////////////////////////////////////////////////////////////////////////
    // The next three functions replicate the SHA1 password hash and salt method.  It was       //
    // found at http://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android //
    //////////////////////////////////////////////////////////////////////////////////////////////

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static String securePass(String text, String defValue){
        String returnString = defValue;
        try {
            returnString = SHA1(text);
        }
        catch (Exception e){
            Log.v("TAG:", "Password hash failure", e);
        }
        return returnString;
    }
}
