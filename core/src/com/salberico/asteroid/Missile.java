package com.salberico.asteroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import java.util.Random;

/**
 * Created by Sam Alberico on 27/04/2016.
 */

//Missile class representing missile entity
public class Missile {

    //STATIC
    public static Random rng = new Random();
    public static float width = 2f; //static width
    public static float height = 1f; //static height
    public static float reloadTime = 2f; //time before new missile is fired
    public static float turnIncrement = 0.01f; //how much missile can rotate per update
    public static float turnTime = 0.05f; //how often updates to velocity/rotation occur
    public static float damageScale = 10f; // how much damage the missile does to the character
    public static Sound explodeSound;
    public static Animation animation;

    //DYNAMIC
    public Body body; //box2d body
    public Sprite sprite = new Sprite(new Texture("images/missile.png"));
    public float timeCount = 0f;
    public Vector2 goingPoint;
    public float speed;
    public float lastTurn; //time since last turn update
    public boolean exploding = false;

    //Instance initialization
    public Missile(float x, float y, Vector2 vel){

        //box2d body definition
        BodyDef bodyDef = new BodyDef();
        this.lastTurn = 0f;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(x + width / 2, y + height / 2));
        bodyDef.linearDamping = 0f;
        bodyDef.angularDamping = 0.2f;
        //add to world
        this.body = mainSamAsteroid.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(width/2, height/2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 0.001f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; //bounce
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryMissile;
        fixtureDef.filter.maskBits = mainSamAsteroid.categoryPlayer | mainSamAsteroid.categoryBullet;

        this.goingPoint = new Vector2(Character.sprite.getX(), Character.sprite.getY());
        this.body.createFixture(fixtureDef).setUserData(this);

        //set velocity given
        speed = vel.len();
        body.setLinearVelocity(vel);
        body.setTransform(x, y, body.getLinearVelocity().angle());
        box.dispose();

        this.sprite.setPosition(body.getPosition().x - width / 2, body.getPosition().y - height / 2);
        this.sprite.setSize(width, height);
        this.timeCount = rng.nextFloat() * 10f;
    }

    //Static initialization
    public static void init(){
        int columns = 9;
        int rows = 9;
        explodeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/softbreak.mp3"));
        Texture sheet = new Texture("images/explosion.png");
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()/columns, sheet.getHeight()/rows);
        TextureRegion[] frames = new TextureRegion[columns * rows - 1]; // last part of this sprite is a repeat therefore I took it off
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (columns * i + j < columns * rows - 1){
                    frames[i * columns + j] = tmp[i][j];
                }
            }
        }
        animation = new Animation(0.056f, frames);
    }

    public void updateAngle(){ //update the change in velocity angle based on sprite angle
        body.setTransform(body.getPosition(),body.getLinearVelocity().angle()*(float)Math.PI/180);
    }

    //apply force to change velocity, therefore tracking the Character
    public void applyForceToPath(){
        float temp;
        float other = 1;
        float a1 = body.getLinearVelocity().angleRad(); //angle of velocity
        float a2 = (float)Math.atan2(goingPoint.y - body.getPosition().y, goingPoint.x - body.getPosition().x); //angle between character and missile
        if (a1 > a2){
            temp = -1; //turn clockwise if needed
        }
        else
        {
            temp = 1; //turn counterclockwise if needed
        }
        if (Math.abs(a1-a2) < turnIncrement){
            other = Math.abs((a1-a2))/turnIncrement; //turn fine amounts (less than increment) if needed
        }

        //finally set change in velocity/rotation based on calculated
        body.setLinearVelocity(speed*(float)Math.cos(a1+other * temp * turnIncrement),speed*(float)Math.sin(a1+other * temp * turnIncrement));
    }

    //update the target point to the characters position
    public void updatePoint(){
        goingPoint = new Vector2(Character.sprite.getX(), Character.sprite.getY());
    }
}


