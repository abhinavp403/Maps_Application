package com.dev.abhinav.mapsapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.mapsapplication.R
import com.dev.abhinav.mapsapplication.adapter.FavoritesAdapter
import com.dev.abhinav.mapsapplication.database.LocationDatabase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: LocationDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_favs)
        retrieveList()
        return view
    }

    private fun retrieveList() {
        doAsync {
            db = LocationDatabase.invoke(activity?.applicationContext!!)
            val data = db.locationDao().getAll()
            uiThread {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = FavoritesAdapter(data, activity?.applicationContext!!)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        retrieveList()
    }
}