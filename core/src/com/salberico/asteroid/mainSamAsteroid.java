package com.salberico.asteroid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Main logic class of game, controls all aspects from touch to physics
public class mainSamAsteroid extends ApplicationAdapter {
	SpriteBatch batch; //main and only sprite batch, used to draw to screen
	boolean gameOver; //is game over?
	Sprite backGround;
	public static World world; //box2d world, container for backend physics
	public static float scale = 32f; //scale of world
	static float gravity = -9.8f; //gravity (CURRENTLY NOT USING)
	public static OrthographicCamera camera = new OrthographicCamera(); //Camera used for drawing, calculations based
	List<Debris> debrisList = new ArrayList<Debris>(); //list for Debris class
	List<Bullet> bulletList = new ArrayList<Bullet>(); //bullet list
	public static List<Asteroid> asteroidList = new ArrayList<Asteroid>(); //list containing all asteroid instances
	List<Enemy> enemyList = new ArrayList<Enemy>();
	List<Engine> enemyEngine = new ArrayList<Engine>();
	List<Missile> missileList = new ArrayList<Missile>();
	List<Explosion> explosionList = new ArrayList<Explosion>();
	List<ArmorBar> armorEnemyList = new ArrayList<ArmorBar>();
	boolean play = true; //play or pause
	ArmorBar charBar; //health/armor bar for character
	Random rng = new Random();
	Matrix4 matrix; //for variable gravity (NOT CURRENTLY USING)
	Vector2 rotation; // (NOT CURRENTLY USING)
	float deltaTime = 0; //time since last render
	BitmapFont smallFont; //font for score
	FreeTypeFontGenerator.FreeTypeFontParameter parameter;
	BitmapFont largeFont; //font for final score
	BitmapFont yellowFont; //font if top 3 score is achieved
	float engineVolume; //engine volume for resuming after paused


	//Categories used for collision filtering
	public static final int categoryWall = 0x0001;
	public static final int categoryPlayer = 0x0002;
	public static final int categoryAsteroid = 0x0004;
	public static final int categoryBullet = 0x0008;
	public static final int categoryEnemy = 0x0010;
	public static final int categoryDebris = 0x0020;
	public static final int categoryMissile = 0x0040;

	//restart function, simple resets all necessary variables, and reinitialize the static classes
	public void restart(){
		batch.dispose();
		batch = new SpriteBatch();
		gameOver = false;
		world.dispose();
		world = new World(new Vector2(0, 0), false);
		debrisList.clear();
		bulletList.clear();
		asteroidList.clear();
		enemyList.clear();
		enemyEngine.clear();
		missileList.clear();
		explosionList.clear();
		armorEnemyList.clear();
		play = true;
		deltaTime = 0;
		matrix = new Matrix4();
		rotation = new Vector2(0,0);
		Run.init();
		Shoot.init();
		Character.init();
		Fire.init();
		Asteroid.init();
		Missile.init();
		Explosion.init();
		PausePlay.init();
		Restart.init();
		Highscore.init();
		charBar = new ArmorBar(Character.sprite.getX(), Character.sprite.getY()+Character.sprite.getHeight()*1.2f, Character.sprite.getWidth(), Character.sprite.getWidth()/5, Character.armor);
		new Wall(-1,0,1, camera.viewportHeight);
		new Wall(0,camera.viewportHeight,camera.viewportWidth, 1);
		new Wall(camera.viewportWidth,0,1, camera.viewportHeight);
		new Wall(0,-1,camera.viewportWidth,1);
		world.setContactListener(new contactListener());
		Character.engineCont.play();
		Character.engineCont.setVolume(0);
		engineVolume = 0f;
		Character.score = 0;
	}

	//initial creating function, called by android
	@Override
	public void create() {
		play = true; //not paused

		//initialize
		batch = new SpriteBatch();
		world = new World(new Vector2(0, 0), false);
		camera.setToOrtho(false, Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale);

		backGround = new Sprite(new Texture("images/space.jpg"));
		backGround.setSize(camera.viewportWidth, camera.viewportHeight);
		backGround.setPosition(0, 0);

		matrix = new Matrix4();
		rotation = new Vector2(0,0);

		//initialize
		Run.init();
		Shoot.init();
		Character.init();
		Fire.init();
		Asteroid.init();
		Missile.init();
		Explosion.init();
		PausePlay.init();
		Restart.init();
		Highscore.init();
		charBar = new ArmorBar(Character.sprite.getX(), Character.sprite.getY()+Character.sprite.getHeight()*1.2f, Character.sprite.getWidth(), Character.sprite.getWidth()/5, Character.armor);

		//Initialize fonts
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/braciola.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 30;
		largeFont = generator.generateFont(parameter);
		largeFont.setUseIntegerPositions(false);
		largeFont.getData().setScale(0.1f);
		parameter.size = 30;
		smallFont = generator.generateFont(parameter);
		smallFont.setUseIntegerPositions(false);
		smallFont.getData().setScale(0.07f);
		parameter.color = Color.YELLOW;
		yellowFont = generator.generateFont(parameter);
		yellowFont.setUseIntegerPositions(false);
		yellowFont.getData().setScale(0.07f);
		generator.dispose();

		//creating bounding walls for characters
		new Wall(-1,0,1, camera.viewportHeight);
		new Wall(0,camera.viewportHeight,camera.viewportWidth, 1);
		new Wall(camera.viewportWidth,0,1, camera.viewportHeight);
		new Wall(0,-1,camera.viewportWidth,1);

		//initialize created contact listener
		world.setContactListener(new contactListener());

		//start music (engine noise) but set volume to zero
		Character.engineCont.play();
		Character.engineCont.setVolume(0);

	}

