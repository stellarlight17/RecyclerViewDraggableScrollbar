package com.stellarlight17.recyclerviewdraggablescrollbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stellarlight17.recyclerviewdraggablescrollbar.databinding.ActivityExampleBinding

class ExampleActivity: AppCompatActivity() {
    private val binding get() = this._binding
    private lateinit var _binding: ActivityExampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this._binding = ActivityExampleBinding.inflate(this.layoutInflater).also {
            this.setContentView(it.root)
        }

        this.binding.recyclerView.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            it.adapter = Adapter(50000)
            this.binding.scrollbarView.attachToRecyclerView(it)
        }
    }

    class Adapter(val count: Int): RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun getItemCount() = this.count
        override fun getItemId(position: Int) = position.toLong()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.activity_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = "${position}"
        }

        class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
            val textView: TextView = this.view.findViewById(android.R.id.text1)
        }
    }
}
