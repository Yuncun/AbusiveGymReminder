package com.pipit.agc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.activity.IntroductionActivity;
import com.pipit.agc.adapter.IntroAdapter;
import com.pipit.agc.adapter.IntroPageTransformer;
import com.pipit.agc.R;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * This is the first fragment that is shown when the user launchs the app for the first time
 * It holds a viewpager that contains n IntroViewPagerFragments.
 * Created by Eric on 12/13/2015.
 */
public class IntroFragment extends Fragment {
    public static final String TAG = "IntroFragment";
    private TextView finishButton;
    private ViewPager mViewPager;
    private CirclePageIndicator _indicator;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.intro_opening_message,
                container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);

        final IntroAdapter introadapter = new IntroAdapter(getActivity().getSupportFragmentManager(), getContext());
        mViewPager.setAdapter(introadapter);
        mViewPager.setPageTransformer(false, new IntroPageTransformer());

        _indicator  = (CirclePageIndicator) view.findViewById(R.id.titles);
        _indicator.setStrokeColor(ContextCompat.getColor(getContext(), R.color.schemefour_darkerteal));
        _indicator.setFillColor(ContextCompat.getColor(getContext(), R.color.schemefour_darkerteal));
        _indicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position==1) introadapter.notifyStartAnimation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        LinearLayout bottomBar = (LinearLayout) view.findViewById(R.id.intro_bottombar);
        bottomBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.basewhite));
        finishButton = (TextView) view.findViewById(R.id.finishbutton);
        finishButton.setText("Find your gym");
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((IntroductionActivity) getActivity()).selectFrag(1);
            }
        });

        return view;
    }
}
