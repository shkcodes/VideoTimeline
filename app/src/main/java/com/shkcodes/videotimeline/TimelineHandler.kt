package com.shkcodes.videotimeline

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fueled.reclaim.ItemsViewAdapter
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.shkcodes.videotimeline.items.VideoSegmentAdapterItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


/**
 * Created by shashank@fueled.com on 19/09/20.
 */
class TimelineHandler(
    context: Context,
    private val videos: List<String>,
    private val player: Player,
    private val timeline: RecyclerView
) : Player.EventListener {

    companion object {
        const val SEGMENT_MULTIPLIER = 50F
        const val PLAYBACK_POLL_DURATION = 50L //ms
        const val MILLIS_TO_SECONDS_MULTIPLIER = 0.001F
        const val END_OFFSET_BUFFER = 50
    }

    private val segmentBackgrounds = context.resources.getIntArray(R.array.segment_backgrounds)
    private val timelineAdapter = ItemsViewAdapter()
    private val halfScreenWidth = context.screenWidth / 2
    private val layoutManager = timeline.layoutManager as LinearLayoutManager
    private val durationList = mutableListOf<Duration>()
    private var disposable: Disposable? = null

    fun init() {
        with(timeline) {
            adapter = timelineAdapter
            setPadding(halfScreenWidth, 0, halfScreenWidth, 0)
        }
        player.addListener(this)
        timeline.addOnScrollListener(seekListener)
    }


    private val seekListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING && player.isPlaying) {
                player.playWhenReady = false
                disposable?.dispose()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!player.isPlaying) seekPlayer()
        }
    }

    private fun seekPlayer() {
        val (index, offset) = positionAndOffsetFromCenter()
        val segmentDuration = durationList[index].millis
        val isAtTheEnd = index == durationList.size - 1 && offset == 1F
        val millis = if (isAtTheEnd) {
            offset * segmentDuration - END_OFFSET_BUFFER
        } else {
            offset * segmentDuration
        }
        player.seekTo(index, millis.roundToLong())
    }

    private fun positionAndOffsetFromCenter(): Pair<Int, Float> {
        val (index, offset) = timeline.findChildViewUnder(
            halfScreenWidth.toFloat(),
            timeline.pivotY
        )?.run {
            val position = timeline.getChildAdapterPosition(this)
            position to (halfScreenWidth - left).toFloat() / width
        } ?: 0 to 0F

        return index to offset
    }


    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        durationList.clear()
        val timeline = player.currentTimeline
        var totalTime = 0F
        val tempWindow = Timeline.Window()
        for (i in 0 until timeline.windowCount) {
            val windowDuration = timeline.getWindow(i, tempWindow).durationMs
            totalTime += windowDuration
            durationList.add(Duration(windowDuration))
        }
        if (totalTime > 0) {
            renderTimeline()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            observePlayback()
        } else {
            disposable?.dispose()
        }
    }

    private fun renderTimeline() {
        val segmentAdapterItems = videos.mapIndexed { i, video ->
            VideoSegmentAdapterItem(
                video,
                durationList[i].seconds * SEGMENT_MULTIPLIER,
                segmentBackgrounds[i]
            )
        }
        timelineAdapter.replaceItems(segmentAdapterItems, true)
    }

    private fun observePlayback() {
        disposable = timerObservable
            .subscribe(::scrollTimeline)
    }

    private val timerObservable: Observable<PlaybackInfo>
        get() =
            Observable.interval(PLAYBACK_POLL_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    val currentPosition = player.currentPosition
                    val currentWindowIndex = player.currentWindowIndex
                    PlaybackInfo(currentPosition, currentWindowIndex)
                }

    private fun scrollTimeline(info: PlaybackInfo) {
        val window = info.window
        val position = info.position
        val windowOffset = durationList.subList(0, window).map { it.seconds }.sum()
        val positionInSeconds = position * MILLIS_TO_SECONDS_MULTIPLIER
        val scrollPosition = (windowOffset + positionInSeconds) * SEGMENT_MULTIPLIER
        layoutManager.scrollToPositionWithOffset(
            0,
            -scrollPosition.toInt()
        )
    }
}