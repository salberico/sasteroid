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

//Class just for fun when Character dies, spawns basic box entity for physics simulation
public class Debris {

    //again box2d body
    public Body body;
    public Sprite sprite = new Sprite(new Texture("images/block.png"));
    public static Random rng = new Random();

    //Instance initialization
    public Debris(float x, float y, float width, float height){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(x + width / 2, y + height / 2));
        bodyDef.linearDamping = 0.2f;
        bodyDef.angularDamping = 0.2f;
        this.body = mainSamAsteroid.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(width/2, height/2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; //bounce
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryDebris;
        fixtureDef.filter.maskBits = -1;

        this.body.createFixture(fixtureDef).setUserData(this);
        box.dispose();

        this.sprite.setPosition(body.getPosition().x - width / 2, body.getPosition().y - height / 2);
        this.sprite.setSize(width, height);
    }
}


