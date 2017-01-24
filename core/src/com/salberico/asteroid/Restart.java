package com.salberico.asteroid;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Sam Alberico on 6/26/2016.
 */

//Class for button simulation, for restart button specifically
public class Restart {
    //STATIC
    private static Vector2 offset = new Vector2(0,-6);
    public static Sprite sprite;

    //Static initialization (sprite/position)
    public static void init(){
        sprite = new Sprite(new Texture("images/restart.png"));
        sprite.setSize(6.5f, 6.5f);
        sprite.setPosition(mainSamAsteroid.camera.viewportWidth/2 - sprite.getWidth()/2 + offset.x,
                mainSamAsteroid.camera.viewportHeight/2 - sprite.getHeight()/2 + offset.y);
    }
    public static boolean isTouching(Vector3 point){
        if (point.x >= sprite.getX() && point.x <= sprite.getX()+sprite.getWidth() && point.y >= sprite.getY() && point.y <= sprite.getY() + sprite.getHeight()){
            return true;
        }
        else return false;
    }
}
