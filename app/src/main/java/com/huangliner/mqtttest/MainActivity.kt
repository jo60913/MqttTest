package com.huangliner.mqtttest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.huangliner.mqtttest.Utils.Companion.disableView
import com.huangliner.mqtttest.Utils.Companion.enableView
import com.huangliner.mqtttest.databinding.ActivityMainBinding
import com.huangliner.mqtttest.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val mqttIP = "tcp://broker.emqx.io:1883"
    private val mqttClientID = "223"
    private val viewModel by viewModels<MainViewModel>()
    private var isConnect:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewMode = viewModel
        binding.btnMainConnect.setOnClickListener {
            if(isConnect){
                viewModel.disconnectMqtt()
            }else {
                viewModel.connectMqtt(mqttIP = mqttIP, mqttClientID = mqttClientID)
            }
        }

        binding.btnMainSubscribe.setOnClickListener {
            val topic = binding.editMainTopic.text.toString()
            viewModel.subscript(topic)
        }

        binding.btnMainEmit.setOnClickListener {
            val topic = binding.editMainTopic.text.toString()
            val message = binding.editMainMessage.text.toString()
            viewModel.emitMessageToBroker(message = message,topic = topic)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.emitState.collectLatest {
                    if(it.isNotEmpty()){
                        Toast.makeText(this@MainActivity,it,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.subscriptState.collectLatest {
                    if(it.isNotEmpty()){
                        Toast.makeText(this@MainActivity,it,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.mqttMessage.collectLatest {
                    Timber.e("測試 view 收到的消息${it}")
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.connectState.collect{
                    when(it){
                        is MqttConnectState.Error -> {
                            binding.btnMainConnect.text = "連線錯誤，請確認網路狀況"
                            binding.btnMainConnect.enableView()
                            binding.btnMainSubscribe.disableView()
                            binding.btnMainEmit.disableView()
                        }
                        is MqttConnectState.Idle -> {
                            binding.btnMainConnect.text = "連線"
                            binding.btnMainConnect.enableView()
                            binding.btnMainSubscribe.disableView()
                            binding.btnMainEmit.disableView()
                        }
                        is MqttConnectState.Loading -> {
                            binding.btnMainConnect.text = "連線中"
                            binding.btnMainConnect.disableView()
                            binding.btnMainSubscribe.disableView()
                            binding.btnMainEmit.disableView()
                        }
                        is MqttConnectState.Success -> {
                            binding.btnMainConnect.text = "中斷連線"
                            binding.btnMainConnect.enableView()
                            binding.btnMainSubscribe.enableView()
                            binding.btnMainEmit.enableView()
                        }
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}