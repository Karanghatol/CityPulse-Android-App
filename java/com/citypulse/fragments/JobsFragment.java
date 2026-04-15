package com.citypulse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.citypulse.R;
import com.citypulse.activities.CreateJobActivity;
import com.citypulse.adapters.JobsAdapter;
import com.citypulse.local.AppDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class JobsFragment extends Fragment {

    private RecyclerView recyclerView;
    private JobsAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        db = AppDatabase.get(requireContext());
        recyclerView = view.findViewById(R.id.rvJobs);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize adapter with empty list
        adapter = new JobsAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabNewJob);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreateJobActivity.class));
        });

        // ✅ Observe ALL jobs instead of filtering
        db.jobDao().getAllJobsLiveData().observe(getViewLifecycleOwner(), jobs -> {
            adapter.updateData(jobs);
        });
    }
}
