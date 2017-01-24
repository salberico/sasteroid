package com.salberico.asteroid;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * Created by Sam Alberico on 6/26/2016.
 */
//implementation of contact listener class, to identify when collisions happen/end
public class contactListener implements ContactListener{
    @Override

    //when any tracked contact/collision begins
    public void beginContact(Contact contact) {

        //get arbitrary fixture a and b, the two objects colliding
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        //stop if null (had some weird errors)
        if(a == null || b == null) return;
        if(a.getUserData() == null || b.getUserData() == null) return;

        //any asteroid collision
        if (a.getUserData() instanceof Asteroid){
            if (b.getUserData() instanceof Asteroid){ //Asteroid on asteroid
                if (shouldExplodeAsteroid(((Asteroid) a.getUserData()), ((Asteroid) b.getUserData()))){
                    ((Asteroid) a.getUserData()).exploding = true; //explode based on given differential
                }
            }
            else
            {
                //explode always if not asteroid asteroid
                ((Asteroid) a.getUserData()).exploding = true;
            }
        }
        // repeated for compliment scenario
        if (b.getUserData() instanceof Asteroid){
            if (a.getUserData() instanceof Asteroid){
                if (shouldExplodeAsteroid(((Asteroid) b.getUserData()), ((Asteroid) a.getUserData()))){
                    ((Asteroid) b.getUserData()).exploding = true; //explode based on given differential
                }
            }
            else
            {
                ((Asteroid) b.getUserData()).exploding = true;
            }
        }

        //Missile with anything, however can only collide with Character because of mask
        if (a.getUserData() instanceof Missile){
            ((Missile) a.getUserData()).exploding = true; //explode missile upon contact
        }
        if (b.getUserData() instanceof Missile){
            ((Missile) b.getUserData()).exploding = true;
        }

        //Asteroid and Character
        if (a.getUserData() instanceof Asteroid && b.getUserData()== "Character"){
            // subtract armor based on squared radius and scale
            Character.armor -= ((Asteroid) a.getUserData()).radius* ((Asteroid) a.getUserData()).radius* Asteroid.damageScale;
        }
        if (b.getUserData() instanceof Asteroid && a.getUserData()== "Character"){
            Character.armor -= ((Asteroid) b.getUserData()).radius* ((Asteroid) b.getUserData()).radius* Asteroid.damageScale;
        }

        //Enemy and Character
        if (a.getUserData() instanceof Enemy && b.getUserData()== "Character"){
            // subtract armor based on squared radius and scale
            Character.armor -= ((Enemy) a.getUserData()).shipWidth* ((Enemy) a.getUserData()).shipWidth* Enemy.damageScale;
            ((Enemy) a.getUserData()).exploding = true;
            // Add some score
            Character.score += Character.multiplier*100*((Enemy) a.getUserData()).speed;
            Character.multiplier += 1;
        }
        if (b.getUserData() instanceof Enemy && a.getUserData()== "Character"){
            Character.armor -= ((Enemy) b.getUserData()).shipWidth* ((Enemy) b.getUserData()).shipWidth* Enemy.damageScale;
            ((Enemy) b.getUserData()).exploding = true;
            Character.score += Character.multiplier*100*((Enemy) b.getUserData()).speed;
            Character.multiplier += 1;
        }

        //Missile and Character
        if (a.getUserData() instanceof Missile && b.getUserData()== "Character"){
            //Subtract damage, missile should already be destoroyed in its own function
            Character.armor -= Missile.damageScale;
        }
        if (b.getUserData() instanceof Missile && a.getUserData()== "Character"){
            Character.armor -= Missile.damageScale;
        }

        //Enemy and Bullet
        if (a.getUserData() instanceof Enemy && b.getUserData() instanceof Bullet){
            //Destroy enemy and bullet once physics is done being calculated
            ((Bullet) b.getUserData()).exploding = true;
            ((Enemy) a.getUserData()).exploding = true;
            //Add some score
            Character.score += Character.multiplier*1000*((Enemy) a.getUserData()).speed;
            Character.multiplier += 1;
        }
        if (b.getUserData() instanceof Enemy && a.getUserData() instanceof Bullet){
            ((Bullet) a.getUserData()).exploding = true;
            ((Enemy) b.getUserData()).exploding = true;
            Character.score += Character.multiplier*1000*((Enemy) b.getUserData()).speed;
            Character.multiplier += 1;
        }

        //Bullet and Asteroid
        if (a.getUserData() instanceof Asteroid && b.getUserData() instanceof Bullet){
            //Debtor bullet and corresponding asteroid
            ((Bullet) b.getUserData()).exploding = true;
            ((Asteroid) a.getUserData()).exploding = true;
            // add some score
            Character.score += Character.multiplier*100*((Asteroid) a.getUserData()).radius;
            Character.multiplier += 1;
        }
        if (b.getUserData() instanceof Asteroid && a.getUserData() instanceof Bullet){
            ((Bullet) a.getUserData()).exploding = true;
            ((Asteroid) b.getUserData()).exploding = true;
            Character.score += Character.multiplier*100*((Asteroid) b.getUserData()).radius;
            Character.multiplier += 1;
        }

    }


    //Unused, but necessary given implementation
    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    public boolean shouldExplodeAsteroid(Asteroid a, Asteroid b){
        if (a.radius/b.radius >= Asteroid.sizeDifferential){
            return false;
        }
        else {
            return true;
        }
    }

}
