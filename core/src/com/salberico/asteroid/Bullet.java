package com.salberico.asteroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.audio.Sound;

/**
 * Created by Sam Alberico on 4/27/2016.
 */

//Bullet entity class, for Characters shots
public class Bullet {
    //STATIC
    final public static float radius = 0.4f;
    public static long time = 0;
    static public long reloadTime = 300;
    final public static float gunPower = 250;
    BodyDef bodyDef;
    static Texture bulletTexture = new Texture("images/plasma.png");
    public static Sound gunSound = Gdx.audio.newSound(Gdx.files.internal("sounds/gun.mp3"));

    //DYNAMIC
    public Sprite sprite;
    public Body body;
    public boolean exploding = false;


    //Instance initialization
    public Bullet(float x, float y, Vector2 vel) {
        //xPos, yPos, velocity

        //Body def for box2d
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.linearDamping = 0f;

        this.body = mainSamAsteroid.world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.05f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.7f; //bounce
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryBullet;
        fixtureDef.filter.maskBits = ~mainSamAsteroid.categoryWall; // collide with all except wall

        this.body.createFixture(fixtureDef).setUserData(this);

        circle.dispose(); //disposing to eliminate memory leaks

        this.sprite = new Sprite(bulletTexture);
        this.sprite.setSize(radius*2, radius*2);
        this.sprite.setPosition(x - radius, y - radius);

        body.setLinearVelocity(vel);

        //time of creation
        time = TimeUtils.millis();
    }

    //checks if offscreen, to clean up useless bullets
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

    //get total lifespan of bullet
    public static long getDeltaTime(){
        return TimeUtils.timeSinceMillis(time);
    }
}
