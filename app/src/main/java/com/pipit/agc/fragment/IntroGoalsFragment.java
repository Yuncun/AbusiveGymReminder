package com.pipit.agc.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pipit.agc.R;

/**
 * This is a prototype page for a future feature to let the user define personal goals at startup.
 * The goals will be in the form of three choices
 *  'im too fat'
 *  'im too skinny'
 *  'im satisfied with my own self image'
 *
 *  These would be used to personalize insults (for example, choice 1 will send you fat insults)
 *  If you pick choice 3, the card disappears and you see a notification saying 'dont kid yourself'
 */
public class IntroGoalsFragment extends Fragment {
    CardView choiceOne;
    CardView choiceTwo;
    CardView choiceThree;

    public IntroGoalsFragment() {
        // Required empty public constructor
    }

    public static IntroGoalsFragment newInstance() {
        IntroGoalsFragment fragment = new IntroGoalsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_intro_goals, container, false);
        v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightgreen));
        View.OnClickListener clicky = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        };

        View.OnClickListener demoralize = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Don't kid yourself", Toast.LENGTH_LONG).show();
                view.animate()
                        .translationY(view.getHeight())
                        .alpha(0.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                choiceThree.setVisibility(View.GONE);
                            }
                        });
        }};
        choiceOne = (CardView) v.findViewById(R.id.choice_one);
        choiceOne.setOnClickListener(clicky);
        choiceTwo = (CardView) v.findViewById(R.id.choice_two);
        choiceTwo.setOnClickListener(clicky);
        choiceThree = (CardView) v.findViewById(R.id.choice_three);
        choiceThree.setOnClickListener(demoralize);
        return v;
    }

}
