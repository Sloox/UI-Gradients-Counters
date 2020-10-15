package com.wrightstuff.countereffect.counter

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import com.wrightstuff.countereffect.R
import com.wrightstuff.countereffect.logd
import com.wrightstuff.countereffect.loge
import kotlin.math.abs

/**
 * This animated counter is based upon the Number Picker widget provided by android. It achieves the challenge by 'forcibly' building upon the number picker widget
 * by using themes for styling and reflection for gaining access to the animation effects.
 *
 * Unfortunately Number Picker is fairly limited as to how much is customizable, therefore there is some
 * reflection included specifically for the animation that the challenge requires. NumberPicker handles the
 * animation via the private method call {@link NumberPicker#changeValueByOne}
 * As the name states it only caters for a single number change. Therefore the internal class {@see IncreasePicker} is used to
 * keep track of timing and value more than 1 value changes.
 * Reflection is also used to handle the focus changes of the EditText as it too is private and is required.
 *
 * To address the elephant in the room, is this the best way to achieve this?
 * It depends. The challenge was straight forward and the result is achieved.
 * It is a simple implementation and is easily reusable. The only consideration is the theme styling which needs to be taken into account in the xml.
 * The negative is that its very single focused solution and makes use of reflection to achieve its goals.
 *
 * Ultimately if this was a custom widget that needs to be expanded upon and greatly customized in future another route needs to be taken.
 * A better approach would be to create a custom widget based upon the NumberPicker source code. This then can be built upon and actual non reflection based
 * code that is robust can be built for its intended purpose.
 *
 * This has been done countless amounts of times for example:
 * @see https://github.com/ShawnLin013/NumberPicker
 * @see https://github.com/SuperRabbitD/NumberPicker
 * @see https://github.com/StephenVinouze/MaterialNumberPicker
 *
 * Each one of these libraries would achieve the goal set out by the challenge within 1-2 lines of code and possibly 1 style addition.
 *
 * Therefore this solution takes the route right down the middle, by neither just using the off the shelf solution but also not creating a full custom NumberPicker widget.
 * The down side of this, is the use of reflection. Be what it may.
 * */
class AnimatedCounter : NumberPicker {
    private var animatingLock = false
    private var newValue = this.value

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        processXmlAttributes(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        processXmlAttributes(attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        processXmlAttributes(attrs, defStyleAttr, defStyleRes)
    }

    override fun setValue(value: Int) {
        if (animatingLock) return
        super.setValue(value)
    }

    fun animateToValue(nextInt: Int) {
        if (animatingLock) return
        logd("Animating to :$nextInt , current val:${this.value} ")
        newValue = nextInt
        animatingLock = true
        val increase = IncreasePicker(this, newValue) {
            animatingLock = false
        }
        increase.start(0)
    }

    private fun processXmlAttributes(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.CounterNumberPicker, defStyleAttr, defStyleRes)
        try {
            this.selectionDividerHeight = 0
            this.minValue = attributes.getInt(R.styleable.CounterNumberPicker_minValue, 0)
            this.maxValue = attributes.getInt(R.styleable.CounterNumberPicker_maxValue, 0)
            this.value = attributes.getInt(R.styleable.CounterNumberPicker_defaultValue, 0)
            newValue = this.value
        } finally {
            attributes.recycle()
        }
        setupEditListeners()
    }

    private fun setupEditListeners() {
        getEditText()?.let {
            it.onFocusChangeListener = null
            it.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) it.selectAll() else {
                    it.setSelection(0, 0)
                    validateView(v)
                }
            }
        }
    }

    private fun validateView(v: View?) {
        val str = (v as TextView).text.toString()
        if (TextUtils.isEmpty(str)) {
            reUpdateInputTextView()
        } else {
            val current: Int? = str.toIntOrNull()
            current?.let { animateToValue(it) }
        }
    }

    private fun getEditText(): EditText? {
        return try {
            val field = javaClass.superclass.getDeclaredField("mInputText")
            field.isAccessible = true
            field.get(this) as EditText
        } catch (e: Exception) {
            loge("failed to get edittext: $e")
            null
        }
    }

    private fun reUpdateInputTextView() {
        getEditText()?.let {
            it.setText(this.value.toString())
        }
    }

    internal class IncreasePicker(private val picker: NumberPicker, private val newValue: Int, private val onComplete: () -> Unit) {
        private var counter = 0
        private var incrementValue = 0
        private var incrementFlag = false
        private val handler = Handler()
        private val runner = Runnable { run() }

        init {
            this.incrementValue = abs(picker.value - newValue)
            incrementFlag = newValue > picker.value
        }

        fun start(startDelay: Int = 0) = handler.postDelayed(runner, startDelay.toLong())

        private fun run() {
            ++counter
            if (counter > incrementValue) {
                onComplete.invoke()
                return
            }
            try {
                val method = picker.javaClass.superclass.getDeclaredMethod("changeValueByOne", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(picker, incrementFlag)
            } catch (e: Exception) {
                loge("Error: $e")
            }
            handler.postDelayed(runner, getDelayRequired())
        }

        private fun getDelayRequired(): Long {
            return when (abs(picker.value - newValue)) {
                in 0..10 -> DELAY_BASIS_STANDARD
                in 10..30 -> DELAY_BASIS_MEDIUM
                else -> DELAY_BASIS_FAST
            }
        }

        companion object {
            private const val DELAY_BASIS_STANDARD = 100L
            private const val DELAY_BASIS_MEDIUM = 80L
            private const val DELAY_BASIS_FAST = 60L
        }
    }
}