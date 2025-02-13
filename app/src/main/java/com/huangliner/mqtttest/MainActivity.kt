package com.huangliner.mqtttest

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.huangliner.mqtttest.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class MainActivity : AppCompatActivity() {
    private var _binding:ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnMainConnect.setOnClickListener {
            try {
                Log.e("測試","連線")
                // 設置持久性層
                val persistence = MemoryPersistence()

                // 初始化MQTT客戶端
                val client = MqttClient("tcp://127.0.0.1:1883", "mqttx_65c75492", persistence)

                // 設置連接選項
                val connectOptions = MqttConnectOptions()
                connectOptions.isCleanSession = true

                // 連接到代理
                client.connect(connectOptions)
                Log.e("測試","${client.isConnected}")
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