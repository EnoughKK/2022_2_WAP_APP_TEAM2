package com.example.wapapp2.view.calendar.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wapapp2.databinding.DialogCalculationItemBinding
import com.example.wapapp2.model.ReceiptDTO

class ReceiptListForADayAdapter(private val list: ArrayList<ReceiptDTO>) : RecyclerView.Adapter<ReceiptListForADayAdapter.ViewHolder>() {


    inner class ViewHolder(private val binding: DialogCalculationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val position = adapterPosition
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DialogCalculationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = list.size
}
