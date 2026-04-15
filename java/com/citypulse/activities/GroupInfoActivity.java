package com.citypulse.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.citypulse.R;
import com.citypulse.adapters.MemberAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.GroupEntity;
import com.citypulse.local.entity.GroupMemberEntity;
import com.citypulse.utils.SessionManager;
import java.util.concurrent.Executors;

public class GroupInfoActivity extends AppCompatActivity implements MemberAdapter.OnMemberActionListener {

    private int groupId;
    private AppDatabase db;
    private SessionManager sm;
    private MemberAdapter memberAdapter;
    private MemberAdapter requestAdapter;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        db = AppDatabase.get(this);
        sm = SessionManager.get();
        groupId = getIntent().getIntExtra("groupId", -1);

        TextView tvName = findViewById(R.id.tvInfoGroupName);
        TextView tvCategory = findViewById(R.id.tvInfoGroupCategory);
        TextView tvLocation = findViewById(R.id.tvInfoGroupLocation);
        RecyclerView rvMembers = findViewById(R.id.rvGroupMembers);
        RecyclerView rvRequests = findViewById(R.id.rvPendingRequests);
        TextView tvRequestsHeader = findViewById(R.id.tvRequestsHeader);
        Button btnLeave = findViewById(R.id.btnLeaveGroup);

        Executors.newSingleThreadExecutor().execute(() -> {
            GroupEntity group = db.groupDao().getById(groupId);
            GroupMemberEntity currentMember = db.groupMemberDao().getMember(groupId, sm.getUserId());
            isAdmin = currentMember != null && "admin".equals(currentMember.userRole);

            runOnUiThread(() -> {
                if (group != null) {
                    tvName.setText(group.name);
                    tvCategory.setText(group.reason);
                    tvLocation.setText(group.city);
                }

                memberAdapter = new MemberAdapter(isAdmin, this);
                rvMembers.setLayoutManager(new LinearLayoutManager(this));
                rvMembers.setAdapter(memberAdapter);

                if (isAdmin) {
                    tvRequestsHeader.setVisibility(View.VISIBLE);
                    rvRequests.setVisibility(View.VISIBLE);
                    requestAdapter = new MemberAdapter(true, this);
                    rvRequests.setLayoutManager(new LinearLayoutManager(this));
                    rvRequests.setAdapter(requestAdapter);

                    db.groupMemberDao().getPendingRequests(groupId).observe(this, requests -> {
                        requestAdapter.setMembers(requests);
                        tvRequestsHeader.setVisibility(requests.isEmpty() ? View.GONE : View.VISIBLE);
                        rvRequests.setVisibility(requests.isEmpty() ? View.GONE : View.VISIBLE);
                    });
                }

                db.groupMemberDao().getAcceptedMembers(groupId).observe(this, members -> {
                    memberAdapter.setMembers(members);
                });
            });
        });

        btnLeave.setOnClickListener(v -> leaveGroup());
    }

    private void leaveGroup() {
        Executors.newSingleThreadExecutor().execute(() -> {
            GroupMemberEntity me = db.groupMemberDao().getMember(groupId, sm.getUserId());
            if (me != null) {
                db.groupMemberDao().delete(me);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Left group", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    @Override
    public void onAccept(GroupMemberEntity member) {
        Executors.newSingleThreadExecutor().execute(() -> {
            member.status = "accepted";
            member.joinedAt = System.currentTimeMillis();
            db.groupMemberDao().update(member);
            
            GroupEntity group = db.groupDao().getById(groupId);
            if (group != null) {
                group.memberCount++;
                db.groupDao().update(group);
            }
        });
    }

    @Override
    public void onDecline(GroupMemberEntity member) {
        Executors.newSingleThreadExecutor().execute(() -> db.groupMemberDao().delete(member));
    }

    @Override
    public void onRemove(GroupMemberEntity member) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.groupMemberDao().delete(member);
            GroupEntity group = db.groupDao().getById(groupId);
            if (group != null && group.memberCount > 0) {
                group.memberCount--;
                db.groupDao().update(group);
            }
        });
    }
}
