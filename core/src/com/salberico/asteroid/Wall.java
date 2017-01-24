package com.salberico.asteroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Created by Sam Alberico on 4/26/2016.
 */

//Wall class which is a non dynamic entity which blocks the Character from leaving the bounds of the screen
public class Wall {
    public Body body; //box2d body
    public Sprite sprite = new Sprite(new Texture("images/block.png")); //arbitrary sprite, not seen.

    //Instance initialization
    public Wall(float x, float y, float width, float height){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(x + width/2, y + height/2));
        this.body = mainSamAsteroid.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        //set category for collision filtering
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryWall;
        //collide with all entities, unless they themselves specify
        fixtureDef.filter.maskBits = -1;

        body.createFixture(fixtureDef);
        body.setUserData(sprite);
        box.dispose();

        this.sprite.setPosition(body.getPosition().x - width/2, body.getPosition().y - height/2);
        this.sprite.setSize(width, height);
    }
}

