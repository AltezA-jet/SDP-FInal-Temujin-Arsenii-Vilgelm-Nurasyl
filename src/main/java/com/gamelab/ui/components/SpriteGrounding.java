package com.gamelab.ui.components;

import javafx.scene.image.PixelReader;

/** Feet refer to the lower edge of visible artwork, not to transparent atlas padding. */
public final class SpriteGrounding {
    public static int bottomPadding(PixelReader pixels,int frameX,int width,int height) {
        for(int y=height-1;y>=0;y--) for(int x=frameX;x<frameX+width;x++)
            if((pixels.getArgb(x,y)>>>24)>=128) return height-y-1;
        return 0;
    }
    /** JavaFX scaleY is centred on the node; compensate that pivot to keep crouching feet anchored. */
    public static double footOffset(double height,double padding,double scaleY) {
        return height*(1-scaleY)/2+padding*scaleY;
    }
    private SpriteGrounding() { }
}
