package com.example.ssul.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ssul.R
import com.example.ssul.StoreItem

class StoreAdapter(
    private var storeItems: List<StoreItem>,
    private val onFavoriteClicked: (Int) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    inner class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storeImage: ImageView = itemView.findViewById(R.id.store_image)
        private val storeText: TextView = itemView.findViewById(R.id.store_text)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favorite_button)
        private val locationText: TextView = itemView.findViewById(R.id.location_text)
        private val filterGroup: TextView = itemView.findViewById(R.id.filter_group_image)
        private val filterDate: TextView = itemView.findViewById(R.id.filter_date_image)
        private val filterEfficiency: TextView = itemView.findViewById(R.id.filter_efficiency_image)
        private val filterPartner: TextView = itemView.findViewById(R.id.filter_partner_image)

        fun bind(item: StoreItem) {
            storeImage.setImageResource(item.storeImage)
            storeText.text = item.storeText
            locationText.text = item.locationText

            // 즐겨찾기 가시성 조정
            favoriteButton.setImageResource(
                if (item.isFavorite) R.drawable.favorite_clicked else R.drawable.favorite_non_clicked
            )

            favoriteButton.setOnClickListener {
                onFavoriteClicked(item.storeId)
            }

            // 필터 가시성 조정
            controlVisibility(filterPartner, item.isFilterPartnerChecked)
            controlVisibility(filterGroup, item.isFilterGroupChecked)
            controlVisibility(filterDate, item.isFilterDateChecked)
            controlVisibility(filterEfficiency, item.isFilterEfficiencyChecked)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(storeItems[position])
    }

    override fun getItemCount(): Int = storeItems.size

    // 필터 선택 여부에 따른 가시성 조정
    fun controlVisibility(view : View, isSelected : Boolean) {
        view.visibility = if (isSelected) View.VISIBLE else View.GONE
    }

    // 필터 체크에 따른 가게 리스트 업데이트
    fun updateItems(newItems: List<StoreItem>) {
        storeItems = newItems
        notifyDataSetChanged()
    }
}