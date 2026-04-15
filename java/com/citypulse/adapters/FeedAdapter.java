package com.citypulse.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.citypulse.R;
import com.citypulse.local.entity.PostEntity;
import com.citypulse.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import java.util.List;

public class FeedAdapter extends androidx.recyclerview.widget.ListAdapter<PostEntity, FeedAdapter.VH> {

    public interface OnPostActionListener {
        void onLike(PostEntity post);
        void onComment(PostEntity post);
        void onShare(PostEntity post);
    }
    
    private OnPostActionListener listener;
    public void setOnPostActionListener(OnPostActionListener l) { listener = l; }

    private final Context ctx;

    public FeedAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<PostEntity>() {
            @Override public boolean areItemsTheSame(@NonNull PostEntity a, @NonNull PostEntity b) {
                return a.id == b.id;
            }
            @Override public boolean areContentsTheSame(@NonNull PostEntity a, @NonNull PostEntity b) {
                return a.likeCount == b.likeCount && a.isLiked == b.isLiked && a.commentCount == b.commentCount;
            }
        });
        this.ctx = ctx;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_post, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PostEntity p = getItem(pos);
        h.tvUsername.setText(p.authorName);
        h.tvCity.setText(p.city + " · " + TimeUtils.getRelativeTime(p.createdAt));
        h.tvDesc.setText(p.description);
        h.tvLikes.setText(String.valueOf(p.likeCount));
        h.tvComments.setText(String.valueOf(p.commentCount));

        if (p.location != null && !p.location.isEmpty()) {
            h.tvSpecificLocation.setVisibility(View.VISIBLE);
            h.tvSpecificLocation.setText(p.location);
        } else {
            h.tvSpecificLocation.setVisibility(View.GONE);
        }

        // Author pic
        if (p.authorPicPath != null && !p.authorPicPath.isEmpty())
            Glide.with(ctx).load(p.authorPicPath).circleCrop().placeholder(R.drawable.ic_person_placeholder).into(h.ivAvatar);
        else
            h.ivAvatar.setImageResource(R.drawable.ic_person_placeholder);

        // Media
        if (p.mediaPath != null && !p.mediaPath.isEmpty()) {
            h.mediaContainer.setVisibility(View.VISIBLE);
            boolean isVideo = "video".equals(p.mediaType);
            h.ivPlayButton.setVisibility(isVideo ? View.VISIBLE : View.GONE);
            
            Uri uri;
            if (p.mediaPath.startsWith("content://")) {
                uri = Uri.parse(p.mediaPath);
            } else {
                try {
                    File mediaFile = new File(p.mediaPath);
                    uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", mediaFile);
                } catch (Exception e) {
                    uri = Uri.parse(p.mediaPath);
                }
            }

            Glide.with(ctx)
                .load(uri)
                .centerCrop()
                .placeholder(R.color.cp_bg_elevated)
                .into(h.ivMedia);

            if (isVideo) {
                Uri finalUri = uri;
                h.mediaContainer.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(finalUri, "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        ctx.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(ctx, "Could not open video player", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                h.mediaContainer.setOnClickListener(null);
            }
        } else {
            h.mediaContainer.setVisibility(View.GONE);
        }

        // Badge
        setBadge(h.tvBadge, p.intensity);

        // Like icon
        h.btnLike.setImageResource(p.isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        h.btnLike.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.heart_bounce));
            if (listener != null) listener.onLike(p);
        });

        h.btnComment.setOnClickListener(v -> {
            if (listener != null) listener.onComment(p);
        });

        h.btnShare.setOnClickListener(v -> {
            if (listener != null) listener.onShare(p);
        });
    }

    private void setBadge(TextView tv, String intensity) {
        int bg, fg; String label;
        switch (intensity == null ? "" : intensity) {
            case "urgent":       bg = R.color.cp_urgent_dim;    fg = R.color.cp_urgent;    label = "URGENT";    break;
            case "important":    bg = R.color.cp_important_dim; fg = R.color.cp_important; label = "IMPORTANT"; break;
            case "promotion":    bg = R.color.cp_promo_dim;     fg = R.color.cp_promo;     label = "PROMO";     break;
            case "announcement": bg = R.color.cp_announce_dim;  fg = R.color.cp_announce;  label = "ANNOUNCE";  break;
            default:             bg = R.color.cp_bg_elevated;   fg = R.color.cp_text_muted;label = "UPDATE";    break;
        }
        tv.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bg)));
        tv.setTextColor(ContextCompat.getColor(ctx, fg));
        tv.setText(label);
    }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUsername, tvCity, tvDesc, tvLikes, tvComments, tvBadge, tvSpecificLocation;
        ImageView ivMedia, ivPlayButton, btnLike, btnComment, btnShare;
        View mediaContainer;

        VH(@NonNull View v) {
            super(v);
            ivAvatar      = v.findViewById(R.id.ivPostAvatar);
            tvUsername    = v.findViewById(R.id.tvPostUsername);
            tvCity        = v.findViewById(R.id.tvPostCity);
            tvSpecificLocation = v.findViewById(R.id.tvPostSpecificLocation);
            tvDesc        = v.findViewById(R.id.tvPostDescription);
            tvLikes       = v.findViewById(R.id.tvLikeCount);
            tvComments    = v.findViewById(R.id.tvCommentCount);
            tvBadge       = v.findViewById(R.id.tvIntensityBadge);
            ivMedia       = v.findViewById(R.id.ivPostMedia);
            ivPlayButton  = v.findViewById(R.id.ivPlayButton);
            mediaContainer= v.findViewById(R.id.mediaContainer);
            btnLike       = v.findViewById(R.id.btnLike);
            btnComment    = v.findViewById(R.id.btnComment);
            btnShare      = v.findViewById(R.id.btnShare);
        }
    }
}
