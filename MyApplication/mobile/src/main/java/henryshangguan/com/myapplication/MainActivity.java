package henryshangguan.com.myapplication;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StateSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements StatsAdapter.Callbacks {

    private Button mStoreDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Parse initialization
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9", "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment fragmentMap = fm.findFragmentById(R.id.mapFragmentContainer);
        android.support.v4.app.Fragment fragmentStats = fm.findFragmentById(R.id.statsFragmentContainer);

        if (fragmentMap == null) {
            fragmentMap = new MapFragment();
            fm.beginTransaction().add(R.id.mapFragmentContainer, fragmentMap).commit();
        }

        if (fragmentStats == null) {
            fragmentStats = new StatsFragment();
            fm.beginTransaction().add(R.id.statsFragmentContainer, fragmentStats).commit();
        }
        
        // Button to store data
//        mStoreDataButton = (Button)findViewById(R.id.store_data_button);
//        mStoreDataButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Get trip end time
//                Calendar rightNow = Calendar.getInstance();
//                Date date = rightNow.getTime();
//
//                // Parse file-storing test code
//                byte[] translated = "Hello World!".getBytes();
//                ParseFile stored = new ParseFile("test.txt", translated);
//                stored.saveInBackground();
//
//                ParseObject testFileObject = new ParseObject("TestFileObject");
//                testFileObject.put("testFile", stored);
//                testFileObject.put("time", date);
//                testFileObject.saveInBackground();
//                Toast.makeText(MainActivity.this, R.string.data_saved, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStatSelected(Statistic statistic) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
        Fragment newFragment = DetailFragment.newInstance(statistic.getId());
        Log.d("stat click", "STAT CLICKED");
        // NEED TO ADD STORE MAP

        ft.add(R.id.mapFragmentContainer, newFragment);
        ft.commit();

    }
}
