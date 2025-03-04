package com.huangliner.mqtttest

sealed class ResultState{
    data object Success : ResultState()
    data class Error(val message: String) : ResultState()
}
