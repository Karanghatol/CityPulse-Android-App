package com.citypulse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citypulse.R;
import com.citypulse.adapters.FeedAdapter;
import com.citypulse.adapters.GroupsAdapter;
import com.citypulse.adapters.JobsAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.GroupEntity;
import com.citypulse.local.entity.JobEntity;
import com.citypulse.local.entity.PostEntity;
import com.citypulse.utils.SessionManager;

import java.util.List;

public class DashboardFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView rvPosts, rvMyJobs, rvAcceptedJobs, rvGroups;
    private FeedAdapter feedAdapter;
    private GroupsAdapter groupsAdapter;
    private SessionManager sm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sm = SessionManager.get();
        db = AppDatabase.get(requireContext());

        rvGroups = view.findViewById(R.id.rvGroups);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvMyJobs = view.findViewById(R.id.rvMyJobs);
        rvAcceptedJobs = view.findViewById(R.id.rvAcceptedJobs);

        rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAcceptedJobs.setLayoutManager(new LinearLayoutManager(requireContext()));

        feedAdapter = new FeedAdapter(requireContext());
        groupsAdapter = new GroupsAdapter(requireContext());

        rvPosts.setAdapter(feedAdapter);
        rvGroups.setAdapter(groupsAdapter);

        // Add action listeners to prevent crashes with the new FeedAdapter
        feedAdapter.setOnPostActionListener(new FeedAdapter.OnPostActionListener() {
            @Override
            public void onLike(PostEntity post) {
                new Thread(() -> db.postDao().toggleLike(post.id, !post.isLiked, post.isLiked ? -1 : 1)).start();
            }

            @Override
            public void onComment(PostEntity post) {
                // Comments not handled here, but could show a toast or simplified view
            }

            @Override
            public void onShare(PostEntity post) {
                // Sharing not implemented in dashboard
            }
        });

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            String currentUser = sm.getUserName();

            List<GroupEntity> myGroups = db.groupDao().getMyGroups(currentUser);
            List<PostEntity> posts = db.postDao().getPostsByUser(currentUser);
            List<JobEntity> myJobs = db.jobDao().getJobsByAuthor(currentUser);
            List<JobEntity> acceptedJobs = db.jobDao().getAcceptedJobsSync(currentUser);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    feedAdapter.submitList(posts);
                    groupsAdapter.submitList(myGroups);
                    rvMyJobs.setAdapter(new JobsAdapter(requireContext(), myJobs));
                    rvAcceptedJobs.setAdapter(new JobsAdapter(requireContext(), acceptedJobs));
                });
            }
        }).start();
    }
}
