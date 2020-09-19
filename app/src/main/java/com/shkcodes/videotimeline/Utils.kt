package com.shkcodes.videotimeline

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File


/**
 * Created by shashank@fueled.com on 19/09/20.
 */

val videoFiles = listOf(
    File(
        Environment.getExternalStorageDirectory(),
        "/Download/video_a.mp4"
    ), File(
        Environment.getExternalStorageDirectory(),
        "/Download/video_b.mp4"
    ), File(
        Environment.getExternalStorageDirectory(),
        "/Download/video_c.mp4"
    )
)

val videoPaths = videoFiles.map { it.path }

fun createMediaPlayer(context: Context): SimpleExoPlayer {

    val dataSourceFactory = DefaultDataSourceFactory(
        context,
        Util.getUserAgent(context, "VideoTimeline")
    )
    val videoSources = videoFiles.map {
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(
                Uri.fromFile(it)
            )
    }

    val concatenatedSource = ConcatenatingMediaSource().apply { addMediaSources(videoSources) }
    return SimpleExoPlayer.Builder(context).build().apply {
        prepare(concatenatedSource)
        repeatMode = Player.REPEAT_MODE_ALL
        playWhenReady = true
    }
}

inline class Duration(val millis: Long) {
    val seconds: Float
        get() = millis / 1000F

}

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

data class PlaybackInfo(val position: Long, val window: Int)
