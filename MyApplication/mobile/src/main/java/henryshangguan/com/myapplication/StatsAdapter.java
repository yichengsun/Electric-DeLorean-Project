package henryshangguan.com.myapplication;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

/**
 * Created by henryshangguan on 6/17/15.
 */
public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {
    private static Statistic[] mDataset;
    private static CastRemoteDisplayLocalService.Callbacks mCallbacks;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.title);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int s = getAdapterPosition();
                    Statistic stat = mDataset[s];
                    //Fragment newFragment = DetailFragment.newInstance(stat.getId());
                    //getSupportFragmentManager().
                    //FragmentManager fm = v.getParent().getSupportFragmentManager();

//                    mCallbacks.onStatSelected(stat);
                }
            });
        }
    }

    public StatsAdapter(Statistic[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public StatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_stats, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset[position].getTitle());
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    public interface Callbacks {
        void onStatSelected(Statistic statistic);
    }

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
