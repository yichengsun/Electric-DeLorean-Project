package com.example.epic.testapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

public class StatsFragment extends Fragment {
    private CardView mCardView1;
    private CardView mCardView2;
    private CardView mCardView3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, parent, false);
        mCardView1 = (CardView) v.findViewById(R.id.card_view1);
        mCardView2 = (CardView) v.findViewById(R.id.card_view2);
        mCardView3 = (CardView) v.findViewById(R.id.card_view3);

        mCardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackgrounds(mCardView1);
                Fragment newFragment = DetailedStatsFragment.newInstance("zero");
                swapDetail(newFragment);
            }
        });

        mCardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackgrounds(mCardView2);
                Fragment newFragment = DetailedStatsFragment.newInstance("hello");
                swapDetail(newFragment);
            }
        });

        mCardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
                ft.remove(oldFragment).commit();

//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
//
//                if(oldFragment != null) {
//                    fm.beginTransaction().remove(oldFragment).commit();
//                }

//                setBackgrounds(mCardView3);
//                Fragment newFragment = DetailedStatsFragment.newInstance("world");
//                swapDetail(newFragment);
            }
        });
        return v;
    }

    private void swapDetail(Fragment newFragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
        
        ft.remove(oldFragment).commit();
        ft.add(R.id.mapFragmentContainer, newFragment);
        ft.commit();
    }

    private void setBackgrounds(CardView cardview) {
        mCardView1.setBackgroundColor(Color.WHITE);
        mCardView2.setBackgroundColor(Color.WHITE);
        mCardView3.setBackgroundColor(Color.WHITE);

        cardview.setBackgroundColor(Color.GRAY);
    }
}