	//called on every game update, as fast as it can handle
	@Override
	public void render() {
		//update camera
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//update change in time since last render, that way physics does not scale of framerate
		if (play) deltaTime = Gdx.graphics.getDeltaTime();
		else deltaTime = 0;

		//begin sprite batch and begin drawing
		batch.begin();

		//background
		batch.draw(backGround, 0, 0, backGround.getWidth(), backGround.getHeight());

		//Debris
		for (int i = 0; i < debrisList.size(); i++) {
			batch.draw(debrisList.get(i).sprite.getTexture(), debrisList.get(i).sprite.getX(), debrisList.get(i).sprite.getY(), debrisList.get(i).sprite.getWidth() / 2, debrisList.get(i).sprite.getHeight() / 2, debrisList.get(i).sprite.getWidth(), debrisList.get(i).sprite.getHeight(), 1, 1, debrisList.get(i).body.getAngle() * (float) (180f / Math.PI), debrisList.get(i).sprite.getRegionX(), debrisList.get(i).sprite.getRegionY(), debrisList.get(i).sprite.getRegionWidth(), debrisList.get(i).sprite.getRegionHeight(), false, false);
		}

		//Enemies
		for (int i = 0; i < enemyList.size(); i++) {
			enemyList.get(i).timeCount += deltaTime;
			//drawn off global animation with respective timing
			batch.draw(enemyList.get(i).sprite.getTexture(), enemyList.get(i).sprite.getX(), enemyList.get(i).sprite.getY(), enemyList.get(i).sprite.getWidth() / 2, enemyList.get(i).sprite.getHeight() / 2, enemyList.get(i).sprite.getWidth(), enemyList.get(i).sprite.getHeight(), 1, 1, enemyList.get(i).body.getAngle() * (float) (180f / Math.PI), enemyList.get(i).sprite.getRegionX(), enemyList.get(i).sprite.getRegionY(), enemyList.get(i).sprite.getRegionWidth(), enemyList.get(i).sprite.getRegionHeight(), false, false);
		}

		//(NOT CURRENTLY USED)
		for (int i = 0; i < armorEnemyList.size(); i++) {
			batch.draw(armorEnemyList.get(i).baseSprite, armorEnemyList.get(i).baseSprite.getX(), armorEnemyList.get(i).baseSprite.getY(), armorEnemyList.get(i).baseSprite.getWidth(), armorEnemyList.get(i).baseSprite.getHeight());
			batch.draw(armorEnemyList.get(i).currentSprite, armorEnemyList.get(i).currentSprite.getX(), armorEnemyList.get(i).currentSprite.getY(), armorEnemyList.get(i).currentSprite.getWidth(), armorEnemyList.get(i).currentSprite.getHeight());
		}

		//Asteroids
		for (int i = 0; i < asteroidList.size(); i++) {
			asteroidList.get(i).timeCount += deltaTime; //increment animation
			//drawn off global animation with respective timing
			batch.draw(asteroidList.get(i).animation.getKeyFrame(asteroidList.get(i).timeCount, true).getTexture(), asteroidList.get(i).sprite.getX(), asteroidList.get(i).sprite.getY(), asteroidList.get(i).sprite.getWidth() / 2, asteroidList.get(i).sprite.getHeight() / 2, asteroidList.get(i).sprite.getWidth(), asteroidList.get(i).sprite.getHeight(), 1, 1, asteroidList.get(i).body.getAngle() * (float) (180f / Math.PI), asteroidList.get(i).animation.getKeyFrame(asteroidList.get(i).timeCount, true).getRegionX(), asteroidList.get(i).animation.getKeyFrame(asteroidList.get(i).timeCount, true).getRegionY(), asteroidList.get(i).animation.getKeyFrame(asteroidList.get(i).timeCount, true).getRegionWidth(), asteroidList.get(i).animation.getKeyFrame(asteroidList.get(i).timeCount, true).getRegionHeight(), false, false);
		}

		//Bullets
		for (int i = 0; i < bulletList.size(); i++) {
			batch.draw(bulletList.get(i).sprite.getTexture(), bulletList.get(i).sprite.getX(), bulletList.get(i).sprite.getY(), bulletList.get(i).sprite.getWidth() / 2, bulletList.get(i).sprite.getHeight() / 2, bulletList.get(i).sprite.getWidth(), bulletList.get(i).sprite.getHeight(), 1, 1, bulletList.get(i).body.getAngle() * (float) (180f / Math.PI), bulletList.get(i).sprite.getRegionX(), bulletList.get(i).sprite.getRegionY(), bulletList.get(i).sprite.getRegionWidth(), bulletList.get(i).sprite.getRegionHeight(), false, false);
		}

		//Missiles
		for (int i = 0; i < missileList.size(); i++) {
			batch.draw(missileList.get(i).sprite.getTexture(), missileList.get(i).sprite.getX(), missileList.get(i).sprite.getY(), missileList.get(i).sprite.getWidth() / 2, missileList.get(i).sprite.getHeight() / 2, missileList.get(i).sprite.getWidth(), missileList.get(i).sprite.getHeight(), 1, 1, missileList.get(i).body.getAngle() * (float) (180f / Math.PI), missileList.get(i).sprite.getRegionX(), missileList.get(i).sprite.getRegionY(), missileList.get(i).sprite.getRegionWidth(), missileList.get(i).sprite.getRegionHeight(), false, false);
		}

		//Increase characters timecount for animation
		Character.timeCount += deltaTime;

		//Draw all current explosions
		for (int i = 0; i < explosionList.size(); i++) {
			//Increment through global animation based on relative explosion timecount, without looping
			batch.draw(Explosion.animation.getKeyFrame(explosionList.get(i).explosionCount, false).getTexture(), explosionList.get(i).x, explosionList.get(i).y, explosionList.get(i).diameter / 2, explosionList.get(i).diameter / 2,
					explosionList.get(i).diameter, explosionList.get(i).diameter, 1, 1, explosionList.get(i).angle, Explosion.animation.getKeyFrame(explosionList.get(i).explosionCount, false).getRegionX(), Explosion.animation.getKeyFrame(explosionList.get(i).explosionCount, false).getRegionY(), Explosion.animation.getKeyFrame(explosionList.get(i).explosionCount, false).getRegionWidth(), Explosion.animation.getKeyFrame(explosionList.get(i).explosionCount, false).getRegionHeight(), false, false);
		}

		//Character
		batch.draw(Character.sprite.getTexture(), Character.sprite.getX(), Character.sprite.getY(), Character.sprite.getWidth() / 2, Character.sprite.getHeight() / 2, Character.sprite.getWidth(), Character.sprite.getHeight(), 1, 1, Character.angle, Character.sprite.getRegionX(), Character.sprite.getRegionY(), Character.sprite.getRegionWidth(), Character.sprite.getRegionHeight(), false, false);

		//Draw all character fires (aka engines)
		for (int i = 0; i < Character.engine.length; i++) { //Fire from engines
			batch.draw(Fire.animation.getKeyFrame(Character.timeCount, true).getTexture(), Character.engine[i].getX(Character.angle, Character.sprite), Character.engine[i].getY(Character.angle, Character.sprite),
					Character.engine[i].size.x / 2, Character.engine[i].size.y / 2, Character.engine[i].size.x, Character.engine[i].size.y, 1, 1,
					Character.angle + 90, Fire.animation.getKeyFrame(Character.timeCount, true).getRegionWidth(), Fire.animation.getKeyFrame(Character.timeCount, true).getRegionY(), Fire.animation.getKeyFrame(Character.timeCount, true).getRegionWidth(), Fire.animation.getKeyFrame(Character.timeCount, true).getRegionHeight(), false, false);
		}

		//Draw all enemy fires (aka engines)
		for (int i = 0; i < enemyEngine.size(); i++) {
			//relative timecount, again global animation
			batch.draw(Fire.animation.getKeyFrame(enemyList.get(i).timeCount, true).getTexture(), enemyEngine.get(i).getX(enemyList.get(i).body.getAngle() * (float) (180f / Math.PI), enemyList.get(i).sprite),
					enemyEngine.get(i).getY(enemyList.get(i).body.getAngle() * (float) (180f / Math.PI), enemyList.get(i).sprite),
					enemyEngine.get(i).size.x / 2, enemyEngine.get(i).size.y / 2, enemyEngine.get(i).size.x, enemyEngine.get(i).size.y, 1, 1,
					enemyList.get(i).body.getAngle() * (float) (180f / Math.PI) + 90, Fire.animation.getKeyFrame(enemyList.get(i).timeCount, true).getRegionWidth(), Fire.animation.getKeyFrame(enemyList.get(i).timeCount, true).getRegionY(), Fire.animation.getKeyFrame(enemyList.get(i).timeCount, true).getRegionWidth(), Fire.animation.getKeyFrame(enemyList.get(i).timeCount, true).getRegionHeight(), false, false);

		}
		//Armor bar character
		batch.draw(charBar.baseSprite, charBar.baseSprite.getX(), charBar.baseSprite.getY(), charBar.baseSprite.getWidth(), charBar.baseSprite.getHeight());
		batch.draw(charBar.currentSprite, charBar.currentSprite.getX(), charBar.currentSprite.getY(), charBar.currentSprite.getWidth(), charBar.currentSprite.getHeight());

		//Gun
		batch.draw(Character.gun.getTexture(), Character.gun.getX(), Character.gun.getY(), Character.gun.getOriginX(), Character.gun.getOriginY(), Character.gun.getWidth(), Character.gun.getHeight(), 1, 1, Shoot.angle * MathUtils.radiansToDegrees, Character.gun.getRegionX(), Character.gun.getRegionY(), Character.gun.getRegionWidth(), Character.gun.getRegionHeight(), false, false);

		//Draw joysticks if not gameOver
		if (!gameOver) {
			if (Run.touchIndex >= 0) {
				batch.draw(Run.fingerSprite, Run.getVisualX(), Run.getVisualY(), Run.fingerSprite.getWidth(), Run.fingerSprite.getHeight());
				batch.draw(Run.baseSprite.getTexture(), Run.baseSprite.getX(), Run.baseSprite.getY(), Run.baseSprite.getWidth() / 2, Run.baseSprite.getHeight() / 2, Run.baseSprite.getWidth(), Run.baseSprite.getHeight(), 1, 1, Run.angle, Run.baseSprite.getRegionX(), Run.baseSprite.getRegionY(), Run.baseSprite.getRegionWidth(), Run.baseSprite.getRegionHeight(), false, false);
			}
			if (Shoot.touchIndex >= 0) {
				batch.draw(Shoot.fingerSprite, Shoot.getVisualX(), Shoot.getVisualY(), Shoot.fingerSprite.getWidth(), Shoot.fingerSprite.getHeight());
				batch.draw(Shoot.baseSprite.getTexture(), Shoot.baseSprite.getX(), Shoot.baseSprite.getY(), Shoot.baseSprite.getWidth() / 2, Shoot.baseSprite.getHeight() / 2, Shoot.baseSprite.getWidth(), Shoot.baseSprite.getHeight(), 1, 1, Shoot.angle * MathUtils.radiansToDegrees, Shoot.baseSprite.getRegionX(), Shoot.baseSprite.getRegionY(), Shoot.baseSprite.getRegionWidth(), Shoot.baseSprite.getRegionHeight(), false, false);
			}
		}

		//Draw score in top left, based on freetype font
		if (!gameOver) {
			smallFont.draw(batch, Character.giveScore() + " x" + Character.multiplier, 2, camera.viewportHeight - 2);
		} else { //If gameover draw scaled highscores and current score
			largeFont.draw(batch, "Your score: " + Character.giveScore(), camera.viewportWidth / 2 - Character.giveScore().length() * parameter.size * largeFont.getScaleX() / 2 - camera.viewportWidth/13, camera.viewportHeight / 2 + parameter.size * largeFont.getScaleY() / 2 + camera.viewportWidth/5);
			smallFont.draw(batch, "Top 3 Scores", camera.viewportWidth / 2 - "Top 3 Scores".length() * parameter.size * smallFont.getScaleX() / 2 + camera.viewportWidth/11, 2 * camera.viewportHeight / 3 - parameter.size * smallFont.getScaleY() * -1);
			for (int i = 0; i < Highscore.scoreCount; i++){
				if (Character.score == Highscore.score.get(i)){ //if new highscore make it yellow
					yellowFont.draw(batch, Highscore.giveScore(i), camera.viewportWidth / 2 - Highscore.giveScore(i).length() * parameter.size * smallFont.getScaleX() / 2 + camera.viewportWidth/10.5f,  2*camera.viewportHeight/3 - parameter.size * smallFont.getScaleY() * i);
				}
				else{
					smallFont.draw(batch, Highscore.giveScore(i), camera.viewportWidth / 2 - Highscore.giveScore(i).length() * parameter.size * smallFont.getScaleX() / 2 + camera.viewportWidth/10.5f,  2*camera.viewportHeight/3 - parameter.size * smallFont.getScaleY() * i);
				}
			}
		}
		//draw the 'button' in the top left
		batch.draw(PausePlay.sprite, PausePlay.sprite.getX(), PausePlay.sprite.getY(), PausePlay.sprite.getWidth(), PausePlay.sprite.getHeight());
		if (gameOver) {
			batch.draw(Restart.sprite,Restart.sprite.getX(),Restart.sprite.getY(),Restart.sprite.getWidth(),Restart.sprite.getHeight());
		}
		batch.end();
		updateTouch(); //update touch inputs from android api
		if (play) {
			// if not paused step the world physics and update logic
			world.step(Gdx.graphics.getDeltaTime(), 6, 2);
			updateLogic(Gdx.graphics.getDeltaTime());
		}
	}
	private void updateTouch() {
		for (int i = 0; i < 10; i++) { // 10 is max number of touch points, cycle through each
			if (Gdx.input.isTouched(i)) { // if touchpoint i is not null
				if (PausePlay.isTouching(camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0)))){
					PausePlay.touchIndex = i; //record finger that is touching play button
				}
				else if (PausePlay.touchIndex == i){
					PausePlay.touchIndex = -1; //if finger is not touching clear it
				}
				else if(gameOver && Restart.isTouching(camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0)))){
					// restart button handling
					Character.engineStart.play(0.5f);
					restart(); //restart whole game
				}
				else { // if not touched prior to this record location of base of joystick for the right (shoot)
					if (play && !gameOver && i != Shoot.touchIndex && Run.touchIndex < 0 && Gdx.input.getX(i) < Gdx.graphics.getWidth() / 2) {
						Character.engineCont.setVolume(0f);
						engineVolume = 0;
						Run.baseLoc.set(camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0)));
						Run.touchIndex = i;
					}
					// if not touched prior to this record location of base of joystick for the left (run) aka fly
					if (play && !gameOver && i != Run.touchIndex && Shoot.touchIndex < 0 && Gdx.input.getX(i) > Gdx.graphics.getWidth() / 2) {
						Shoot.baseLoc.set(camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0)));
						Shoot.touchIndex = i;
					}
				}
			}
			//when finger is lifted off of pause/play execute pause/play to eliminate constant pressing on render
			else if(i == PausePlay.touchIndex){
				if (play) {
					Character.engineStart.play(0.5f);
					Character.engineCont.setVolume(0);
					play = false;
					PausePlay.sprite.setTexture(PausePlay.play);
					PausePlay.touchIndex = -1;
				}
				else{
					Character.engineStart.play(0.5f);
					Character.engineCont.setVolume(engineVolume);
					play = true;
					PausePlay.sprite.setTexture(PausePlay.pause);
					PausePlay.touchIndex = -1;
				}
			}
		}
		if (play && !gameOver && Run.touchIndex >= 0) {
			if (!Gdx.input.isTouched(Run.touchIndex)) {
				Run.touchIndex = -1;
				Character.engineStart.play(Run.getSpeedscale() * 0.2f);
				Character.engineCont.setVolume(0);
				engineVolume = 0;
			} else { //set finger long if base is already down
				Run.fingerLoc.set(camera.unproject(new Vector3(Gdx.input.getX(Run.touchIndex), Gdx.input.getY(Run.touchIndex),0)));
			}
		}

		if (!gameOver && Shoot.touchIndex >= 0) {
			if (!Gdx.input.isTouched(Shoot.touchIndex)) {
				Shoot.touchIndex = -1;
			} else { //set fingerloc if base is already down
				Shoot.fingerLoc.set(camera.unproject(new Vector3(Gdx.input.getX(Shoot.touchIndex), Gdx.input.getY(Shoot.touchIndex),0)));
			}
		}
	}

	//update locations and some physics based of change in time since last update
	private void updateLogic(float deltaTime) {
		if (Run.touchIndex >= 0 && !gameOver) {
			//set sprite locations for base finger and current finger loc
			Run.fingerSprite.setPosition(Run.fingerLoc.x - Run.fingerSprite.getWidth() / 2, Run.fingerLoc.y - Run.fingerSprite.getHeight() / 2);
			Run.baseSprite.setPosition(Run.baseLoc.x - Run.baseSprite.getWidth() / 2, Run.baseLoc.y - Run.baseSprite.getHeight() / 2);

			//apply custom force to center of character based on left finger locations as seen above
			Character.body.applyForceToCenter(Run.maxForce * Run.getSpeedscale() * (float) (Math.cos(convertToRadians(Run.angle))), Run.maxForce * Run.getSpeedscale() * (float) (Math.sin(convertToRadians(Run.angle))), true);
			Character.body.applyForceToCenter(Character.body.getLinearVelocity().nor().scl(Character.resistance * Character.body.getLinearVelocity().len() * Character.body.getLinearVelocity().len() * -1), false); // air resistance from square velocity
			Run.updateAngle(); //update angle for static Run class

			//Update fire size for character based on current calculated scale of speed
			for (int i = 0; i < Character.engine.length; i++){
				Character.engine[i].updateSize(Run.getSpeedscale());
			}
			//update volume of engine and apply
			engineVolume = Run.getSpeedscale();
			Character.engineCont.setVolume(engineVolume);

			//finally apply angle to character sprite visually
			Character.angle = Run.angle;
		}
		else{
			for (int i = 0; i < Character.engine.length; i++){
				Character.engine[i].size.y = 0; //if not running set all engines to off
			}
		}
		if (Shoot.touchIndex >= 0 && !gameOver) { //if right finger is touching
			// again set visual positions of sprite
			Shoot.fingerSprite.setPosition(Shoot.fingerLoc.x - Shoot.fingerSprite.getWidth() / 2, Shoot.fingerLoc.y - Shoot.fingerSprite.getHeight() / 2);
			Shoot.baseSprite.setPosition(Shoot.baseLoc.x - Shoot.baseSprite.getWidth() / 2, Shoot.baseLoc.y - Shoot.baseSprite.getHeight() / 2);
			Shoot.updateAngle();

			// if change in time since last bullet is greater than the minimum time allowed then shoot
			if (Bullet.getDeltaTime() >= Bullet.reloadTime) { //Shoot
				Bullet.gunSound.play(0.1f);
				// add bullet instance to list, based on right finger loc character loc and velocity
				bulletList.add(new Bullet((Character.gun.getWidth() - Character.gun.getHeight()/2 - Bullet.radius) * (float)(Math.cos(Shoot.angle)) + Character.gun.getOriginX() + Character.gun.getX()
						, (Character.gun.getWidth() - Character.gun.getHeight()/2 - Bullet.radius) * (float)(Math.sin(Shoot.angle)) + Character.gun.getOriginY() + Character.gun.getY(),
						new Vector2(Bullet.gunPower * (float)(Math.cos(Shoot.angle)) + Character.body.getLinearVelocity().x, Bullet.gunPower * (float)(Math.sin(Shoot.angle)) + Character.body.getLinearVelocity().y)));
				//apply recoil (linear impulse) in the opposite direction of velocity vector of bullet with magnitude scaled
				if (Character.recoil) {
					Character.body.applyLinearImpulse(bulletList.get(bulletList.size() - 1).body.getLinearVelocity().scl(Character.recoilEffect * bulletList.get(bulletList.size() - 1).body.getMass()),
							Character.body.getWorldCenter(), true);
				}
			}
		}

		//Character gun, sprite and bar set
		Character.sprite.setPosition(Character.body.getPosition().x - Character.sprite.getWidth() / 2, Character.body.getPosition().y - Character.sprite.getHeight() / 2);
		Character.gun.setPosition(Character.body.getPosition().x - Character.gun.getHeight() / 2, Character.body.getPosition().y - Character.gun.getHeight() / 2);
		charBar.updateBar(Character.armor, Character.sprite.getX(), Character.sprite.getY() + Character.sprite.getHeight() * 1.2f);

		// update sprite position based on body position
		for (int i = 0; i < debrisList.size(); i++){
			debrisList.get(i).sprite.setPosition(debrisList.get(i).body.getPosition().x - debrisList.get(i).sprite.getWidth()/2, debrisList.get(i).body.getPosition().y - debrisList.get(i).sprite.getHeight()/2);
		}

		//Asteroids
		for (int i = 0; i < asteroidList.size(); i++){
			//update asteroid position based on body position
			asteroidList.get(i).sprite.setPosition(asteroidList.get(i).body.getPosition().x - asteroidList.get(i).sprite.getWidth()/2, asteroidList.get(i).body.getPosition().y - asteroidList.get(i).sprite.getHeight()/2);
			if (asteroidList.get(i).exploding){ //explode any waiting to be exploded asteroids
				explodeAsteroid(i); //this indeed will skip one asteroid, but in reality it is much more efficient
			}
			else if (!asteroidList.get(i).isOffscreen()){
				asteroidList.get(i).isPrior = false; //update prior boolean
			}
			else if (asteroidList.get(i).isOffscreen() && !asteroidList.get(i).isPrior)
			{
				//remove asteroid if offscreen after onscreen to reduce lag
				removeAsteroid(i);
			}
		}

		//Bullets
		for (int i = 0; i < bulletList.size(); i++){
			//update bullet sprite position
			bulletList.get(i).sprite.setPosition(bulletList.get(i).body.getPosition().x - bulletList.get(i).sprite.getWidth()/2, bulletList.get(i).body.getPosition().y - bulletList.get(i).sprite.getHeight()/2);
			if (bulletList.get(i).isOffscreen() || bulletList.get(i).exploding){
				//remove bullet if offscreen
				removeBullet(i);
			}
		}

		//Enemies
		for (int i = 0; i < enemyList.size(); i++){
			//increment time since last turn
			enemyList.get(i).lastTurn += deltaTime;
			//update enemies positions
			enemyList.get(i).sprite.setPosition(enemyList.get(i).body.getPosition().x - enemyList.get(i).sprite.getWidth()/2, enemyList.get(i).body.getPosition().y - enemyList.get(i).sprite.getHeight()/2);
			if (enemyList.get(i).lastTurn >= Enemy.turnTime ){
				enemyList.get(i).applyForceToPath(); //apply calculated change in velocity to slowly go towards given point
				enemyList.get(i).updatePoint(); //update said point
				enemyList.get(i).lastTurn = 0; //reset change in time since last turn
			}
			enemyList.get(i).updateAngle(); //finally update angle based on now updated velocity

			// Missile spawning
			enemyList.get(i).lastMissile += deltaTime; //increment enemies count since last missile
			if (enemyList.get(i).isOffscreen()){
				enemyList.get(i).lastScreen += deltaTime; //increment time offscreen, to eventually remove stuck enemies
			}
			else if (enemyList.get(i).lastMissile >= Missile.reloadTime && !gameOver){
				//if enemy is not offscreen, spawn/shoot a missile
				addMissile(i);
				enemyList.get(i).lastMissile = 0;
				enemyList.get(i).lastScreen = 0;
			}
			else enemyList.get(i).lastScreen = 0;

			if (enemyList.get(i).lastScreen >= Enemy.screenTime){
				destroyEnemy(i, false); //remove stuck enemies
			}
			else if (enemyList.get(i).exploding){
				destroyEnemy(i, true); //explode any waiting to be exploded enemies
			}
		}

		//Missiles
		for (int i = 0; i < missileList.size(); i++){
			//Increment missiles last turn count, for heat seeking
			missileList.get(i).lastTurn += deltaTime;
			//Update positions
			missileList.get(i).sprite.setPosition(missileList.get(i).body.getPosition().x - missileList.get(i).sprite.getWidth() / 2, missileList.get(i).body.getPosition().y - missileList.get(i).sprite.getHeight() / 2);

			//Update velocity/rotation based on Characters position, therefore following that position
			if (missileList.get(i).lastTurn >= Missile.turnTime ){
				missileList.get(i).updatePoint(); //update characters pos
				missileList.get(i).applyForceToPath(); //apply necessary change in velocity to go to that said point
				missileList.get(i).lastTurn = 0; //rest counter
				missileList.get(i).updateAngle(); //finally update angle based on calculated velocity
			}
			if (missileList.get(i).exploding){
				//explode any waiting to be exploded missiles
				explodeMissile(i);
			}
		}

		// Asteroid spawning
		Asteroid.deltaTime += deltaTime;
		if (Asteroid.deltaTime >= Asteroid.reloadTime){ //spawn asteroid when due
			addValidAsteroid();
			Asteroid.deltaTime = 0;
		}

		//Enemy spawning
		Enemy.deltaTime += deltaTime;
		//spawn enemy when time is right
		if (Enemy.deltaTime >= Enemy.reloadTime){
			addValidEnemy();
			Enemy.deltaTime = 0;
		}

		//Explosion Increment
		for (int i = 0; i < explosionList.size(); i++){
			//Increment each seperate explosions count, to recalculate corrosponding sprite
			explosionList.get(i).explosionCount += deltaTime;
			if (Explosion.animation.isAnimationFinished(explosionList.get(i).explosionCount)){
				//remove any finished animations from list
				explosionList.remove(i);
			}
		}

		//if character is dead DESTROY IT!
		if (Character.armor <= 0){
			destroyCharacter();
		}

		//(NOT CURRENTLY IN USE)
		//    Variable Gravity
		//Gdx.input.getRotationMatrix(matrix.val);
		//rotation.set(matrix.getRotation(new Quaternion(), true).getPitchRad(), matrix.getRotation(new Quaternion(), true).getYawRad());
		//world.setGravity(new Vector2(gravity*rotation.x, gravity*rotation.y));
	}

	//simple convert to radians from degrees
	private float convertToRadians(float Degrees) {
		return Degrees * (float)(Math.PI/180f);
	}

	//remove a bullet from both the given list and world
	private void removeBullet(int index) {
		if (index < 0){
			index = bulletList.size() + index;
		}
		world.destroyBody(bulletList.get(index).body);
		bulletList.remove(index);
	}

	//remove an asteroid from both the list and world
	private void removeAsteroid(int index) {
		if (index < 0){
			index = asteroidList.size() + index;
		}
		world.destroyBody(asteroidList.get(index).body);
		asteroidList.remove(index);
	}

	//explode/ fracture the given asteroid, creating 3 more random asteroids which can also be exploded
	private void explodeAsteroid(int index){
		Asteroid.explodeSound.play(asteroidList.get(index).normalizedSize() * 0.25f); //play explosion sound
		if (asteroidList.get(index).radius >= Asteroid.miniumum){ //if radius is greater than the minimum radius
			Asteroid temp = asteroidList.get(index); // store copy of original asteroid

			//Destroy the original asteroid
			world.destroyBody(asteroidList.get(index).body);
			asteroidList.remove(index);

			//get center of original asteroid
			Vector2 center = new Vector2(temp.sprite.getX()+temp.radius*1.5f,temp.sprite.getY()+temp.radius*1.5f);
			float offset = temp.radius/100; //temp offset for calculating new positions of children asteroids
			float angle = rng.nextFloat()*4; //random angle of asteroid spawning

			// add asteroid child 1, around center of point given rotated vector, giving resulting vector of velocity
			asteroidList.add(new Asteroid(rotateAround(new Vector2(center.x - temp.radius - offset, center.y + offset), new Vector2(center.x - temp.radius/2, center.y - temp.radius/2), angle).x,
					rotateAround(new Vector2(center.x - temp.radius - offset, center.y + offset), new Vector2(center.x - temp.radius/2, center.y - temp.radius/2), angle).y, temp.radius / 2,
					calculateVelocity(new Vector2(center.x - temp.radius - offset, center.y + offset), new Vector2(center.x - temp.radius/2, center.y - temp.radius/2), angle, temp.body.getLinearVelocity())));

			// add asteroid child 2, around center of point given rotated vector, giving resulting vector of velocity
			asteroidList.add(new Asteroid(rotateAround(new Vector2(center.x + offset, center.y + offset), new Vector2(center.x - temp.radius/2, center.y - temp.radius/2), angle).x,
					rotateAround(new Vector2(center.x + offset, center.y + offset), new Vector2(center.x - temp.radius/2, center.y - temp.radius/2), angle).y, temp.radius/2,
					calculateVelocity(new Vector2(center.x + offset, center.y + offset), new Vector2(center.x - temp.radius/2, center.y - temp.radius/2), angle, temp.body.getLinearVelocity())));

			// add asteroid child 3, around center of point given rotated vector, giving resulting vector of velocity
			asteroidList.add(new Asteroid(rotateAround(new Vector2(center.x - temp.radius / 2, center.y - temp.radius - offset), new Vector2(center.x - temp.radius / 2, center.y - temp.radius / 2), angle).x,
					rotateAround(new Vector2(center.x - temp.radius / 2, center.y - temp.radius - offset), new Vector2(center.x - temp.radius / 2, center.y - temp.radius / 2), angle).y, temp.radius / 2,
					calculateVelocity(new Vector2(center.x - temp.radius / 2, center.y - temp.radius - offset), new Vector2(center.x - temp.radius / 2, center.y - temp.radius / 2), angle, temp.body.getLinearVelocity())));

		}
		else //if the asteroid is under the minimum radius destroy it without creating children
		{
			world.destroyBody(asteroidList.get(index).body);
			asteroidList.remove(index);
		}
	}

	// to reduce clutter of asteroid spawning
	//rotate vector around given center vector to desired angle
	private Vector2 rotateAround(Vector2 point, Vector2 center, float angle){
		Vector2 difference = new Vector2(point.x - center.x, point.y - center.y);
		float length = difference.len();
		float temp = (float)Math.atan2(difference.y, difference.x);
		difference.x = length*(float)Math.cos(angle + temp) + center.x;
		difference.y = length*(float)Math.sin(angle + temp) + center.y;
		return difference;
	}

	//calculate velocity of rotated vector
	private Vector2 calculateVelocity(Vector2 point, Vector2 center, float angle, Vector2 baseVel){
		Vector2 difference = new Vector2(point.x - center.x, point.y - center.y);
		float temp = (float)Math.atan2(difference.y, difference.x);
		difference.x = Asteroid.explosionStrength*(float)Math.cos(angle + temp) + baseVel.x;
		difference.y = Asteroid.explosionStrength*(float)Math.sin(angle + temp) + baseVel.y;
		return difference;
	}

	// return a random float between min and max inclusive
	private float randFloat(float min, float max){
		return rng.nextFloat() * (max - min) + min;
	}

	//add a valid, yet random asteroid to the world
	private void addValidAsteroid(){
		//temporary x and y
		float x = 0;
		float y = 0;
		//random speed
		float speed = randFloat(Asteroid.minSpeed, Asteroid.maxSpeed);

		Vector2 vel;
		//initializing point to which the asteroid will go through
		Vector2 point = new Vector2(0, 0);

		//random radius
		float radius = randFloat(Asteroid.minRadius, Asteroid.maxRadius);

		//switch statement for random side the asteroid will come out from, starting at a random location from the edge of the camera
		switch (rng.nextInt(4)){
			case 0: //left
				x = -2*radius;
				y = randFloat(0,camera.viewportHeight-radius*2);
				break;
			case 1: //up
				x = randFloat(0, camera.viewportWidth-radius*2);
				y = camera.viewportHeight;
				break;
			case 2: //right
				x = camera.viewportWidth;
				y = randFloat(0,camera.viewportHeight-radius*2);
				break;
			case 3: //down
				x = randFloat(0, camera.viewportWidth-radius*2);
				y = -2*radius;
				break;
		}

		//switch statement for the type of point the asteroid will calculate its velocity to
		switch (rng.nextInt(2)){
			case 0:
				// Velocity at current body
				point = new Vector2(Character.sprite.getX() + Character.sprite.getWidth()/2, Character.sprite.getY() + Character.sprite.getHeight()/2);
				break;
			case 1:
				// Velocity at random point
				point = new Vector2(randFloat(0, camera.viewportWidth), randFloat(0, camera.viewportHeight));
				break;
		}

		//finally calculate velocity from given point and starting location
		vel = new Vector2(speed*(float)Math.cos(Math.atan2(point.y - y, point.x - x)), speed*(float)Math.sin(Math.atan2(point.y - y, point.x - x)));

		//add the asteroid to the asteroid list, and therefore the world
		asteroidList.add(new Asteroid(x, y, radius, vel));
	}

	//void for creating random yet valid enemy
	private void addValidEnemy(){
		float x = 0;
		float y = 0;

		//initial starting location on random point on edge of 1 of the 4 sides of the camera
		switch (rng.nextInt(4)){
			case 0: //left
				x = -1*Enemy.shipWidth;
				y = randFloat(0,camera.viewportHeight-Enemy.shipWidth);
				break;
			case 1: //up
				x = randFloat(0, camera.viewportWidth-Enemy.shipWidth);
				y = camera.viewportHeight;
				break;
			case 2: //right
				x = camera.viewportWidth;
				y = randFloat(0,camera.viewportHeight-Enemy.shipWidth);
				break;
			case 3: //down
				x = randFloat(0, camera.viewportWidth - Enemy.shipWidth);
				y = -1 * Enemy.shipWidth;
				break;
		}

		//initial side the point will be randomly selected in
		int side = rng.nextInt(2) * 2 - 1;
		Vector2 p; //point vector

		//get random point in corresponding side
		if (side == -1) p = randomLeft();
		else p = randomRight();

		//finally add the enemy with its corresponding initial speed, initial point and initial position
		enemyList.add(new Enemy(x, y, Enemy.shipWidth, Enemy.shipHeight, randFloat(Enemy.minSpeed, Enemy.maxSpeed), p, side));

		//add the corresponding engine to the enemy and set the size based on the constant speed of the enemy
		enemyEngine.add(new Engine(Enemy.shipWidth/-2.65f,0,2f,3.5f));
		enemyEngine.get(enemyEngine.size()-1).updateSize(enemyList.get(enemyList.size() - 1).speed / Enemy.maxSpeed);
	}

	//generate random point on left 1/3 of the screen
	private Vector2 randomLeft(){
		return new Vector2(randFloat(0,camera.viewportWidth/3),randFloat(0,camera.viewportHeight));
	}

	//generate random point on right 1/3 of the screen
	private Vector2 randomRight(){
		return new Vector2(randFloat(2*camera.viewportWidth/3, camera.viewportWidth),randFloat(0,camera.viewportHeight));
	}

	//spawn/add 2 missiles from the corresponding enemy
	private void addMissile(int index){
		missileList.add(new Missile(enemyList.get(index).sprite.getX() + enemyList.get(index).sprite.getWidth() / 2 - Missile.height / 2, enemyList.get(index).sprite.getY() + enemyList.get(index).sprite.getHeight() / 2 + Missile.width / 2, enemyList.get(index).body.getLinearVelocity().rotate(90)));
		missileList.add(new Missile(enemyList.get(index).sprite.getX() + enemyList.get(index).sprite.getWidth() / 2 - Missile.height / 2, enemyList.get(index).sprite.getY() + enemyList.get(index).sprite.getHeight() / 2 + Missile.width / 2, enemyList.get(index).body.getLinearVelocity().rotate(-90)));
	}

	//explode given missile
	private void explodeMissile(int index){
		//add explosion to list
		explosionList.add(new Explosion(missileList.get(index).sprite.getX(), missileList.get(index).sprite.getY(), Missile.width, missileList.get(index).body.getAngle()-90));

		//destroy missile
		world.destroyBody(missileList.get(index).body);
		missileList.remove(index);

		//play sound
		Explosion.explodeSound.play(0.1f);
	}

	//destroy given enemy
	private void destroyEnemy(int index, boolean explosion){
		//sometimes the enemy is destroyed offscreen therefore it is redundant to animate the explosion in such a case.

		//explode if needed
		if (explosion){
			explosionList.add(new Explosion(enemyList.get(index).sprite.getX(), enemyList.get(index).sprite.getY(), Enemy.shipWidth, enemyList.get(index).body.getAngle()-90));
			Explosion.explodeSound.play(0.2f);
		}

		//remove enemy
		world.destroyBody(enemyList.get(index).body);
		enemyList.remove(index);
		enemyEngine.remove(index);
	}

	//destroy/remove the one and only character :'(
	private void destroyCharacter(){

		//add a big explosion to the list
		explosionList.add(new Explosion(Character.sprite.getX() - Character.radius, Character.sprite.getY() - Character.radius, Character.radius * 4, Character.angle));

		//add resulting randomly generated debris
		for (int i = 0; i < 5; i++)
		{
			debrisList.add(new Debris(Character.sprite.getX(), Character.sprite.getY(), randFloat(Character.radius/4,Character.radius*2), randFloat(Character.radius/4,Character.radius*2)));
		}

		//move character aside (cause i'm nice) instead of destroying
		Character.body.setTransform(-100, -100, 0);
		Highscore.addScore(Character.score);
		Character.engineCont.setVolume(0);
		Character.armor = 10000000;
		gameOver = true;
	}

	//android called final deletion of world and batch, to prevent memory leaks
	@Override
	public void dispose(){
		batch.dispose();
		world.dispose();
	}
}
