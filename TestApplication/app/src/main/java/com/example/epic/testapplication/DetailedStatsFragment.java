//package com.example.epic.testapplication;
//
//import android.media.Image;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.TextView;
//
//import java.util.UUID;
//
///**
// * LEGACY CODE - NOT IN USE
// */
//
//public class DetailedStatsFragment extends Fragment {
//
//    private TextView mName;
//    private TextView mData;
//    private ImageButton mImageButton;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_detail, parent, false);
//
//        Bundle b = getArguments();
//        if(b.getString("name") != null) {
//            mName = (TextView) v.findViewById(R.id.statName);
//            mName.setText(b.getString("name"));
//        }
//
//        mImageButton = (ImageButton)v.findViewById(R.id.closeButton);
//        mImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//
//                if (fm.findFragmentById(R.id.mainFragmentContainer) != null) {
//                    Fragment oldFragment = fm.findFragmentById(R.id.mainFragmentContainer);
//                    ft.remove(oldFragment).commit();
//                }
//            }
//        });
//
//        return v;
//    }
//
////    public static DetailFragment newInstance(UUID crimeId) {
////        Bundle args = new Bundle();
////        args.putSerializable("string?", crimeId);
////        DetailFragment fragment = new DetailFragment();
////        fragment.setArguments(args);
////        return fragment;
////    }
//
////    public static DetailedStatsFragment newInstance(String string) {
////        DetailedStatsFragment fragment = new DetailedStatsFragment(string);
////        return fragment;
////    }
//}
