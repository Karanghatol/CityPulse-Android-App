package com.citypulse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.citypulse.R;
import com.citypulse.activities.CreatePostActivity;
import com.citypulse.activities.MainActivity;
import com.citypulse.activities.NotificationActivity;
import com.citypulse.adapters.CommentsAdapter;
import com.citypulse.adapters.FeedAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.CommentEntity;
import com.citypulse.local.entity.NotificationEntity;
import com.citypulse.local.entity.PostEntity;
import com.citypulse.local.entity.PostLikeEntity;
import com.citypulse.utils.SessionManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.concurrent.Executors;

public class FeedFragment extends Fragment {

    private FeedAdapter adapter;
    private AppDatabase db;
    private SessionManager sm;

    private TextView tvHeaderName, tvHeaderCity;
    private CircleImageView ivUserAvatar;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_feed, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sm = SessionManager.get();
        db = AppDatabase.get(requireContext());

        // ✅ Dynamic Header
        tvHeaderName = view.findViewById(R.id.tvHeaderName);
        tvHeaderCity = view.findViewById(R.id.tvHeaderCity);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);

        updateHeaderUI();

        // Navigate to Profile on avatar click
        ivUserAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigationView);
                nav.setSelectedItemId(R.id.nav_profile);
            }
        });

        RecyclerView rv = view.findViewById(R.id.rvFeed);
        ShimmerFrameLayout shimmer = view.findViewById(R.id.shimmerFeed);
        View notifBadge = view.findViewById(R.id.notifBadge);

        adapter = new FeedAdapter(requireContext());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // ✅ Observe Notifications
        db.notificationDao().getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            notifBadge.setVisibility(count != null && count > 0 ? View.VISIBLE : View.GONE);
        });

        view.findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificationActivity.class));
        });

        // ✅ Observe Feed - Showing ALL posts instead of filtering by city
        db.postDao().getAllFeed(sm.getUserId(), System.currentTimeMillis()).observe(getViewLifecycleOwner(), posts -> {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            adapter.submitList(posts);
            
            // Clean up expired posts in background
            Executors.newSingleThreadExecutor().execute(() -> {
                db.postDao().deleteExpiredPosts(System.currentTimeMillis());
            });
        });

        // ✅ Action Listeners
        adapter.setOnPostActionListener(new FeedAdapter.OnPostActionListener() {
            @Override
            public void onLike(PostEntity post) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    int userId = sm.getUserId();
                    boolean isCurrentlyLiked = db.postLikeDao().isLikedByUser(post.id, userId);
                    
                    if (isCurrentlyLiked) {
                        db.postLikeDao().delete(new PostLikeEntity(post.id, userId));
                        db.postDao().updateLikeCount(post.id, -1);
                    } else {
                        db.postLikeDao().insert(new PostLikeEntity(post.id, userId));
                        db.postDao().updateLikeCount(post.id, 1);

                        // Notification for post author
                        NotificationEntity n = new NotificationEntity();
                        n.title = "Post Liked";
                        n.message = sm.getUserName() + " liked your post: " + post.description;
                        n.type = "post_like";
                        n.postId = post.id;
                        n.senderName = sm.getUserName();
                        n.timestamp = System.currentTimeMillis();
                        db.notificationDao().insert(n);
                    }
                });
            }

            @Override
            public void onComment(PostEntity post) {
                showCommentsSheet(post);
            }

            @Override
            public void onShare(PostEntity post) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post.description + "\n\nShared via CityPulse");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share post to..."));
            }
        });

        view.findViewById(R.id.fabNewPost).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreatePostActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHeaderUI(); // Update UI when returning to this fragment
    }

    private void updateHeaderUI() {
        if (tvHeaderName != null) tvHeaderName.setText(sm.getUserName());
        if (tvHeaderCity != null) tvHeaderCity.setText(sm.getUserCity());
        if (ivUserAvatar != null && !sm.getProfilePic().isEmpty()) {
            Glide.with(this)
                .load(new java.io.File(sm.getProfilePic()))
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .into(ivUserAvatar);
        }
    }

    private void showCommentsSheet(PostEntity post) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_comments_sheet, null);
        dialog.setContentView(view);

        RecyclerView rv = view.findViewById(R.id.rvComments);
        EditText et = view.findViewById(R.id.etComment);
        ImageButton btn = view.findViewById(R.id.btnSendComment);

        CommentsAdapter cAdapter = new CommentsAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(cAdapter);

        db.commentDao().getCommentsForPost(post.id).observe(this, comments -> {
            cAdapter.submitList(comments);
        });

        btn.setOnClickListener(v -> {
            String content = et.getText().toString().trim();
            if (content.isEmpty()) return;

            CommentEntity comment = new CommentEntity();
            comment.postId = post.id;
            comment.authorId = sm.getUserId();
            comment.authorName = sm.getUserName();
            comment.authorPicPath = sm.getProfilePic();
            comment.content = content;
            comment.createdAt = System.currentTimeMillis();

            Executors.newSingleThreadExecutor().execute(() -> {
                db.commentDao().insert(comment);
                
                // Notification for post author
                NotificationEntity n = new NotificationEntity();
                n.title = "New Comment";
                n.message = sm.getUserName() + " commented on your post: " + content;
                n.type = "post_comment";
                n.postId = post.id;
                n.senderName = sm.getUserName();
                n.timestamp = System.currentTimeMillis();
                db.notificationDao().insert(n);

                // Simple logic to increment comment count in PostEntity
                post.commentCount++;
                db.postDao().update(post);
                
                requireActivity().runOnUiThread(() -> {
                    et.setText("");
                    Toast.makeText(getContext(), "Comment added", Toast.LENGTH_SHORT).show();
                });
            });
        });

        dialog.show();
    }
}
