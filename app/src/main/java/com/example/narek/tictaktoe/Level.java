package com.example.narek.tictaktoe;

/**
 * Created by Narek on 3/28/16.
 */
public enum Level {

    EAZY(1), NORMAL(2), HARD(4),;


    private int levelDepth;

    Level(int levelDepth) {
        this.levelDepth = levelDepth;

    }

    public int getLevelDepth() {
        return levelDepth;
    }


}
