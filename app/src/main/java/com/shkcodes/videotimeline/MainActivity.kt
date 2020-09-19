package com.shkcodes.videotimeline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mediaPlayer = createMediaPlayer(this)
        exo_player.apply {
            setShowMultiWindowTimeBar(true)
            player = mediaPlayer
            showController()
        }
        val timelineHandler = TimelineHandler(this, videoPaths, mediaPlayer, timeline)
        timelineHandler.init()
    }
}