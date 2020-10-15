package com.wrightstuff.gradient

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


/**
 * The challenge is to animate a gradient red-blue vertical to ti green to yellow vertical with a 2 second fixed timing.
 * Its achieved here with a animation list.
 * The 2 second fixed timing is done via the fade duration enter and exit being 1 second each.
 * There are other possible routes to achieve the same result. However this is possibly the simplest with minimal amount of code.
 * For example making use of TimeAnimator with an ArgbEvaluator : @see https://stackoverflow.com/a/51007924
 * And of course there are countless libraries to make use of or build upon: @see https://github.com/Gradients/awesome-gradient#kotlin
 * This achieves the challenge in a straightforward way.
 * */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_change_gradient.setOnClickListener {
            applyGradientChangeContinuous()
        }
        btn_change_once.setOnClickListener {
            applyGradientChangeOnce()
        }
    }

    private fun applyGradientChangeOnce() {
        view_gradient.background = null
        view_gradient.background = ContextCompat.getDrawable(this, R.drawable.gradient_list)
        val gradient = view_gradient.background as AnimationDrawable
        gradient.stop()
        gradient.setEnterFadeDuration(1000)
        gradient.setExitFadeDuration(1000) //2 seconds as fade in is 1 and fadeout is 1
        gradient.isOneShot = true
        gradient.start()
    }

    private fun applyGradientChangeContinuous() {
        val gradient = view_gradient.background as AnimationDrawable
        if (!gradient.isRunning) {
            gradient.isOneShot = false
            gradient.setEnterFadeDuration(1000)//2 seconds as fade in is 1 and fadeout is 1
            gradient.setExitFadeDuration(1000)
            gradient.start()
            Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Stopping...", Toast.LENGTH_SHORT).show()
            gradient.stop()
        }


    }
}