package com.citypulse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.citypulse.R;
import com.citypulse.local.entity.CommentEntity;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends ListAdapter<CommentEntity, CommentsAdapter.VH> {

    public CommentsAdapter() {
        super(new DiffUtil.ItemCallback<CommentEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull CommentEntity oldItem, @NonNull CommentEntity newItem) {
                return oldItem.id == newItem.id;
            }
            @Override
            public boolean areContentsTheSame(@NonNull CommentEntity oldItem, @NonNull CommentEntity newItem) {
                return oldItem.content.equals(newItem.content);
            }
        });
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CommentEntity c = getItem(position);
        holder.tvUser.setText(c.authorName);
        holder.tvContent.setText(c.content);
        if (c.authorPicPath != null && !c.authorPicPath.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(c.authorPicPath)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUser, tvContent;
        VH(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivCommentAvatar);
            tvUser = v.findViewById(R.id.tvCommentUsername);
            tvContent = v.findViewById(R.id.tvCommentContent);
        }
    }
}
