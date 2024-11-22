package com.example.ssul.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ssul.R
import com.example.ssul.StoreInfo

class MenuAdapter(private val menuList: List<StoreInfo.MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImage: ImageView = itemView.findViewById(R.id.menu_image)
        val menuText: TextView = itemView.findViewById(R.id.menu_text)
        val priceText: TextView = itemView.findViewById(R.id.price_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuList[position]
        Glide.with(holder.menuImage.context)
            .load(menuItem.imageUrl)
            .placeholder(R.drawable.default_menu)
            .error(R.drawable.default_menu)
            .into(holder.menuImage)
        holder.menuText.text = menuItem.name
        holder.priceText.text = menuItem.price
    }

    override fun getItemCount(): Int = menuList.size
}
