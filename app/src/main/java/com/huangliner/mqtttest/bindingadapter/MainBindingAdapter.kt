package com.huangliner.mqtttest.bindingadapter

import android.view.View
import android.widget.Button
import androidx.databinding.BindingAdapter
import com.huangliner.mqtttest.MqttConnectState
import com.huangliner.mqtttest.Utils.Companion.disableView
import com.huangliner.mqtttest.Utils.Companion.enableView
import kotlinx.coroutines.flow.StateFlow

class MainBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter("showTextByConnect")
        fun showTextByConnect(button: Button, isConnect: Boolean) {
            if (isConnect) {
                button.text = "中斷連線"
            } else {
                button.text = "連線"
            }
        }

        @BindingAdapter("disableWhenDisconnect")
        @JvmStatic
        fun disableWhenDisconnect(
            view: View,
            connectState: StateFlow<MqttConnectState<String>>
        ) {
            when(connectState.value) {
                is MqttConnectState.Error -> {view.enableView()}
                is MqttConnectState.Idle -> {view.disableView()}
                is MqttConnectState.Loading -> {view.disableView()}
                is MqttConnectState.Success -> {view.enableView()}
            }
        }

        @JvmStatic
        @BindingAdapter("enableWhenDisconnect")
        fun enableWhenDisconnect(
            view: View,
            connectState: StateFlow<MqttConnectState<String>>
        ) {
            when(connectState.value) {
                is MqttConnectState.Error -> {view.enableView()}
                is MqttConnectState.Idle -> {view.enableView()}
                is MqttConnectState.Loading -> {view.disableView()}
                is MqttConnectState.Success -> {view.enableView()}
            }
        }
    }
}
