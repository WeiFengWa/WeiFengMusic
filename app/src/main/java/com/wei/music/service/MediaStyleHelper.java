package com.wei.music.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import com.wei.music.R;
import com.wei.music.activity.PlayerActivity;


public class MediaStyleHelper {
    
    public static NotificationCompat.Builder from(Context context, MediaSessionCompat mediaSession, String tag) {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();   
        Intent intent = new Intent(context, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, tag);
        builder
            .setContentTitle(description.getTitle())
            .setContentText(description.getSubtitle())
            .setSmallIcon(R.drawable.ic_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                      .setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2))
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
            .setContentIntent(pendingIntent)
            .setOngoing(true);
        return builder;
    }
}

