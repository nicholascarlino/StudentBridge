package edu.tufts.cs.studentbridge;

/**
 * Created by George on 12/2/2016.
 */

//A class to contain what a post should have in it
public class PostItem {
    private String text;
    private String user;
    private String time;

    //Constructor to set values
    public PostItem(String text, String user, String time) {
        super();
        this.text = text;
        this.user = user;
        this.time = time;
    }

    //Set functions

    public void set_text(String text){
        this.text = text;
    }

    public void set_user(String user){
        this.user = user;
    }

    public void set_time(String time){
        this.time = time;
    }

    //Get functions

    public String get_text(){
        return this.text;
    }

    public String get_user(){
        return this.user;
    }

    public String get_time(){
        return this.time;
    }
}
