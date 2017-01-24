package com.salberico.asteroid;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Sam Alberico on 6/26/2016.
 */

//Class holding bindings for artificial button, specifically the pause/play dynamic button
public class PausePlay {
    //STATIC
    public static Texture pause;
    public static Texture play;
    public static Vector2 offset = new Vector2(2,2);
    public static Sprite sprite;
    public static int touchIndex = -1; //which finger is over the button

    //Static initialization for sprites and pos
    public static void init(){
        sprite = new Sprite(new Texture("images/pausesmall.png"));
        sprite.setSize(3.7f, 3.7f);
        sprite.setPosition(mainSamAsteroid.camera.viewportWidth - sprite.getWidth() - offset.x, mainSamAsteroid.camera.viewportHeight - sprite.getHeight() - offset.y);
        pause = new Texture("images/pausesmall.png");
        play = new Texture("images/playsmall.png");
    }

    //Is the given vector inside the button bounds
    public static boolean isTouching(Vector3 point){
        if (point.x >= sprite.getX() && point.x <= sprite.getX()+sprite.getWidth() && point.y >= sprite.getY() && point.y <= sprite.getY() + sprite.getHeight()){
            return true;
        }
        else return false;
    }
}
