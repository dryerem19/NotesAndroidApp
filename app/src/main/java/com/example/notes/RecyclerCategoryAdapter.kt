package com.example.notes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.db.entities.Category

class RecyclerCategoryAdapter() :
    RecyclerView.Adapter<RecyclerCategoryAdapter.CategoryViewHolder>() {

    private var categories = mutableListOf<Category>()

    private var selectedCategory: Category? = null

    interface OnItemClickListener {
        fun onItemClicked(category: Category)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(callback: OnItemClickListener) {
        onItemClickListener = callback
    }

    fun getSelectedCategory() : Category? {
        return selectedCategory
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.category_name_text_view)

        init {
            itemView.setOnClickListener {
                selectedCategory = categories[adapterPosition]
                onItemClickListener?.onItemClicked(categories[adapterPosition])
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryTextView.text = category.name
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}