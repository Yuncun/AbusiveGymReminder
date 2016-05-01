package com.pipit.agc.agc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pipit.agc.agc.R;

/**
 * Created by Eric on 4/13/2016.
 */
public class IntroViewPagerFragment extends Fragment {

    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";

    private int mBackgroundColor, mPage;

    public static IntroViewPagerFragment newInstance(int backgroundColor, int page) {
        IntroViewPagerFragment frag = new IntroViewPagerFragment();
        Bundle b = new Bundle();
        b.putInt(BACKGROUND_COLOR, backgroundColor);
        b.putInt(PAGE, page);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(BACKGROUND_COLOR))
            throw new RuntimeException("Fragment must contain a \"" + BACKGROUND_COLOR + "\" argument!");
        mBackgroundColor = getArguments().getInt(BACKGROUND_COLOR);

        if (!getArguments().containsKey(PAGE))
            throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");
        mPage = getArguments().getInt(PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutResId;
        String title;
        String subtitle;
        switch (mPage) {
            case 0:
                layoutResId = R.layout.intro_viewpager_layout_1;
                title = "Automatically knows when you've been near your gym...";
                break;
            case 1:
                layoutResId = R.layout.intro_viewpager_layout_1;
                title = "Sends you abusive messages when you miss gym days!";
                break;
            default:
                layoutResId = R.layout.intro_viewpager_layout_1;
                title = "";
        }
        View view = getActivity().getLayoutInflater().inflate(layoutResId, container, false);
        view.setTag(mPage);
        TextView titletv = (TextView) view.findViewById(R.id.title);
        titletv.setText(title);
        if (mPage==1){
            titletv.setTextColor(ContextCompat.getColor(getContext(), R.color.basewhite));
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the background color of the root view to the color specified in newInstance()
        View background = view.findViewById(R.id.intro_background);
        background.setBackgroundColor(mBackgroundColor);
    }

}