package com.huangliner.mqtttest

sealed class MqttConnectState<T>(
    val message : String? = null
) {
    class Idle<T>:MqttConnectState<T>()
    class Success<T>:MqttConnectState<T>()
    class Error<T>(message:String?):MqttConnectState<T>(message)
    class Loading<T>:MqttConnectState<T>()
}