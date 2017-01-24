package com.salberico.asteroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sam Alberico on 6/27/2016.
 */

//Class holding bindings to receive store and process highscores
public class Highscore {
    //STATIC
    private static Preferences prefs;
    public static int scoreCount = 3;
    public static List<Integer> score;

    //Static initialization
    public static void init(){
        score = new ArrayList<Integer>(); //init score list
        prefs = Gdx.app.getPreferences("My Preferences"); //get preferences
        for (int i = 0; i < scoreCount; i++){
            score.add(prefs.getInteger(Integer.toString(i))); //read data from preference file
        }
    }

    //Highscore processing, called every GAMEOVER
    public static void addScore(int s){
        score.add(s); //add score to the end
        Collections.sort(score); //sort list now with new score
        Collections.reverse(score); //reverse so highest is at the start
        score.remove(score.size()-1); //remove the lowest score
        for (int i = 0; i < score.size(); i++){
            prefs.putInteger(Integer.toString(i), score.get(i)); //write updates scores to preferences file
        }
        prefs.flush(); //save
    }

    //Return formatted and converted score (leading zeroes)
    static String giveScore(int index){
        String temp =  Integer.toString(score.get(index));
        String r = temp;
        for (int i = 0; i < Character.scoreSize - temp.length(); i++){
            r = "0" + r;
        }
        return r;
    }
}

