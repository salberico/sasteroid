package com.salberico.asteroid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import java.util.Random;

/**
 * Created by Sam Alberico on 27/04/2016.
 */

//Enemy ships entity class
public class Enemy {
    //STATIC
    public static Random rng = new Random();
    public static float shipWidth = 6f; //static width for all enemies
    public static float shipHeight = 3.3f; //static height for all enemies
    public static float reloadTime = 5f; //time between spawn for enemies
    public static float maxSpeed = 9f; //for random generation
    public static float minSpeed = 5f; //for random generation
    public static float deltaTime; //time from last spawned enemy
    public static float turnIncrement = 0.01f; //maximum allowed radians per update of turn
    public static float turnTime = 0.01f; //time between velocity updates
    public static float damageScale = 0.5f; //how much damage is done on collision
    public static float screenTime = 10f; //time off screen before ship is destroyed

    //DYNAMIC
    public Body body; //box2d body
    public Sprite sprite = new Sprite(new Texture("images/ship.png"));
    public float timeCount = 0f; //time for fire animation
    public Vector2 goingPoint; //current point of destination
    public float speed; //random speed
    public float lastTurn; //time since last turn/ velocity update
    public float lastMissile = Missile.reloadTime/2; //time since last missile shot
    public float lastScreen = 0f; //time since last on screen
    public boolean exploding = false;
    public int side; //-1 left, 1 right what side is the ship currently flying to

    //Instance initialization
    public Enemy(float x, float y, float width, float height, float sp, Vector2 point, int s){
        //initial xPos, initial yPos, width, height, speed, initial going point, initial side

        //Box2d initialization
        BodyDef bodyDef = new BodyDef();
        this.lastTurn = 0f;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(x + width / 2, y + height / 2));
        bodyDef.linearDamping = 0f;
        bodyDef.angularDamping = 0.2f;
        this.lastMissile = 0f;
        this.body = mainSamAsteroid.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(width/2, height/2);

        //box.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; //bounce
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryEnemy;
        fixtureDef.filter.maskBits = ~mainSamAsteroid.categoryWall;

        this.goingPoint = point;
        this.body.createFixture(fixtureDef).setUserData(this);

        speed = sp;

        //calculate and set velocity to going point based on random speed given
        body.setLinearVelocity(new Vector2(sp * (float) Math.cos(Math.atan2(goingPoint.y - y, goingPoint.x - x)), sp * (float) Math.sin(Math.atan2(goingPoint.y - y, goingPoint.x - x))));
        body.setTransform(x, y, body.getLinearVelocity().angle()); //set angle to velocity, ship pointed forwards
        box.dispose();

        this.sprite.setPosition(body.getPosition().x - width / 2, body.getPosition().y - height / 2);
        this.sprite.setSize(width, height);
        this.side = s;

        //randomize time for fire animation
        this.timeCount = rng.nextFloat() * 10f;
    }

    //set the angle of the ship to current velocity, to keep it going forwards
    public void updateAngle(){
        body.setTransform(body.getPosition(),body.getLinearVelocity().angle()*(float)Math.PI/180);
    }

    //apply change in velocity to make ship slowly turn towards destination
    public void applyForceToPath(){
        float temp; //temp used for final change
        float other = 1; //if change needed is less than max increment
        float a1 = body.getLinearVelocity().angleRad(); //angle of velocity
        float a2 = (float)Math.toDegrees(Math.atan2(goingPoint.y - body.getPosition().y, goingPoint.x - body.getPosition().x)); //angle to point
        if (a1 > a2){
            temp = -1; //counterclockwise
        }
        else
        {
            temp = 1; //clockwise
        }
        if (Math.abs(a1-a2) < turnIncrement){
            other = Math.abs((a1-a2))/turnIncrement; //either direction, but proper size
        }

        // apply rotation, with same speed
        body.setLinearVelocity(speed*(float)Math.cos(a1+other * temp * turnIncrement),speed*(float)Math.sin(a1+other * temp * turnIncrement));
    }

    //give new point if reached current one
    public void updatePoint(){
        //if ship is close to current going point
        if (new Vector2(goingPoint.y - body.getPosition().y, goingPoint.x - body.getPosition().x).len() <= shipWidth*2){
            side *= -1; //flip flop from left to right
            goingPoint = randPoint(side);
        }
    }

    //return a random float between min and max inclusive
    private float randFloat(float min, float max){
        return rng.nextFloat() * (max - min) + min;
    }

    //give random point on left(-1) or right(1) side
    public Vector2 randPoint(int side){
        return new Vector2(randFloat((side+1)*2*mainSamAsteroid.camera.viewportWidth/3, mainSamAsteroid.camera.viewportHeight/((side-1)*-1.5f)), randFloat(0,mainSamAsteroid.camera.viewportHeight));
    }

    //check if enemy ship is offscreen
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
