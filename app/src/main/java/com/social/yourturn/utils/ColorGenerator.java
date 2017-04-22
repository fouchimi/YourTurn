package com.social.yourturn.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by ousma on 4/12/2017.
 */

public class ColorGenerator {

    public static ColorGenerator DEFAULT;

    static {
        DEFAULT = create(Arrays.asList(
                0xfff16364,
                0xffe4c62e,
                0xff4dd0e1,
                0xff59a2be,
                0xff2093cd,
                0xffffd54f,
                0xffad62a7,
                0xffe57373,
                0xfff06292,
                0xff4db6ac,
                0xffba68c8,
                0xff9575cd,
                0xff7986cb,
                0xff64b5f6,
                0xff4fc3f7,
                0xfff9a43e,
                0xff81c784,
                0xffaed581,
                0xffff8a65,
                0xff805781,
                0xffd4e157,
                0xffffb74d,
                0xffa1887f,
                0xfff58559,
                0xff67bf74,
                0xff90a4ae
        ));
    }

    private final List<Integer> mColors;
    private final Random mRandom;

    public static ColorGenerator create(List<Integer> colorList){
        return new ColorGenerator(colorList);
    }

    private ColorGenerator(List<Integer> colorList){
        mColors = colorList;
        mRandom = new Random(System.currentTimeMillis());
    }

    public int getRandomColor(){
        return mColors.get(mRandom.nextInt(mColors.size()));
    }

    public int getColor(Object key){
        return mColors.get(Math.abs(key.hashCode()) % mColors.size());
    }
}
