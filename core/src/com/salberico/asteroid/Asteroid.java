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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import java.util.Random;
/**
 * Created by Sam Alberico on 6/25/2016.
 */

//Dynamic class representing the Asteroid entity
public class Asteroid {
    // STATICS
    public static float maxRadius = 4f; //Max radius for random generation
    public static float minRadius = 2f; //Min radius for random generation
    public static float explosionStrength = 3f; //how far fractured asteroid travel
    public static float maxSpeed = 15f; // for random gen
    public static float minSpeed = 5f; // for random gen
    private static final int columns = 8; //sprite sheet
    private static final int rows = 4; //sprite sheet
    static Random rng = new Random();
    public static Animation animation; //static animation used by all instances
    private static Texture sheet;
    private static TextureRegion[] frames;
    public static float damageScale = 2.5f; //scaling for damage dealt to character
    public static float reloadTime = 1f; //time between spawns
    public static float deltaTime; //time since last spawn
    public static float miniumum = 0.5f; //minimum width of asteroid
    public static Sound explodeSound;
    public static float sizeDifferential = 2; //minimum difference in size a/b such that exploding occurs

    //DYNAMICS
    public float timeCount; //time for animation
    public boolean isPrior; //boolean to check if asteroid has been on screen yet
    public Sprite sprite;
    public Body body; //box2d body for physics
    BodyDef bodyDef;
    public float radius;
    boolean exploding = false; //exploding used for delayed explosion in logic, after physic step

    public Asteroid(float x, float y, float radius, Vector2 vel){

        this.radius = radius;
        this.timeCount = rng.nextFloat() * 100;

        //body def for box2d physics
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.linearDamping = 0f;

        this.body = mainSamAsteroid.world.createBody(bodyDef);

        //shape
        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryAsteroid; //collision category used for identification in listener
        fixtureDef.filter.maskBits = ~mainSamAsteroid.categoryWall; //Collide with all but walls
        fixtureDef.density = 0.15f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.1f; //bounce

        this.body.createFixture(fixtureDef).setUserData(this);
        circle.dispose();

        this.sprite = new Sprite(new Texture("images/upstick.png")); //random texture, is not used but sprite still calls for it
        this.sprite.setSize(radius*2.5f, radius*2.5f);
        this.sprite.rotate(rng.nextFloat()*360);
        this.sprite.setPosition(x - radius, y - radius);

        // initial velocities
        this.body.setLinearVelocity(vel);
        this.body.setAngularVelocity(2*rng.nextFloat()-1f);

        //not on screen yet
        this.isPrior = true;
    }

    public static void init(){
        //Static initialization, for loading sound and sprite
        explodeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/softbreak.mp3")); //sound loading

        //Sprite loading
        sheet = new Texture("images/asteroidsheet.png");
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()/columns, sheet.getHeight()/rows); //split image into sections
        frames = new TextureRegion[columns * rows - 1]; // last part of this sprite is a repeat therefore I took it off

        //Making 2d array 1d
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (columns * i + j < columns * rows - 1){
                    frames[i * columns + j] = tmp[i][j];
                }
            }
        }

        //Add frames to a new animation
        animation = new Animation(0.050f, frames);
    }

    // get normalized size (0,1) for audio volume
    public float normalizedSize(){
        return radius/maxRadius;
    }

    //Check if asteroid is offscreen
    public boolean isOffscreen(){
        if (sprite.getX() > mainSamAsteroid.camera.viewportWidth ){
            return true;
        }
        if (sprite.getX() + sprite.getWidth() < 0){
            return true;
        }
        if (sprite.getY() > mainSamAsteroid.camera.viewportHeight){
            return true;
        }
        if (sprite.getY() + sprite.getHeight() < 0){
            return true;
        }
        return false;
    }
}
