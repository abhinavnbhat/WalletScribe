package com.example.walletscribe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LogAdapter :
    ListAdapter<String, LogAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = getItem(position)
    }

    class VH(item: View) : RecyclerView.ViewHolder(item) {
        val text: TextView = item.findViewById(android.R.id.text1)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(o: String, n: String) = o == n
            override fun areContentsTheSame(o: String, n: String) = o == n
        }
    }
}
