package com.dev.abhinav.mapsapplication.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.mapsapplication.R
import com.dev.abhinav.mapsapplication.database.LocationDatabase
import com.dev.abhinav.mapsapplication.database.LocationEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavoritesAdapter(private val list: List<LocationEntity>, context: Context):
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {
    private var db = LocationDatabase.invoke(context)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.place)
        var address: TextView = itemView.findViewById(R.id.address)
        var remove: ImageView = itemView.findViewById(R.id.delete_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_favorites, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].name
        holder.address.text = list[position].address
        holder.remove.setOnClickListener {
            GlobalScope.launch {
                db.locationDao().delete(
                    LocationEntity(
                        list[position].id,
                        list[position].name,
                        list[position].address,
                        list[position].latitude,
                        list[position].longitude
                    )
                )
            }
            Log.d("FavoritesAdapter", "Removed from Favorite")
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = list.size
}