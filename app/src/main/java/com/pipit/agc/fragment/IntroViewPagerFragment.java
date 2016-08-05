package com.pipit.agc.fragment;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.pipit.agc.R;

import io.codetail.animation.ViewAnimationUtils;

/**
 * IntroViewPagerFragment is a fragment used in the intro viewpager
 *
 * Created by Eric on 4/13/2016.
 */
public class IntroViewPagerFragment extends Fragment {
    private static final String TAG = "IntroViewPAgerFragment";
    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";

    private static final int ANIMATION_DURATION_MS = 1000;
    private Animator revealanimator;
    private View animatedViewTwo;
    private View animatedViewOne;

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
        int animatedViewId = 0;

        String title;
        String subtitle;
        switch (mPage) {
            case 0:
                layoutResId = R.layout.intro_viewpager_layout_0;
                title = "Skip a gym day...";
                break;
            case 1:
                layoutResId = R.layout.intro_viewpager_layout_1;
                title = "Get abused!";
                break;
            default:
                layoutResId = R.layout.intro_viewpager_layout_0;
                title = "";
        }
        final View view = getActivity().getLayoutInflater().inflate(layoutResId, container, false);
        view.setTag(mPage);
        TextView titletv = (TextView) view.findViewById(R.id.title);
        titletv.setText(title);

        if (mPage==1) {
            final int secondAnimation = R.id.yousuck_popup; //View of the "You Suck" popup animation
            final int firstAnimation = R.id.phoneicon;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        animatedViewOne = view.findViewById(firstAnimation);
                        animatedViewTwo = view.findViewById(secondAnimation);
                        setupanimation(animatedViewTwo);
                    }
                });
            }
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

    private void setupanimation(View animatedView){
        int cx = (animatedView.getLeft() + animatedView.getRight()) / 2;
        int cy = (animatedView.getTop() + animatedView.getBottom()) / 2;

        int dx = Math.max(cx, animatedView.getWidth() - cx);
        int dy = Math.max(cy, animatedView.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);
        finalRadius  = finalRadius / 2;

        revealanimator =
                ViewAnimationUtils.createCircularReveal(animatedView, cx, cy, 0, finalRadius);

        revealanimator.setInterpolator(new AccelerateDecelerateInterpolator());
        revealanimator.setDuration(ANIMATION_DURATION_MS);
        //revealanimator.setStartDelay(1000);
    }

    public void startAnimation(){
        if (revealanimator !=null){
            try{
                if (revealanimator.isStarted()){
                    setupanimation(animatedViewTwo);
                }
                Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        if (animatedViewTwo!=null)
                        animatedViewTwo.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (animatedViewTwo!=null && revealanimator!=null)
                        animatedViewTwo.setVisibility(View.VISIBLE);
                        revealanimator.start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                animatedViewOne.startAnimation(shake);
            }catch(Exception e){
                Log.e(TAG, "Couldn't start animation" + e.toString());
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT);
            }
        }
    }
}