package com.shkcodes.videotimeline.items

import android.view.View
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.videotimeline.R
import kotlin.math.roundToInt


/**
 * Created by shashank@fueled.com on 19/09/20.
 */
class VideoSegmentAdapterItem(
    private val video: String,
    private val width: Float,
    private val color: Int
) : AdapterItem<VideoSegmentViewHolder>() {

    override val layoutId = R.layout.item_video_segment

    override fun onCreateViewHolder(view: View) = VideoSegmentViewHolder(view)

    override fun updateItemViews(viewHolder: VideoSegmentViewHolder) {
        viewHolder.itemView.layoutParams.width = width.roundToInt()
        viewHolder.itemView.setBackgroundColor(color)
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is VideoSegmentAdapterItem && newItem.video == video && newItem.width == width
    }

    override fun isContentsTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is VideoSegmentAdapterItem && newItem.width == width && newItem.color == color
    }

}

class VideoSegmentViewHolder(view: View) : BaseViewHolder(view)