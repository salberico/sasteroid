package com.salberico.asteroid;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;

/**
 * Created by Sam Alberico on 11/05/2016.
 */

//class holding the fire animation, again used for optimization so no new instances are needed
public class Fire {
    private static final int columns = 8;
    private static final int rows = 4;

    public static Animation animation;
    private static Texture sheet;
    private static TextureRegion[] frames;

    //Sprite animation initialization once again
    static public void init(){
        sheet = new Texture("images/fire.png");
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()/columns, sheet.getHeight()/rows);
        frames = new TextureRegion[columns * rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                frames[i * columns + j] = tmp[i][j];
            }
        }
        animation = new Animation(0.0125f, frames);
    }



}
