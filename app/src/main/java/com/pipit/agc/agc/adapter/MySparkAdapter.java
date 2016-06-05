package com.pipit.agc.agc.adapter;

import android.graphics.RectF;
import android.util.Log;

import com.pipit.agc.agc.model.DayRecord;
import com.robinhood.spark.SparkAdapter;

import java.util.List;

/**
 * Created by Eric on 5/26/2016.
 */
public class MySparkAdapter  extends SparkAdapter {
    private static String TAG = "SparkAdapter";
    public float maxY = 100.0f;
    private float[] yData;

    public MySparkAdapter(float[] yData) {
        this.yData = yData;
    }

    public void update(float[] data){
        this.yData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return yData.length;
    }

    @Override
    public Object getItem(int index) {
        return yData[index];
    }

    @Override
    public float getY(int index) {
        return yData[index];
    }

    @Override
    public RectF getDataBounds() {
        RectF rectum = super.getDataBounds();
        Log.d(TAG, "getDataBounds top:" + rectum.top + " bottom:" + rectum.bottom + " left:" + rectum.left + " right:" + rectum.right );
        rectum.bottom = maxY;
        return rectum;
    }

    public boolean hasBaseLine(){
        return true;
    }

    public float getBaseLine(){
        return 60.0f;
    }



}
