package com.citypulse.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.citypulse.R;
import com.citypulse.activities.LoginActivity;
import com.citypulse.adapters.FeedAdapter;
import com.citypulse.adapters.GroupsAdapter;
import com.citypulse.adapters.JobsAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.GroupEntity;
import com.citypulse.local.entity.JobEntity;
import com.citypulse.local.entity.PostEntity;
import com.citypulse.local.entity.UserEntity;
import com.citypulse.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private RecyclerView rvActivity;
    private FeedAdapter feedAdapter;
    private GroupsAdapter groupsAdapter;
    private AppDatabase db;
    private SessionManager sm;

    private TextView tvPostCount, tvJobCount, tvGroupCount;
    private TextView tvName, tvCity;
    private CircleImageView ivAvatar;

    private String tempPicPath = "";

    private final ActivityResultLauncher<Intent> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        handlePickedImage(uri);
                    }
                }
            }
    );

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_profile, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        super.onViewCreated(v, saved);

        sm = SessionManager.get();
        db = AppDatabase.get(requireContext());

        // Header info
        tvName    = v.findViewById(R.id.tvProfileName);
        tvCity    = v.findViewById(R.id.tvProfileCity);
        ivAvatar  = v.findViewById(R.id.ivProfileAvatar);
        ImageView ivMore = v.findViewById(R.id.ivMoreOptions);
        ImageView btnEdit  = v.findViewById(R.id.btnEditProfile);

        tvPostCount = v.findViewById(R.id.tvPostCount);
        tvJobCount = v.findViewById(R.id.tvJobCount);
        tvGroupCount = v.findViewById(R.id.tvGroupCount);

        loadProfileUI();

        // Adapters
        feedAdapter = new FeedAdapter(requireContext());
        groupsAdapter = new GroupsAdapter(requireContext());

        rvActivity = v.findViewById(R.id.rvProfileActivity);
        rvActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initial Tab
        rvActivity.setAdapter(feedAdapter);
        loadMyPosts();

        TabLayout tabs = v.findViewById(R.id.profileTabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Posts
                        rvActivity.setAdapter(feedAdapter);
                        loadMyPosts();
                        break;
                    case 1: // Jobs
                        loadMyJobs();
                        break;
                    case 2: // Groups
                        rvActivity.setAdapter(groupsAdapter);
                        loadMyGroups();
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Edit Profile
        btnEdit.setOnClickListener(x -> showEditDialog());

        // Three dots menu
        ivMore.setOnClickListener(this::showMoreMenu);

        updateStats();
    }

    private void showMoreMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add("Logout");
        popup.getMenu().add("Delete Account Permanently");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Logout")) {
                logout();
            } else if (item.getTitle().equals("Delete Account Permanently")) {
                showDeleteConfirmation();
            }
            return true;
        });
        popup.show();
    }

    private void logout() {
        sm.logout();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone and all your data will be lost.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        new Thread(() -> {
            UserEntity user = db.userDao().findById(sm.getUserId());
            if (user != null) {
                db.userDao().delete(user);
                sm.logout();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                }
            }
        }).start();
    }

    private void loadProfileUI() {
        tvName.setText(sm.getUserName());
        tvCity.setText(sm.getUserCity());
        if (!sm.getProfilePic().isEmpty()) {
            Glide.with(this)
                .load(new File(sm.getProfilePic()))
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etCity = dialogView.findViewById(R.id.etEditCity);
        ImageView ivEditAvatar = dialogView.findViewById(R.id.ivEditAvatar);
        Button btnChangePic = dialogView.findViewById(R.id.btnChangePic);

        etName.setText(sm.getUserName());
        etCity.setText(sm.getUserCity());
        tempPicPath = sm.getProfilePic();

        if (!tempPicPath.isEmpty()) {
            Glide.with(this).load(new File(tempPicPath)).circleCrop().into(ivEditAvatar);
        }

        btnChangePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickMediaLauncher.launch(intent);
            this.ivEditAvatarForDialog = ivEditAvatar; 
        });

        new AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = etName.getText().toString().trim();
                String newCity = etCity.getText().toString().trim();
                if (newName.isEmpty()) return;

                saveProfile(newName, newCity, tempPicPath);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private ImageView ivEditAvatarForDialog;

    private void handlePickedImage(android.net.Uri uri) {
        try {
            File file = createProfilePicFile();
            copyUriToFile(uri, file);
            tempPicPath = file.getAbsolutePath();
            if (ivEditAvatarForDialog != null) {
                Glide.with(this).load(file).circleCrop().into(ivEditAvatarForDialog);
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile(String name, String city, String picPath) {
        new Thread(() -> {
            UserEntity user = db.userDao().findById(sm.getUserId());
            if (user != null) {
                user.name = name;
                user.city = city;
                user.profilePicPath = picPath;
                db.userDao().update(user);
                
                sm.login(user.id, user.name, user.city, user.profilePicPath);
                
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        loadProfileUI();
                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void loadMyPosts() {
        db.postDao().getMyPosts(sm.getUserId()).observe(getViewLifecycleOwner(), posts -> {
            feedAdapter.submitList(posts);
            tvPostCount.setText(String.valueOf(posts.size()));
        });
    }

    private void loadMyJobs() {
        db.jobDao().getAppliedJobs(sm.getUserName()).observe(getViewLifecycleOwner(), jobs -> {
            rvActivity.setAdapter(new JobsAdapter(requireContext(), jobs));
            tvJobCount.setText(String.valueOf(jobs.size()));
        });
    }

    private void loadMyGroups() {
        db.groupDao().getMyActivityGroups(sm.getUserName()).observe(getViewLifecycleOwner(), groups -> {
            groupsAdapter.submitList(groups);
            tvGroupCount.setText(String.valueOf(groups.size()));
        });
    }

    private void updateStats() {
        new Thread(() -> {
            String userName = sm.getUserName();
            List<PostEntity> posts = db.postDao().getPostsByUser(userName);
            List<JobEntity> jobs = db.jobDao().getAppliedJobsSync(userName);
            List<GroupEntity> groups = db.groupDao().getMyGroups(userName);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvPostCount.setText(String.valueOf(posts.size()));
                    tvJobCount.setText(String.valueOf(jobs.size()));
                    tvGroupCount.setText(String.valueOf(groups.size()));
                });
            }
        }).start();
    }

    private File createProfilePicFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "PROFILE_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(null);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private void copyUriToFile(android.net.Uri uri, File dest) throws IOException {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
