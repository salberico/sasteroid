package com.salberico.asteroid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Sam Alberico on 6/26/2016.
 */
// Purely static class for Character health/armor bar. Holds sprite and calculates width of new bar
public class ArmorBar {

    //DYNAMIC
    public float x, y, width, height, current, max;
    public Sprite baseSprite;
    public Sprite currentSprite;

    // start x and y are middle of base sprite
    public ArmorBar(float startX, float startY, float startWidth, float startHeight, float startMax){

        // Initial position
        this.x = startX;
        this.y = startY;

        // Initial size
        this.width = startWidth;
        this.height = startHeight;

        //Initial values
        this.max = startMax;
        this.current = startMax;

        //Base and Current sprite, representing max health and current health respectively
        this.baseSprite = new Sprite(new Texture("images/basebar.png"));
        this.currentSprite = new Sprite(new Texture("images/currentbar.png"));
        this.baseSprite.setSize(this.width, this.height);
        this.baseSprite.setPosition(startX - this.width/2, startY - this.height/2);
        this.currentSprite.setSize(this.width, this.height);
        this.currentSprite.setPosition(startX - this.width/2, startY - this.height/2);
    }

    // Updates current bar size based on current armor. Called from main
    public void updateBar(float currentArmor, float currentX, float currentY){

        //current represents current armor
        current = currentArmor;
        if (current >= 0)
        {
            currentSprite.setSize((current/max)*baseSprite.getWidth(),baseSprite.getHeight());
        }
        else {
            currentSprite.setSize(0,baseSprite.getHeight());
        }

        //update stored positions
        x = currentX;
        y = currentY;
        currentSprite.setPosition(x,y);
        baseSprite.setPosition(x,y);
    }
}
