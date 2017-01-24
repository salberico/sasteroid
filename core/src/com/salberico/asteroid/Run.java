package com.salberico.asteroid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Sam Alberico on 26/04/2016.
 */

//Class in which finger data for the left finger (run/fly) is held, left joystick data
public class Run {
    //STATIC
    public static int touchIndex = -1; //current finger for base
    public static float maxForce = 60; // max force for linear impulse
    public static float forceRange = 6; // range in which this force is reached
    public static Vector3 fingerLoc = new Vector3(0, 0, 0); //joystick nob
    public static Vector3 baseLoc = new Vector3(0, 0, 0); //joystick base
    public static Sprite fingerSprite = new Sprite(new Texture("images/upstick.png"));
    public static Sprite baseSprite = new Sprite(new Texture("images/basestick.png"));
    public static float angle = 0; //angle between joystick nob and base

    //Static initialization
    public static void init() {
        fingerSprite.setSize(3f, 3f);
        baseSprite.setSize(5.5f, 5.5f);
    }

    //calculate and store the updated angle, called from updateLogic()
    public static void updateAngle() {
        angle = (float)(180f/Math.PI)*(float)(Math.atan2(fingerLoc.y - baseLoc.y, fingerLoc.x - baseLoc.x));
    }

    //get the scale of current speed based on finger location (0,1)
    public static float getSpeedscale(){
        if (Math.sqrt(Math.pow((fingerLoc.y - baseLoc.y),2) + Math.pow((fingerLoc.x - baseLoc.x),2)) >= forceRange){
            return 1; //return 1 if finger loc is passed the max force range, giving max force
        }
        else { // otherwise return the scaled value between [0,1)
            return (float)(Math.sqrt(Math.pow((fingerLoc.y - baseLoc.y),2) + Math.pow((fingerLoc.x - baseLoc.x),2))/forceRange);
        }
    }

    //get the visual X value which is based on the scaled data, rather than the raw
    public static float getVisualX(){
        if (getSpeedscale() >= 1) {
            return forceRange * (float)Math.cos(Math.toRadians(angle)) + baseSprite.getX() + baseSprite.getWidth()/2 - fingerSprite.getWidth()/2;
        }
        else return fingerSprite.getX();
    }

    //get the visual Y value which is based on the scaled data, rather than the raw
    public static float getVisualY(){
        if (getSpeedscale() >= 1) {
            return forceRange * (float)Math.sin(Math.toRadians(angle)) + baseSprite.getY() + baseSprite.getHeight()/2 - fingerSprite.getHeight()/2;
        }
        else return fingerSprite.getY();
    }
}
