package com.citypulse.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;
import com.citypulse.R;
import com.citypulse.local.entity.GroupEntity;

public class GroupsAdapter extends ListAdapter<GroupEntity, GroupsAdapter.VH> {

    private final Context ctx;
    private OnJoinClickListener joinListener;

    public interface OnJoinClickListener {
        void onJoinClick(GroupEntity group);
    }

    public void setOnJoinClickListener(OnJoinClickListener listener) {
        this.joinListener = listener;
    }

    public GroupsAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<GroupEntity>() {
            @Override public boolean areItemsTheSame(@NonNull GroupEntity a, @NonNull GroupEntity b) { return a.id == b.id; }
            @Override public boolean areContentsTheSame(@NonNull GroupEntity a, @NonNull GroupEntity b) { 
                return (a.myStatus != null && a.myStatus.equals(b.myStatus)) && a.memberCount == b.memberCount; 
            }
        });
        this.ctx = ctx;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_group, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        GroupEntity g = getItem(pos);
        h.tvName.setText(g.name);
        h.tvReason.setText(g.reason);
        h.tvCity.setText(g.city);
        h.tvMembers.setText(g.memberCount + " members");
        h.tvAdmin.setText("Admin: " + g.adminName);

        // Status badge
        int bg, fg; String label;
        switch (g.myStatus == null ? "" : g.myStatus) {
            case "accepted": bg = R.color.cp_promo_dim;     fg = R.color.cp_promo;     label = "JOINED";   break;
            case "pending":  bg = R.color.cp_warning_dim;   fg = R.color.cp_warning;   label = "PENDING";  break;
            case "rejected": bg = R.color.cp_urgent_dim;    fg = R.color.cp_urgent;    label = "REJECTED"; break;
            case "admin":    bg = R.color.cp_blue_dim;      fg = R.color.cp_blue;      label = "ADMIN";    break;
            default:         bg = R.color.cp_bg_elevated;   fg = R.color.cp_text_muted;label = "JOIN";     break;
        }
        h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bg)));
        h.tvStatus.setTextColor(ContextCompat.getColor(ctx, fg));
        h.tvStatus.setText(label);

        h.tvStatus.setOnClickListener(v -> {
            if (joinListener != null) joinListener.onJoinClick(g);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvReason, tvCity, tvMembers, tvAdmin, tvStatus;
        VH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvGroupName);
            tvReason  = v.findViewById(R.id.tvGroupReason);
            tvCity    = v.findViewById(R.id.tvGroupCity);
            tvMembers = v.findViewById(R.id.tvGroupMembers);
            tvAdmin   = v.findViewById(R.id.tvGroupAdmin);
            tvStatus  = v.findViewById(R.id.tvGroupStatus);
        }
    }
}
