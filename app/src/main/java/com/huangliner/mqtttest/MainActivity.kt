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
import kotlinx.coroutines.flow.collect
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
    private val rowMessageAdapter = RowMessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewMode = viewModel
        binding.rvMainMessage.adapter = rowMessageAdapter
        binding.btnMainConnect.setOnClickListener {
            if(isConnect){
                viewModel.disconnectMqtt()
            }else {
                viewModel.connectMqtt(mqttIP = mqttIP, mqttClientID = mqttClientID)
            }
            isConnect = !isConnect
        }

        binding.btnMainEmit.disableView()
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
                viewModel.emitState.collect {
                    if(it.isNotEmpty()){
                        Toast.makeText(this@MainActivity,it,Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@MainActivity,"發送成功",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                viewModel.subscriptState.collect {
                    when(it) {
                        is ResultState.Error -> {
                            Timber.d("測試 sub 錯誤")
                            binding.btnMainEmit.disableView()
                            Toast.makeText(this@MainActivity,it.message,Toast.LENGTH_SHORT).show()
                        }
                        ResultState.Success -> {
                            Timber.d("測試 sub 成功")
                            binding.btnMainEmit.enableView()
                            Toast.makeText(this@MainActivity,"訂閱成功",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.mqttMessage.collect {
                    Timber.e("測試 view 收到的消息${it}")
                    binding.editMainMessage.setText("")
                    rowMessageAdapter.addNewMessage(it)
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
                        }
                        is MqttConnectState.Idle -> {
                            binding.btnMainConnect.text = "連線"
                            binding.btnMainConnect.enableView()
                            binding.btnMainSubscribe.disableView()
                        }
                        is MqttConnectState.Loading -> {
                            binding.btnMainConnect.text = "連線中"
                            binding.btnMainConnect.disableView()
                            binding.btnMainSubscribe.disableView()
                        }
                        is MqttConnectState.Success -> {
                            binding.btnMainConnect.text = "中斷連線"
                            binding.btnMainConnect.enableView()
                            binding.btnMainSubscribe.enableView()
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