package com.citypulse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.citypulse.R;
import com.citypulse.local.entity.GroupMemberEntity;
import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.VH> {

    private List<GroupMemberEntity> members = new ArrayList<>();
    private boolean isAdmin;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onAccept(GroupMemberEntity member);
        void onDecline(GroupMemberEntity member);
        void onRemove(GroupMemberEntity member);
    }

    public MemberAdapter(boolean isAdmin, OnMemberActionListener listener) {
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setMembers(List<GroupMemberEntity> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GroupMemberEntity member = members.get(position);
        holder.tvName.setText(member.userName);
        holder.tvRole.setText(member.userRole);

        if ("pending".equals(member.status)) {
            holder.layoutActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            holder.btnRemove.setVisibility(View.GONE);
            holder.btnAccept.setOnClickListener(v -> listener.onAccept(member));
            holder.btnDecline.setOnClickListener(v -> listener.onDecline(member));
        } else {
            holder.layoutActions.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(isAdmin && !"admin".equals(member.userRole) ? View.VISIBLE : View.GONE);
            holder.btnRemove.setOnClickListener(v -> listener.onRemove(member));
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvRole;
        ImageButton btnRemove, btnAccept, btnDecline;
        View layoutActions;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvMemberName);
            tvRole = v.findViewById(R.id.tvMemberRole);
            btnRemove = v.findViewById(R.id.btnRemoveMember);
            btnAccept = v.findViewById(R.id.btnAcceptRequest);
            btnDecline = v.findViewById(R.id.btnDeclineRequest);
            layoutActions = v.findViewById(R.id.layoutRequestActions);
        }
    }
}
