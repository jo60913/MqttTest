package com.huangliner.mqtttest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.huangliner.mqtttest.MqttConnectState
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _mqttMessage = MutableStateFlow("")
    private val _subscriptState = MutableStateFlow("")  //如果空字串代表成功，不是空字串帶有錯誤訊息
    private val _emitState = MutableStateFlow("")
    val connectState = _connectState.asStateFlow()
    val mqttMessage = _mqttMessage.asStateFlow()
    val subscriptState = _subscriptState.asStateFlow()
    val emitState = _emitState.asStateFlow()
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
                    val data = String(message?.payload!!)
                    _mqttMessage.value = data
                    Timber.e("測試 接收到${topic}")
                    Timber.e("測試 接收到${data}")
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

        }catch (e:Exception){
            Timber.e("測試 錯誤 ${Log.getStackTraceString(e)}")
        }
    }

    fun subscript(topic:String){
        Timber.d("按下訂閱 : $topic")
        if(topic.isEmpty()){
            _subscriptState.value = "不能輸入為空"
            return
        }
        val token = mqttAndroidClient.subscribe(topic,0)
        token.actionCallback = object : IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Timber.d("訂閱${topic}成功")
                _subscriptState.value = ""
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Timber.e("訂閱${topic}失敗")
                _subscriptState.value = "訂閱${topic}失敗 ${exception?.message}"
            }

        }
    }

    fun emitMessageToBroker(
        message:String,
        topic:String,
        qosLevel :Int = 0) {
        if(message.isEmpty()){
            _emitState.value = "請輸入內容"
            return
        }

        if(topic.isEmpty()){
            _emitState.value = "請輸入主題"
            return
        }
        val mqttMessage = MqttMessage(message.toByteArray()).apply {
            qos = 1 // 設置 QoS
        }
        mqttAndroidClient.publish(topic, mqttMessage)

        _emitState.value = ""

    }
}