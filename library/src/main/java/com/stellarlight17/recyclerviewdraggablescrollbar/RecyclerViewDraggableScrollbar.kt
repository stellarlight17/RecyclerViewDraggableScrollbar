package com.stellarlight17.recyclerviewdraggablescrollbar

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max
import kotlin.math.min

class RecyclerViewDraggableScrollbar: RelativeLayout {
    private var trackView: CardView
    private var trackContentView: View
    private lateinit var thumbView: CardView
    private lateinit var thumbContentView: View

    private var thumbNormalColor = this.context.getColor(DEFAULT_THUMB_COLOR)
    private var thumbSelectedColor = this.context.getColor(DEFAULT_THUMB_SELECTED_COLOR)

    private var orientation = DEFAULT_ORIENTATION

    private var recyclerView: RecyclerView? = null
    private var moving = false
    private var thumbWidth = 0f
    private var thumbHeight = 0f

    private var minY = 0f
    private var maxY = -1f

    private var minX = 0f
    private var maxX = -1f

    private var hideDelay = 1000L
    private var hideTimer: Timer? = null

    private val thumbTouchListener = object: View.OnTouchListener {
        private var lastRawX: Float = 0f
        private var lastRawY: Float = 0f

        override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
            this@RecyclerViewDraggableScrollbar.cancelHideTimer()

            recyclerView?.let { recyclerView ->
                if (thumbWidth <= 0) thumbWidth = thumbView.right.toFloat() - thumbView.left
                if (thumbHeight <= 0) thumbHeight = thumbView.bottom.toFloat() - thumbView.top
                if (maxY < 0) maxY = this@RecyclerViewDraggableScrollbar.height.toFloat() - thumbHeight
                if (maxX < 0) maxX = this@RecyclerViewDraggableScrollbar.width.toFloat() - thumbWidth

                when (p1?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        moving = true
                        this@RecyclerViewDraggableScrollbar.thumbContentView.setBackgroundColor(
                            this@RecyclerViewDraggableScrollbar.thumbSelectedColor)
                        this.lastRawX = p1.rawX
                        this.lastRawY = p1.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (moving) { this.computeThumbPosition(recyclerView, p1) }
                    }
                    MotionEvent.ACTION_UP -> {
                        moving = false
                        this@RecyclerViewDraggableScrollbar.thumbContentView.setBackgroundColor(
                            this@RecyclerViewDraggableScrollbar.thumbNormalColor)
                        this@RecyclerViewDraggableScrollbar.setHideTimer()
                    }
                    else -> {}
                }
                return moving
            } ?: run { return false }
        }

        private fun computeThumbPosition(recyclerView: RecyclerView, p1: MotionEvent) {
            if (this@RecyclerViewDraggableScrollbar.orientation == VERTICAL) this.computeThumbVerticalPosition(recyclerView, p1)
            else this.computeThumbHorizontalPosition(recyclerView, p1)
        }

        private fun computeThumbVerticalPosition(recyclerView: RecyclerView, p1: MotionEvent) {
            // compute new thumb position
            val y = thumbView.y
            val dy = p1.rawY - this.lastRawY
            thumbView.y = max(minY, min(y + dy, maxY))
            this.lastRawY = p1.rawY

            // scroll to position
            val itemCount = recyclerView.adapter?.itemCount ?: 0
            //val thumbPosition = if (dy < 0) thumbView.y else thumbView.y + thumbHeight
            //val position = Math.min(itemCount - 1, (thumbPosition * itemCount /
            //    this@DraggableVerticalScrollbarView.height.toFloat()).toInt())
            val thumbPosition = thumbView.y
            //val containerSize = this@DraggableVerticalScrollbarView.height.toFloat() - thumbHeight
            val containerSize = maxY
            val position = min(itemCount - 1, (thumbPosition * itemCount / containerSize).toInt())
            recyclerView.scrollToPosition(position)
        }

        private fun computeThumbHorizontalPosition(recyclerView: RecyclerView, p1: MotionEvent) {
            // compute new thumb position
            val x = thumbView.x
            val dx = p1.rawX - this.lastRawX
            thumbView.x = max(minX, min(x + dx, maxX))
            this.lastRawX = p1.rawX

            // scroll to position
            val itemCount = recyclerView.adapter?.itemCount ?: 0
            val thumbPosition = thumbView.x
            val containerSize = maxX
            val position = min(itemCount - 1, (thumbPosition * itemCount / containerSize).toInt())
            recyclerView.scrollToPosition(position)
        }
    }

    private val recyclerViewScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) this@RecyclerViewDraggableScrollbar.setHideTimer()
            else this@RecyclerViewDraggableScrollbar.cancelHideTimer()
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val isVertical = this@RecyclerViewDraggableScrollbar.orientation == VERTICAL
            if (moving || (isVertical && dy == 0) || (!isVertical && dx == 0)) return

            val offset: Int
            val extent: Int
            val contentSize: Int

            if (isVertical) {
                offset = recyclerView.computeVerticalScrollOffset()
                extent = recyclerView.computeVerticalScrollExtent()
                contentSize = recyclerView.computeVerticalScrollRange() - extent
            } else {
                offset = recyclerView.computeHorizontalScrollOffset()
                extent = recyclerView.computeHorizontalScrollExtent()
                contentSize = recyclerView.computeHorizontalScrollRange() - extent
            }
            this@RecyclerViewDraggableScrollbar.updateVisibility(contentSize > extent)

            // calculate thumb position
            if (isVertical) {
                if (thumbHeight <= 0) thumbHeight = (thumbView.bottom.toFloat() - thumbView.top)
                if (maxY < 0) maxY = this@RecyclerViewDraggableScrollbar.height - thumbHeight
                thumbView.y = offset.toFloat() * maxY / contentSize
            } else {
                if (thumbWidth <= 0) thumbWidth = (thumbView.right.toFloat() - thumbView.left)
                if (maxX < 0) maxX = this@RecyclerViewDraggableScrollbar.width - thumbWidth
                thumbView.x = offset.toFloat() * maxX / contentSize
            }
        }
    }

    constructor(context: Context): super(context) { }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { this.setAttributes(attrs) }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        this.setAttributes(attrs)
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView?.let { throw Exception("Already attached to a recycler view") }
        this.recyclerView = recyclerView
        (this.recyclerView?.layoutManager as? LinearLayoutManager)?.orientation?.takeIf { it != this.orientation }?.let {
            this.orientation = it
            when (this.orientation) {
                VERTICAL -> {
                    arrayOf(this.thumbView, this.trackView).forEach {
                        (it.layoutParams as RelativeLayout.LayoutParams).apply {
                            this.removeRule(ALIGN_PARENT_BOTTOM)
                            this.removeRule(ALIGN_PARENT_START)
                            this.addRule(ALIGN_PARENT_TOP)
                            this.addRule(ALIGN_PARENT_END)
                        }
                    }
                }
                HORIZONTAL -> {
                    arrayOf(this.thumbView, this.trackView).forEach {
                        (it.layoutParams as RelativeLayout.LayoutParams).apply {
                            this.removeRule(ALIGN_PARENT_TOP)
                            this.removeRule(ALIGN_PARENT_END)
                            this.addRule(ALIGN_PARENT_BOTTOM)
                            this.addRule(ALIGN_PARENT_START)
                        }
                    }
                }
            }
        }
        this.thumbView.setOnTouchListener(this.thumbTouchListener)
        this.recyclerView!!.addOnScrollListener(this.recyclerViewScrollListener)
        this.updateVisibility(false)
    }

    fun dettach() {
        this.recyclerView = null
        this.thumbView.setOnTouchListener(null)
        this.recyclerView?.removeOnScrollListener(this.recyclerViewScrollListener)
    }

    private fun updateVisibility(visible: Boolean) {
        //this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        if (visible) this.showWithAnimation() else this.visibility = View.INVISIBLE
    }

    private fun setAttributes(attrs: AttributeSet) {
        val defaultTrackColor = this.context.getColor(DEFAULT_TRACK_COLOR)
        val defaultThumbColor = this.context.getColor(DEFAULT_THUMB_COLOR)
        val defaultThumbSelectedColor = this.context.getColor(DEFAULT_THUMB_SELECTED_COLOR)

        this.context.theme.obtainStyledAttributes(attrs, R.styleable.RecyclerViewDraggableScrollbar, 0, 0).apply {
            try {
                this.getDimension(R.styleable.RecyclerViewDraggableScrollbar_trackWidth, DEFAULT_TRACK_WIDTH).also {
                    this@RecyclerViewDraggableScrollbar.trackView.layoutParams.width = it.toInt()
                }

                this.getColor(R.styleable.RecyclerViewDraggableScrollbar_trackColor, defaultTrackColor).also {
                    //this@RecyclerViewDraggableScrollbar.trackView.setBackgroundColor(it)
                    this@RecyclerViewDraggableScrollbar.trackContentView.setBackgroundColor(it)
                }

                this.getDimension(R.styleable.RecyclerViewDraggableScrollbar_trackCornerRadius, DEFAULT_TRACK_CORNER_RADIUS).also {
                    this@RecyclerViewDraggableScrollbar.trackView.radius = it
                }

                this.getDimension(R.styleable.RecyclerViewDraggableScrollbar_thumbWidth, DEFAULT_THUMB_WIDTH).also {
                    this@RecyclerViewDraggableScrollbar.thumbView.layoutParams.width = it.toInt()
                }

                this.getDimension(R.styleable.RecyclerViewDraggableScrollbar_thumbHeight, DEFAULT_THUMB_HEIGHT).also {
                    this@RecyclerViewDraggableScrollbar.thumbView.layoutParams.height = it.toInt()
                }

                this.getColor(R.styleable.RecyclerViewDraggableScrollbar_thumbColor, defaultThumbColor).also {
                    //this@RecyclerViewDraggableScrollbar.thumbView.findViewById<View>(R.id.thumbContentView).setBackgroundColor(it)
                    this@RecyclerViewDraggableScrollbar.thumbNormalColor = it
                    this@RecyclerViewDraggableScrollbar.thumbContentView.setBackgroundColor(it)
                }

                this.getColor(R.styleable.RecyclerViewDraggableScrollbar_thumbSelectedColor, defaultThumbSelectedColor).also {
                    this@RecyclerViewDraggableScrollbar.thumbSelectedColor = it
                }

                this.getDimension(R.styleable.RecyclerViewDraggableScrollbar_thumbCornerRadius, DEFAULT_THUMB_CORNER_RADIUS).also {
                    this@RecyclerViewDraggableScrollbar.thumbView.radius = it
                }
            } finally { this.recycle() }
        }
    }

    private fun cancelHideTimer() {
        this.hideTimer?.cancel()
        this.hideTimer = null
    }

    private fun setHideTimer() {
        this.cancelHideTimer()
        this.hideTimer = Timer().apply { this.schedule(object: TimerTask() {
            override fun run() { this@RecyclerViewDraggableScrollbar.hideWithAnimation() }
        }, this@RecyclerViewDraggableScrollbar.hideDelay) }
    }

    private fun hideWithAnimation() {
        AnimationUtils.loadAnimation(this.context, android.R.anim.fade_out).apply {
            this.setAnimationListener(object: AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    this@RecyclerViewDraggableScrollbar.visibility = View.INVISIBLE
                }
                override fun onAnimationRepeat(p0: Animation?) {}
            })
            this@RecyclerViewDraggableScrollbar.startAnimation(this)
        }
    }

    private fun showWithAnimation() {
        if (this.visibility == View.VISIBLE) return
        this.visibility = View.VISIBLE
        this.startAnimation(AnimationUtils.loadAnimation(this.context, android.R.anim.fade_in).apply { this.duration = 200 })
    }

    init {
        inflate(this.context, R.layout.view_recyclerview_draggable_scrollbar, this)
        this.trackView = this.findViewById(R.id.trackView)
        this.trackContentView = this.findViewById(R.id.trackContentView)
        this.thumbView = this.findViewById(R.id.thumbView)
        this.thumbContentView = this.thumbView.findViewById(R.id.thumbContentView)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @IntDef(*[RecyclerViewDraggableScrollbar.HORIZONTAL, RecyclerViewDraggableScrollbar.VERTICAL])
    @kotlin.annotation.Retention
    annotation class Orientation {}

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL
        const val DEFAULT_ORIENTATION = VERTICAL

        const val DEFAULT_TRACK_WIDTH = 20f
        const val DEFAULT_TRACK_COLOR = android.R.color.darker_gray
        const val DEFAULT_TRACK_CORNER_RADIUS = 0f

        const val DEFAULT_THUMB_WIDTH = 20f
        const val DEFAULT_THUMB_HEIGHT = 50f
        const val DEFAULT_THUMB_COLOR = android.R.color.holo_red_dark
        const val DEFAULT_THUMB_SELECTED_COLOR = android.R.color.holo_blue_bright
        const val DEFAULT_THUMB_CORNER_RADIUS = 0f
    }
}