package com.bobrov.draganddismiss

import android.os.Bundle
import android.view.View
import com.bobrov.slidingdismiss.SlidingActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : SlidingActivity() {

    private var direction = SlidingActivity.Direction.BOTTOM_TO_TOP

    override var durationClose = 300L

    override fun getRootView() = rootView!!
    override fun direction() = direction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        textView.setOnClickListener {
            finishDrag()
        }
        BTT.setOnClickListener { direction = SlidingActivity.Direction.BOTTOM_TO_TOP }
        TTB.setOnClickListener { direction = SlidingActivity.Direction.TOP_TO_BOTTOM }
        LTR.setOnClickListener { direction = SlidingActivity.Direction.LEFT_TO_RIGHT }
        RTL.setOnClickListener { direction = SlidingActivity.Direction.RIGHT_TO_LEFT }
    }
}
