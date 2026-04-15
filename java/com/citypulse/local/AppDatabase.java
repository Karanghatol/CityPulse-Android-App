package com.citypulse.local;

import android.content.Context;
import androidx.room.*;
import com.citypulse.local.dao.*;
import com.citypulse.local.entity.*;

@Database(
    entities  = {NotificationEntity.class, UserEntity.class, ApplicationEntity.class, PostEntity.class, JobEntity.class, GroupEntity.class, MessageEntity.class, CommentEntity.class, GroupMemberEntity.class, PostLikeEntity.class},
    version   = 12,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ApplicationDao applicationDao();

    public abstract NotificationDao notificationDao();
    public abstract UserDao  userDao();
    public abstract PostDao  postDao();
    public abstract JobDao   jobDao();
    public abstract GroupDao groupDao();
    public abstract CommentDao commentDao();
    public abstract MessageDao messageDao();
    public abstract GroupMemberDao groupMemberDao();
    public abstract PostLikeDao postLikeDao();

    public static AppDatabase get(Context ctx) {

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            AppDatabase.class,
                            "citypulse.db")
                            .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();

                }
            }
        }
        return INSTANCE;
    }
}
