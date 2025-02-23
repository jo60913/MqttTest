package com.huangliner.mqtttest.bindingadapter

import android.widget.Button
import androidx.databinding.BindingAdapter

class MainBindingAdapter {
    companion object{
        @BindingAdapter("showTextByConnect")
        fun showTextByConnect(button: Button,isConnect:Boolean){
            if(isConnect){
                button.text = "中斷連線"
            }else{
                button.text = "連線"
            }
        }
    }
}