package edu.tufts.cs.studentbridge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class GroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
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

    public void create_group(View view){
        if (view.getId() == R.id.create_group){
            Toast.makeText(this, "Group Created", Toast.LENGTH_LONG).show();
            //TODO: Add functionality
        }
    }
}
