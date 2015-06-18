package henryshangguan.com.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

/**
 * Created by henryshangguan on 6/18/15.
 */
public class DetailFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, parent, false);
        return v;
    }

    public static DetailFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable("string?", crimeId);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
