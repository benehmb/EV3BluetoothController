package de.benehmb.ev3bluetoothcontroller

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar

/**
 * Implementation of an easy vertical SeekBar, based on the normal SeekBar.
 *
 * https://github.com/jeisfeld/Augendiagnose/blob/master/AugendiagnoseIdea/augendiagnoseLib/src/main/java/de/jeisfeld/augendiagnoselib/components/VerticalSeekBar.java
 */
class VerticalSeekBar : SeekBar {

    private var mOnSeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null

    var minSeekBarProgress = 0
        set(value) {
            max += value
            progress += value
            field = value
        }

    var seekBarProgress
        get() = progress - minSeekBarProgress
        set(value) {
            progress = value + minSeekBarProgress
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(height, width, oldHeight, oldWidth)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90F)
        c.translate((-height).toFloat(), 0f)

        super.onDraw(c)
    }

    override fun setOnSeekBarChangeListener(listener: SeekBar.OnSeekBarChangeListener) {
        mOnSeekBarChangeListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setProgressInternally(max - (max * (event.y - paddingRight) / (height - paddingRight - paddingLeft)).toInt(), true)
                mOnSeekBarChangeListener?.onStartTrackingTouch(this)
            }

            MotionEvent.ACTION_MOVE ->
                setProgressInternally(max - (max * (event.y - paddingRight) / (height - paddingRight - paddingLeft)).toInt(), true)

            MotionEvent.ACTION_UP -> {
                setProgressInternally(max - (max * (event.y - paddingRight) / (height - paddingRight - paddingLeft)).toInt(), true)
                mOnSeekBarChangeListener?.onStopTrackingTouch(this)
            }

            MotionEvent.ACTION_CANCEL ->
                mOnSeekBarChangeListener?.onStopTrackingTouch(this)
        }

        return true
    }

    private fun setProgressInternally(progress: Int, fromUser: Boolean) {
        if (progress != getProgress()) {
            super.setProgress(progress)
            mOnSeekBarChangeListener?.onProgressChanged(this, seekBarProgress, fromUser)
        }
        onSizeChanged(width, height, 0, 0)
    }

    override fun setProgress(progress: Int) {
        setProgressInternally(progress, false)
    }
}