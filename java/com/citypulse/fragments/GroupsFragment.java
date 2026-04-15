package com.citypulse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.citypulse.R;
import com.citypulse.activities.GroupChatActivity;
import com.citypulse.adapters.GroupsAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.GroupEntity;
import com.citypulse.local.entity.GroupMemberEntity;
import com.citypulse.local.entity.NotificationEntity;
import com.citypulse.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.concurrent.Executors;

public class GroupsFragment extends Fragment {

    private GroupsAdapter adapter;
    private AppDatabase db;
    private SessionManager sm;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_groups, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        super.onViewCreated(v, saved);

        db = AppDatabase.get(requireContext());
        sm = SessionManager.get();

        RecyclerView rv = v.findViewById(R.id.rvGroups);
        adapter = new GroupsAdapter(requireContext());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        db.groupDao().getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            for (GroupEntity g : groups) {
                updateMyStatusLocally(g);
            }
            adapter.submitList(groups);
        });

        v.findViewById(R.id.fabNewGroup).setOnClickListener(x -> showCreateGroupSheet());

        adapter.setOnJoinClickListener(group -> {
            if ("admin".equals(group.myStatus) || "accepted".equals(group.myStatus)) {
                Intent intent = new Intent(requireContext(), GroupChatActivity.class);
                intent.putExtra("groupId", group.id);
                intent.putExtra("groupName", group.name);
                startActivity(intent);
                return;
            }
            
            if ("pending".equals(group.myStatus)) {
                Toast.makeText(requireContext(), "Request already pending", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                // Check if already a member/requested (redundancy check)
                GroupMemberEntity existing = db.groupMemberDao().getMember(group.id, sm.getUserId());
                if (existing != null) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "Status: " + existing.status, Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                GroupMemberEntity request = new GroupMemberEntity();
                request.groupId = group.id;
                request.userId = sm.getUserId();
                request.userName = sm.getUserName();
                request.userRole = "member";
                request.status = "pending";
                db.groupMemberDao().insert(request);

                // Create notification for admin
                NotificationEntity notif = new NotificationEntity();
                notif.title = "Group Join Request";
                notif.message = sm.getUserName() + " wants to join your group: " + group.name;
                notif.type = "group_request";
                notif.groupId = group.id;
                notif.senderName = sm.getUserName();
                notif.timestamp = System.currentTimeMillis();
                db.notificationDao().insert(notif);
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Join request sent", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    private void updateMyStatusLocally(GroupEntity group) {
        Executors.newSingleThreadExecutor().execute(() -> {
            GroupMemberEntity me = db.groupMemberDao().getMember(group.id, sm.getUserId());
            if (me != null) {
                group.myStatus = "admin".equals(me.userRole) ? "admin" : me.status;
                if (isAdded()) requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            } else {
                group.myStatus = "";
                if (isAdded()) requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        });
    }

    private void showCreateGroupSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_create_group_sheet, null);
        dialog.setContentView(view);

        EditText etName = view.findViewById(R.id.etGroupName);
        Spinner spinnerReason = view.findViewById(R.id.spinnerGroupReason);
        View btnCreate = view.findViewById(R.id.btnCreateGroup);

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;

            GroupEntity group = new GroupEntity();
            group.name = name;
            group.reason = spinnerReason.getSelectedItem().toString();
            group.city = sm.getUserCity();
            group.adminId = sm.getUserId();
            group.adminName = sm.getUserName();
            group.createdBy = sm.getUserName();
            group.memberCount = 1;
            group.createdAt = System.currentTimeMillis();

            Executors.newSingleThreadExecutor().execute(() -> {
                long id = db.groupDao().insert(group);
                
                GroupMemberEntity admin = new GroupMemberEntity();
                admin.groupId = (int) id;
                admin.userId = sm.getUserId();
                admin.userName = sm.getUserName();
                admin.userRole = "admin";
                admin.status = "accepted";
                admin.joinedAt = System.currentTimeMillis();
                db.groupMemberDao().insert(admin);

                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Group created!", Toast.LENGTH_SHORT).show();
                });
            });
        });

        dialog.show();
    }
}
