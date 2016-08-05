package com.pipit.agc.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Eric on 7/24/2016.
 */
public class DayDetailDialog extends AppCompatDialogFragment {
    private static final String PARAM_CONTENT_VIEW = "content_view";
    private String titletxt;

    public static DayDetailDialog create(int contentView) {
        Bundle b = new Bundle();
        b.putInt(PARAM_CONTENT_VIEW, contentView);
        DayDetailDialog fragment = new DayDetailDialog();
        fragment.setArguments(b);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        int contentView = args.getInt(PARAM_CONTENT_VIEW);
        return inflater.inflate(contentView, container, false);
    }

    public void onResume() {
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.75), WindowManager.LayoutParams.WRAP_CONTENT);
        //window.setGravity(Gravity.CENTER);
        super.onResume();
    }

    public void setTitleText(String title){
        titletxt = title;
    }
}