package com.salberico.asteroid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Sam Alberico on 26/04/2016.
 */

//Shoot class, for right finger (shooting finger), right joystick bindings
public class Shoot {
    public static int touchIndex = -1; //finger touching
    public static Vector3 fingerLoc = new Vector3(0,0,0); //joystick nob
    public static Vector3 baseLoc = new Vector3(0,0,0); //joystick base
    public static Sprite fingerSprite = new Sprite(new Texture("images/upstick.png"));
    public static Sprite baseSprite = new Sprite(new Texture("images/basestick.png"));
    public static float angle = 0; //angle between joystick nob and base
    private static float forceRange = 6; //max range of the joystick nob

    //Static initialization
    public static void init(){
        fingerSprite.setSize(3f,3f);
        baseSprite.setSize(5.5f,5.5f);
    }

    //update angle in radians from joystick nob to base
    public static void updateAngle() {
        angle = (float)(Math.atan2(fingerLoc.y - baseLoc.y, fingerLoc.x - baseLoc.x)); //radians
    }

    //get the 'speedScale' which is only used to find the scale needed for force range
    public static float getSpeedscale(){
        if (Math.sqrt(Math.pow((fingerLoc.y - baseLoc.y),2) + Math.pow((fingerLoc.x - baseLoc.x),2)) >= forceRange){
            return 1;
        }
        else {
            return (float)(Math.sqrt(Math.pow((fingerLoc.y - baseLoc.y),2) + Math.pow((fingerLoc.x - baseLoc.x),2))/forceRange);
        }
    }

    //get the visual X value which is based on the scaled data, rather than the raw
    public static float getVisualX(){
        if (getSpeedscale() >= 1) {
            return forceRange * (float)Math.cos(angle) + baseSprite.getX() + baseSprite.getWidth()/2 - fingerSprite.getWidth()/2;
        }
        else return fingerSprite.getX();
    }

    //get the visual Y value which is based on the scaled data, rather than the raw
    public static float getVisualY(){
        if (getSpeedscale() >= 1) {
            return forceRange * (float)Math.sin(angle) + baseSprite.getY() + baseSprite.getHeight()/2 - fingerSprite.getHeight()/2;
        }
        else return fingerSprite.getY();
    }
}
