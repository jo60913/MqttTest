package com.huangliner.mqtttest

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.huangliner.mqtttest.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import info.mqtt.android.service.MqttAndroidClient

class MainActivity : AppCompatActivity() {
    private var _binding:ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val mqttIP = "tcp://broker.emqx.io:1883"
    private val mqttClientID = "mqttx_eca71cdc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnMainConnect.setOnClickListener {
            try {
                Log.e("測試","連線")

                val mqttAndroidClient = MqttAndroidClient(this,mqttIP,mqttClientID)
                val mqttAction = MqttConnectOptions()
                mqttAction.connectionTimeout = 30
                mqttAction.keepAliveInterval = 120
                mqttAction.isAutomaticReconnect = true
                mqttAction.isCleanSession = true

                val token = mqttAndroidClient.connect(mqttAction)
                token.actionCallback = object : IMqttActionListener{
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.e("測試","Mqtt 連線成功")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("測試","Mqtt 連線失敗 ${Log.getStackTraceString(exception)}")
                    }

                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}