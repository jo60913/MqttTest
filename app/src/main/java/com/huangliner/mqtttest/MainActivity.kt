package com.huangliner.mqtttest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.huangliner.mqtttest.databinding.ActivityMainBinding
import com.huangliner.mqtttest.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val mqttIP = "tcp://broker.emqx.io:1883"
    private val mqttClientID = "223"
    private val viewModel by viewModels<MainViewModel>()
    var isConnect:Boolean = false
    private lateinit var mqttAndroidClient: MqttAndroidClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnMainConnect.setOnClickListener {
            if(isConnect){
                try{
                    mqttAndroidClient.disconnect()
                    binding.btnMainConnect.text = "連線"

                }catch (e:Exception){
                    Timber.e("測試 錯誤 ${Log.getStackTraceString(e)}")
                }
            }else {
                try {
                    Timber.d("測試 連線")
                    mqttAndroidClient = MqttAndroidClient(this, mqttIP, mqttClientID)
                    val mqttAction = MqttConnectOptions()
                    mqttAction.connectionTimeout = 30
                    mqttAction.keepAliveInterval = 120
                    mqttAction.isAutomaticReconnect = true
                    mqttAction.isCleanSession = true
                    mqttAndroidClient.setCallback(object:MqttCallback{
                        override fun connectionLost(cause: Throwable?) {
                            cause?.let {
                                Timber.e("測試 丟失 ${it.message}")
                            }
                        }

                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            Timber.e("測試 接收到${topic}")
                            val data = String(message?.payload!!)
                            Timber.e("測試 接收到${data}")
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {
                            token?.let {
                                Timber.d("測試 完整接收 ${token.message}")
                            }
                        }
                    })
                    val token = mqttAndroidClient.connect(mqttAction)
                    token.actionCallback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Timber.d("測試 Mqtt 連線成功")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Timber.d("測試 Mqtt 連線失敗 ${Log.getStackTraceString(exception)}")
                        }

                    }

                    binding.btnMainConnect.text = "中斷連線"
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }

            isConnect = !isConnect
        }

        binding.btnMainSubscribe.setOnClickListener {
            val topic = binding.editMainTopic.text.toString()
            Timber.d("按下訂閱 : $topic")
            if(topic.isEmpty()){
                Toast.makeText(this,"請輸入訂閱主題",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val token = mqttAndroidClient.subscribe(topic,0)
            token.actionCallback = object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Timber.d("訂閱${topic}成功")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Timber.e("訂閱${topic}失敗")
                }

            }
        }

        binding.btnMainEmit.setOnClickListener {
            val topic = binding.editMainTopic.text.toString()
            val mqttMessage = MqttMessage("123".toByteArray()).apply {
                qos = 1 // 設置 QoS
            }
            mqttAndroidClient.publish(topic, mqttMessage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}