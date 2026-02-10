package com.example.finalproject.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.R
import com.example.finalproject.data.Course

class CourseAdapter(
    private val context: Context,
    private var items: MutableList<Course>,
    private val isAdmin: Boolean,
    private val onDeleteClicked: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.VH>() {

    fun updateData(newItems: List<Course>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.title.text = item.title
        holder.desc.text = item.description
        holder.category.text = item.category

        if (isAdmin) {
            holder.delete.visibility = View.VISIBLE
            holder.delete.setOnClickListener { onDeleteClicked(item) }
        } else {
            holder.delete.visibility = View.GONE
            holder.delete.setOnClickListener(null)
        }


        val url = item.imageUrl
        if (!url.isNullOrBlank()) {
            Glide.with(holder.image).load(url).into(holder.image)
        } else {
            item.imageRes?.let { holder.image.setImageResource(it) }
                ?: holder.image.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.play.setOnClickListener {
            val video = item.videoUrl
            if (!video.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video))
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgCourse)
        val title: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val desc: TextView = itemView.findViewById(R.id.tvCourseDesc)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val play: ImageButton = itemView.findViewById(R.id.btnPlay)
        val delete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}
