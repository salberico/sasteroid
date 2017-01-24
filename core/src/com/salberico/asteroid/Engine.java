package com.salberico.asteroid;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
/**
 * Created by Sam Alberico on 20/05/2016.
 */

//class for holding engine (fire location and size) data which can be 'tied' to a given sprite
public class Engine {
    private Vector2 baseLoc; //location of base of fire
    public Vector2 relativeLoc; //relative location to base
    public Vector2 size;
    private float scale;

    //Instance initialization
    public Engine(float relativex, float relativey, float width, float height){
        this.baseLoc = new Vector2(relativex, relativey);
        this.relativeLoc = new Vector2(relativex, relativey);
        this.size = new Vector2(width, 0);
        this.scale = height;
    }

    //get x based on rotation of given sprite
    public float getX(float angle, Sprite sprite){
        return this.relativeLoc.len()*(float)Math.cos(Math.toRadians(angle + 180f - (float)((180/Math.PI)*Math.atan(this.relativeLoc.y / this.relativeLoc.x)))) - this.size.x/2 + sprite.getX() + sprite.getWidth()/2;
    }

    //get y based on rotation of given sprite
    public float getY(float angle, Sprite sprite){
        return this.relativeLoc.len()*(float)Math.sin(Math.toRadians(angle + 180f - (float)((180/Math.PI)*Math.atan(this.relativeLoc.y / this.relativeLoc.x)))) - this.size.y/2 + sprite.getY() + sprite.getHeight()/2;
    }

    //update size of sprite off scale, only used for characters 'engines' which are dynamic in size
    public void updateSize(float scale){
        this.size.y = scale * this.scale;
        this.relativeLoc.x = this.baseLoc.x - this.size.y/2 + this.size.y/6;
    }
}
