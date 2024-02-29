package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class weatherAdapter: ListAdapter<weatherModel,weatherAdapter.Holder>(Comparator()) {
    class Holder(view: View): RecyclerView.ViewHolder(view){
     val binding = ListItemBinding.bind(view)


    fun bind(item: weatherModel) = with(binding) {
        tvDate.text = item.time
        tvTemp.text = item.currentTemp.ifEmpty {"${item.maxTemp}°C / ${item.minTemp}" }
        tvCondition.text = item.condition
        Picasso.get().load("https:"+item.imageUrl).into(im)
    }

    }
    class Comparator: DiffUtil.ItemCallback<weatherModel>(){   //Предназначен для обновлениия элемннтов списка
        override fun areItemsTheSame(oldItem: weatherModel, newItem: weatherModel): Boolean {
            return oldItem == newItem

        }

        override fun areContentsTheSame(oldItem: weatherModel, newItem: weatherModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))

    }
}