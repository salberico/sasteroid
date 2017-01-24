package com.salberico.asteroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.audio.Sound;

/**
 * Created by Sam Alberico on 6/26/2016.
 */

//Class for holding explosion animation, diameter and x and y coordinates
public class Explosion {
    //STATIC
    public static Sound explodeSound;
    public static Animation animation;

    //DYNAMIC
    public float explosionCount = 0;
    public float x, y, diameter, angle;

    //Instance initialization
    public Explosion(float xstart, float ystart, float dia, float rotation){
        this.x = xstart;
        this.y = ystart;
        this.diameter = dia;
        this.angle = rotation;
    }

    //Static initialization for generating animation from sprite sheet
    public static void init(){
        int columns = 9;
        int rows = 9;
        explodeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/boom.mp3"));
        Texture sheet = new Texture("images/explosion.png");
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()/columns, sheet.getHeight()/rows); //create 2d array from sprite sheet
        TextureRegion[] frames = new TextureRegion[columns * rows - 1]; // last part of this sprite is a repeat therefore it is taken off
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (columns * i + j < columns * rows - 1){
                    frames[i * columns + j] = tmp[i][j]; //convert 2d array into a 1d array
                }
            }
        }
        //add frames to static animation
        animation = new Animation(0.03f, frames);
    }
}
