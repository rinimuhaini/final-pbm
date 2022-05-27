package com.example.storyapp.data.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.storyapp.R
import com.example.storyapp.data.remote.ListStoryItem
import com.example.storyapp.databinding.ListStoryBinding
import com.example.storyapp.utils.loadImage

class ListStoryAdapter(private val listStory: ArrayList<ListStoryItem>) :
    RecyclerView.Adapter<ListStoryAdapter.ViewHolder>() {
    private var onItemClickCallback: OnItemClickCallback? = null

    interface OnItemClickCallback {
        fun onItemClicked(story: ListStoryItem, storyBinding: ListStoryBinding)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class ViewHolder(val binding: ListStoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = listStory[position]

        holder.apply {
            binding.apply {
                detailImage.loadImage(story.photoUrl, object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                detailImage.contentDescription = itemView.context.getString(
                    R.string.stories_description, story.name
                )
                detailImage.transitionName =
                    itemView.context.getString(R.string.story_image, story.id)
                detailUser.text = itemView.context.getString(R.string.stories_user, story.name)
                detailUser.transitionName = itemView.context.getString(R.string.story_user, story.id)
            }

            itemView.setOnClickListener {
                onItemClickCallback?.onItemClicked(story, binding)
            }
        }
    }

    override fun getItemCount() = listStory.size
}