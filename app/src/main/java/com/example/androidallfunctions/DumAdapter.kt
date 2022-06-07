package com.example.androidallfunctions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidallfunctions.databinding.ItemBinding
import javax.inject.Inject


class DumAdapter @Inject constructor(
    val dummyInstance: DummyInstance,
    val dummyInstance2: DummyInstance
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder constructor(binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
            companion object{
                @JvmStatic
                fun create(parent:ViewGroup) = ViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 2
    }
}