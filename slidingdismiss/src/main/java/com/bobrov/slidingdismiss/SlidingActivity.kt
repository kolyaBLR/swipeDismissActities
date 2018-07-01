package com.bobrov.slidingdismiss

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View

public abstract class SlidingActivity : AppCompatActivity() {

    private lateinit var root: View
    private var startX = 0f
    private var startY = 0f
    private var isSliding = false
    private val gestureThreshold = 10
    private lateinit var screenSize: Point

    open var durationClose = 200L

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        root = getRootView()
        screenSize = Point().apply { windowManager.defaultDisplay.getSize(this) }
    }

    abstract fun getRootView(): View

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val isSlidingEvent = when (direction()) {
                    Direction.TOP_TO_BOTTOM -> isSlidingDown(startY, event) && isActiveSliding()
                    Direction.BOTTOM_TO_TOP -> isSlidingTop(screenSize.y.toFloat(), event) && isActiveSliding()
                    else -> throw DirectionException()
                }
                if (isSlidingEvent || isSliding) {
                    if (!isSliding) {
                        isSliding = true
                        onSlidingStarted()
                        event.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(event)
                    }
                    handled = true
                    root.y = when (direction()) {
                        Direction.TOP_TO_BOTTOM -> (event.y - startY).coerceAtLeast(0f)
                        Direction.BOTTOM_TO_TOP -> (event.y - startY).coerceAtMost(0f)
                        else -> throw DirectionException()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isSliding) {
                    isSliding = false
                    handled = true
                    val isClose = when (direction()) {
                        Direction.TOP_TO_BOTTOM -> shouldClose(event.y - startY)
                        Direction.BOTTOM_TO_TOP -> shouldClose(startY - event.y)
                        else -> throw DirectionException()
                    }
                    if (isClose) finishDrag() else root.y = 0f
                    onSlidingFinished(isClose)
                }
                startX = 0f
                startY = 0f
            }
        }
        return if (handled) true else super.dispatchTouchEvent(event)
    }

    fun finishDrag() {
        when (direction()) {
            Direction.TOP_TO_BOTTOM -> finishDrag(root.y, screenSize.y.toFloat())
            Direction.BOTTOM_TO_TOP -> finishDrag(root.y, screenSize.y.toFloat() * -1)
            else -> throw DirectionException()
        }
    }

    private fun finishDrag(start: Float, finish: Float) {
        val positionAnimator = ObjectAnimator.ofFloat(root, "y", start, finish)
        positionAnimator.duration = durationClose
        positionAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        positionAnimator.start()
    }

    private fun isSlidingDown(startY: Float, ev: MotionEvent) = ev.y - startY > gestureThreshold
    private fun isSlidingTop(endY: Float, ev: MotionEvent) = endY - ev.y > gestureThreshold

    private fun shouldClose(delta: Float) = delta > screenSize.y / 3

    open fun direction() = Direction.TOP_TO_BOTTOM
    open fun isActiveSliding() = true
    open fun onSlidingStarted() {

    }

    open fun onSlidingFinished(isClosed: Boolean) {

    }

    public enum class Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
    }
}