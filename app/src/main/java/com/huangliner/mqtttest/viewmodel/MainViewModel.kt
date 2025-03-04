package com.huangliner.mqtttest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huangliner.mqtttest.MqttConnectState
import com.huangliner.mqtttest.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val application: Application) :
    AndroidViewModel(application) {
    private val _connectState = MutableStateFlow<MqttConnectState<String>>(MqttConnectState.Idle())
    private val _mqttMessage = MutableSharedFlow<String>()
    private val _subscriptState = MutableSharedFlow<ResultState>()
    private val _emitState = MutableSharedFlow<String>()

    val connectState = _connectState.asStateFlow()
    val mqttMessage = _mqttMessage.asSharedFlow()
    val subscriptState = _subscriptState.asSharedFlow()
    val emitState = _emitState.asSharedFlow()
    private lateinit var mqttAndroidClient: MqttAndroidClient

    fun connectMqtt(mqttIP: String, mqttClientID: String) {
        _connectState.value = MqttConnectState.Loading()
        try {
            Timber.d("測試 連線")
            mqttAndroidClient = MqttAndroidClient(application, mqttIP, mqttClientID)
            val mqttAction = MqttConnectOptions()
            mqttAction.connectionTimeout = 30
            mqttAction.keepAliveInterval = 120
            mqttAction.isAutomaticReconnect = true
            mqttAction.isCleanSession = true
            mqttAndroidClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    cause?.let {
                        Timber.e("測試 丟失 ${it.message}")
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    viewModelScope.launch {
                        val data = String(message?.payload!!)
                        _mqttMessage.emit(data)
                        Timber.e("測試 接收到${topic}")
                        Timber.e("測試 接收到${data}")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    token?.let {
                        Timber.d("測試 完整發送 ${token.message}")
                    }
                }
            })
            val token = mqttAndroidClient.connect(mqttAction)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Timber.d("測試 Mqtt 連線成功")

                    _connectState.value = MqttConnectState.Success()
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?
                ) {
                    _connectState.value = MqttConnectState.Error(exception?.message)
                    Timber.d("測試 Mqtt 連線失敗 ${Log.getStackTraceString(exception)}")
                }

            }

        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnectMqtt(){
        try{
            mqttAndroidClient.disconnect()
            _connectState.value = MqttConnectState.Idle()
        }catch (e:Exception){
            Timber.e("測試 錯誤 ${Log.getStackTraceString(e)}")
        }
    }

    fun subscript(topic:String){
        viewModelScope.launch {
            Timber.d("按下訂閱 : $topic")
            if(topic.isEmpty()){
                _subscriptState.emit(ResultState.Error("請輸入內容"))
                return@launch
            }
            val token = mqttAndroidClient.subscribe(topic,0)
            token.actionCallback = object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Timber.d("訂閱${topic}成功")
                    viewModelScope.launch {
                        _subscriptState.emit(ResultState.Success)
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Timber.e("訂閱${topic}失敗")
                    viewModelScope.launch {

                        _subscriptState.emit(ResultState.Error("訂閱${topic}失敗"))
                    }
                }

            }
        }

    }

    fun emitMessageToBroker(
        message:String,
        topic:String,
        qosLevel :Int = 0) {
        viewModelScope.launch {
            if(message.isEmpty()){
                _emitState.emit("請輸入內容")
                return@launch
            }

            if(topic.isEmpty()){
                _emitState.emit("請輸入主題")
                return@launch
            }
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                qos = 1 // 設置 QoS
            }
            mqttAndroidClient.publish(topic, mqttMessage)

            _emitState.emit("")
        }


    }
}