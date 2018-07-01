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

    private val X = "x"
    private val Y = "y"

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
                val isSlidingEvent = isActiveSliding() && when (direction()) {
                    Direction.TOP_TO_BOTTOM -> isSlidingDown(startY, event)
                    Direction.BOTTOM_TO_TOP -> isSlidingTop(screenSize.y.toFloat(), event)
                    Direction.LEFT_TO_RIGHT -> isSlidingRight(startX, event)
                    Direction.RIGHT_TO_LEFT -> isSlidingLeft(screenSize.x.toFloat(), event)
                }
                if (isSlidingEvent || isSliding) {
                    if (!isSliding) {
                        isSliding = true
                        onSlidingStarted()
                        event.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(event)
                    }
                    handled = true
                    when (direction()) {
                        Direction.TOP_TO_BOTTOM -> root.y = (event.y - startY).coerceAtLeast(0f)
                        Direction.BOTTOM_TO_TOP -> root.y = (event.y - startY).coerceAtMost(0f)
                        Direction.LEFT_TO_RIGHT -> root.x = (event.x - startX).coerceAtLeast(0f)
                        Direction.RIGHT_TO_LEFT -> root.x = (event.x - startX).coerceAtMost(0f)
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
                        Direction.LEFT_TO_RIGHT -> shouldClose(event.x - startX)
                        Direction.RIGHT_TO_LEFT -> shouldClose(startX - event.x)
                    }
                    if (isClose) finishDrag() else {
                        root.y = 0f
                        root.x = 0f
                    }
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
            Direction.TOP_TO_BOTTOM -> finishDrag(root.y, screenSize.y.toFloat(), Y)
            Direction.BOTTOM_TO_TOP -> finishDrag(root.y, screenSize.y.toFloat() * -1, Y)
            Direction.LEFT_TO_RIGHT -> finishDrag(root.x, screenSize.x.toFloat(), X)
            Direction.RIGHT_TO_LEFT -> finishDrag(root.x, screenSize.x.toFloat() * -1, X)
        }
    }

    private fun finishDrag(start: Float, finish: Float, propertyName: String) {
        val positionAnimator = ObjectAnimator.ofFloat(root, propertyName, start, finish)
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
    private fun isSlidingRight(startX: Float, ev: MotionEvent) = ev.x - startX > gestureThreshold
    private fun isSlidingLeft(endX: Float, ev: MotionEvent) = endX - ev.x > gestureThreshold

    private fun shouldClose(delta: Float) = delta > screenSize.y / 3

    open fun direction() = Direction.TOP_TO_BOTTOM
    open fun isActiveSliding() = true
    open fun onSlidingStarted() {}

    open fun onSlidingFinished(isClosed: Boolean) {}

    public enum class Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
    }
}