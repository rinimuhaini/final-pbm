package com.example.storyapp.ui.detail

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.storyapp.R
import com.example.storyapp.data.remote.ListStoryItem
import com.example.storyapp.databinding.FragmentDetailBinding
import com.example.storyapp.utils.loadImage
import com.example.storyapp.utils.withDateFormat

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val story = DetailFragmentArgs.fromBundle(arguments as Bundle).story
        detailStory(story)
    }

    private fun detailStory(story: ListStoryItem) {
        binding.apply {
            detailUser.transitionName = getString(R.string.story_user, story.id)
            detailUser.text = getString(R.string.stories_user, story.name)
            detailUploaded.text =
                getString(R.string.stories_uploaded, story.createdAt.withDateFormat())
            detailDesc.text = getString(R.string.stories_desc, story.description)
            detailImage.transitionName = getString(R.string.story_image, story.id)
            detailImage.contentDescription = getString(
                R.string.stories_description, story.name
            )
            detailImage.loadImage(story.photoUrl, object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}