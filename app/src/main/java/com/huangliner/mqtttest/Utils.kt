package com.huangliner.mqtttest

import android.view.View
import com.huangliner.mqtttest.Utils.Companion.disableView

class Utils {
    companion object {
        fun View.disableView() {
            this.alpha = 0.6f
            this.isEnabled = false
        }

        fun View.enableView() {
            this.alpha = 1f
            this.isEnabled = true
        }
    }
}