package com.citypulse.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.citypulse.R;
import com.citypulse.fragments.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment feedFrag, jobsFrag, groupsFrag, profileFrag, activeFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target = null;
            if      (id == R.id.nav_feed)    { if (feedFrag    == null) feedFrag    = new FeedFragment();    target = feedFrag; }
            else if (id == R.id.nav_jobs)    { if (jobsFrag    == null) jobsFrag    = new JobsFragment();    target = jobsFrag; }
            else if (id == R.id.nav_groups)  { if (groupsFrag  == null) groupsFrag  = new GroupsFragment();  target = groupsFrag; }
            else if (id == R.id.nav_profile) { if (profileFrag == null) profileFrag = new ProfileFragment(); target = profileFrag; }

            if (target != null && target != activeFrag) {
                getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, target)
                    .commit();
                activeFrag = target;
                return true;
            }
            return false;
        });

        if (savedInstanceState == null)
            nav.setSelectedItemId(R.id.nav_feed);
    }
}
