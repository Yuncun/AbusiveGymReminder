package com.pipit.agc.adapter;

import android.graphics.RectF;

import com.robinhood.spark.SparkAdapter;

/**
 * Created by Eric on 5/26/2016.
 */
public class MySparkAdapter  extends SparkAdapter {
    private static String TAG = "SparkAdapter";
    public float maxY = 100.0f;
    private float[] yData;
    private float totalPoints = 6;

    public MySparkAdapter(float[] yData) {
        this.yData = yData;
    }

    public void update(float[] data){
        this.yData = data;
        notifyDataSetChanged();
    }

    public void setGraphWidth(float w){
        totalPoints = w;
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
        float max = 0.0f;
        for (int i = 0 ; i < yData.length; i++){
            if (yData[i]>max){
                max=yData[i];
            }
        }
        if (max<maxY){
            max = maxY;
        }

        RectF rectum = super.getDataBounds();
        //Log.d(TAG, "getDataBounds top:" + rectum.top + " bottom:" + rectum.bottom + " left:" + rectum.left + " right:" + rectum.right );
        rectum.bottom = max;
        rectum.right = totalPoints;
        return rectum;
    }

    public boolean hasBaseLine(){
        return true;
    }

    public float getBaseLine(){
        return 60.0f;
    }



}
