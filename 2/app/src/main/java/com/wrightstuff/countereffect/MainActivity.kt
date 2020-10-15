package com.wrightstuff.countereffect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) //For demo purposes, lets keep the screen on

        setContentView(R.layout.activity_main)
        btn_random.setOnClickListener {
            Random(System.currentTimeMillis()).nextInt(counter.minValue, counter.maxValue).also {
                Toast.makeText(this, getString(R.string.txt_animating, it), Toast.LENGTH_SHORT).show()
                counter.animateToValue(it)
            }
        }
        btn_example.setOnClickListener {
            counter.value = 256
            Handler().postDelayed({ //intentionally delayed for better effect
                counter.animateToValue(294)
            }, 500)
        }
    }
}