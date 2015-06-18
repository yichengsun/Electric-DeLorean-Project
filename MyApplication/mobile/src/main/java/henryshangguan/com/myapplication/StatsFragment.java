package henryshangguan.com.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

public class StatsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private StatsAdapter mAdapter;
    private Statistic[] myDataset;
//    private CastRemoteDisplayLocalService.Callbacks mCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Statistic stat1 = new Statistic("red");
        Statistic stat2 = new Statistic("orange");
        Statistic stat3 = new Statistic("yellow");
        myDataset = new Statistic[]{stat1, stat2, stat3};
        super.onCreate(savedInstanceState);
        mAdapter = new StatsAdapter(myDataset);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, parent, false);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.cardList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

//    public interface Callbacks {
//        void onStatSelected(Statistic statistic);
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        mCallbacks = (CastRemoteDisplayLocalService.Callbacks)activity;
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mCallbacks = null;
//    }
}
