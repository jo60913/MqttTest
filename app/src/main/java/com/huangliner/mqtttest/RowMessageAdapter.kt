package com.huangliner.mqtttest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huangliner.mqtttest.databinding.RowMqttMessageBinding
import timber.log.Timber
import java.util.Locale

class RowMessageAdapter : RecyclerView.Adapter<RowMessageAdapter.ItemViewHolder>() {
    private var messages = mutableListOf<String>()

    class ItemViewHolder(val binding: RowMqttMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RowMqttMessageBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder.from(parent)

    override fun getItemCount() = this.messages.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val message = this.messages[position]
        Timber.e("測試 建構畫面 $message")
        holder.binding.tvRowMqttIndex.text = String.format(Locale.TAIWAN,"%d", (position + 1))
        holder.binding.tvRowMqttMessage.text = message
    }

    fun addNewMessage(newMessage:String) {
        Timber.e("測試 rv顯示新消息 $newMessage")
        this.messages.add(newMessage)
        Timber.d("測試 message : ${this.messages.size}")
        notifyItemInserted(this.messages.size - 1)
    }
}