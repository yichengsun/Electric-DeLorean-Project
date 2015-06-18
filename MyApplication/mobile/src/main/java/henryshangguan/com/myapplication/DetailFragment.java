package henryshangguan.com.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by henryshangguan on 6/18/15.
 */
public class DetailFragment extends Fragment {

    private String text;
    private TextView mTextView;

    public DetailFragment(String s) {
        text = s;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, parent, false);
        mTextView = (TextView)v.findViewById(R.id.detail1);
        mTextView.setText(text);
        return v;
    }

//    public static DetailFragment newInstance(UUID crimeId) {
//        Bundle args = new Bundle();
//        args.putSerializable("string?", crimeId);
//        DetailFragment fragment = new DetailFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }

    public void setText(String string) {
        mTextView.setText(string);
    }

    public static DetailFragment newInstance(String string) {
        DetailFragment fragment = new DetailFragment(string);
        return fragment;
    }

}
