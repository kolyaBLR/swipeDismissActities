package com.bobrov.draganddismiss

import android.os.Bundle
import com.bobrov.slidingdismiss.SlidingActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : SlidingActivity() {

    override var durationClose = 300L

    override fun getRootView() = rootView!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        textView.setOnClickListener {
            finishDrag()
        }
    }
}
