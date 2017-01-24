package com.salberico.asteroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;

/**
 * Created by Sam Alberico on 26/04/2016.
 */

//Fully static class, to hold Character (main player)/(controllable ship) info
public class Character {
    //STATIC
    static float radius = 2;
    public static Sprite sprite;
    public static float angle = 0;
    public static Body body; //box2d body
    public static float maxArmor = 100f;
    public static float armor = 100f; //aka health
    public static float resistance = 0.01f;
    public static float recoilEffect = -0.1f; //custom physics for firing bullet with recoil
    public static int multiplier = 1; //score multiplier (amount of entities destroyed)
    public static int score; //score of player, based on kills of asteroids and enemies
    public static Sprite gun;
    public static boolean recoil = true;
    public static float timeCount = 0f;
    public static final int scoreSize = 12;
    public static Engine[] engine; //location/ class for fire from engine
    public static Sound engineStart = Gdx.audio.newSound(Gdx.files.internal("sounds/fireinit.mp3"));
    public static Music engineCont = Gdx.audio.newMusic(Gdx.files.internal("sounds/firemain.mp3"));


    //Static initialization
    public static void init(){
        score = 0;
        multiplier = 1;// reset multiplier
        Character.engineCont.setLooping(true);
        sprite = new Sprite(new Texture("images/ship2.png"));
        sprite.setSize(radius*2, radius*2);
        gun = new Sprite(new Texture("images/gun.png"));

        //another box2d bodydef
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((Gdx.graphics.getWidth()/2 - sprite.getWidth()/2)/mainSamAsteroid.scale, (Gdx.graphics.getHeight()/2 - sprite.getHeight()/2)/mainSamAsteroid.scale);
        bodyDef.linearDamping = 0.5f; //linear damping aka friction in the air
        armor = 100f;

        body = mainSamAsteroid.world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);
        armor = maxArmor;

        //another box2d fixture definition
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.filter.categoryBits = mainSamAsteroid.categoryPlayer;
        fixtureDef.filter.maskBits = -1; //collide with all in &
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; //bounce
        body.createFixture(fixtureDef).setUserData("Character"); //give arbitrary userdata for id in collision listener

        circle.dispose();

        gun.setSize(4,1.5f);
        gun.setPosition(body.getPosition().x - gun.getHeight()/2, body.getPosition().y - gun.getHeight()/2);
        gun.setOrigin(gun.getHeight()/2, gun.getHeight()/2);

        //Fire
        engine = new Engine[2];
        engine[0] = new Engine(-1.8f, 1.25f, 1.8f, 3);
        engine[1] = new Engine(-1.8f, -1.20f, 1.8f, 3);
    }

    //return converted/formatted score with leading zeroes
    static String giveScore(){
        String temp =  Integer.toString(score);
        String r = temp;
        for (int i = 0; i < scoreSize - temp.length(); i++){
            r = "0" +r;
        }
        return r;
    }
}
