package com.example.storyapp.ui.stories

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.adapter.ListStoryAdapter
import com.example.storyapp.data.remote.ListStoryItem
import com.example.storyapp.databinding.FragmentStoriesBinding
import com.example.storyapp.databinding.ListStoryBinding
import com.example.storyapp.utils.showSnackbar
import com.example.storyapp.utils.visibility
import com.example.storyapp.ui.upload.UploadFragment

class StoriesFragment : Fragment() {
    private var token = ""

    private val viewModel by viewModels<StoriesViewModel> {
        StoriesViewModel.Factory(getString(R.string.auth, token))
    }

    private var _binding: FragmentStoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = StoriesFragmentArgs.fromBundle(arguments as Bundle).token
        postponeEnterTransition()

        setFragmentResultListener(UploadFragment.ADD_RESULT) { _, bundle ->
            if (bundle.getBoolean(UploadFragment.IS_SUCCESS)) {
                storyAdd()
            }
        }

        val layoutManager = if (activity?.applicationContext
                ?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            LinearLayoutManager(context)
        } else {
            GridLayoutManager(context, 2)
        }

        binding.apply {
            listStory.apply {
                setHasFixedSize(true)
                this.layoutManager = layoutManager
                addItemDecoration(
                    DividerItemDecoration(
                        context,
                        layoutManager.orientation
                    )
                )
            }

            addNew.setOnClickListener {
                NewStory()
            }
        }

        viewModel.apply {
            isLoading.observe(viewLifecycleOwner) {
                showLoading(it)
            }

            stories.observe(viewLifecycleOwner) {
                setStories(ArrayList(it))
                binding.tvNoData.visibility = visibility(it.isEmpty())
                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }

            error.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { message ->
                    showSnackbar(binding.root, message, getString(R.string.retry)) {
                        viewModel.AllStories()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun storyAdd() {
        showSnackbar(binding.root, getString(R.string.upload_success))

        viewModel.AllStories()
    }

    private fun NewStory() {
        val navigateAction = StoriesFragmentDirections
            .actionStoriesFragmentToNewStoryFragment()
        navigateAction.token = token

        findNavController().navigate(navigateAction)
    }


    private fun setStories(stories: ArrayList<ListStoryItem>) {
        binding.listStory.adapter = ListStoryAdapter(stories).apply {
            setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
                override fun onItemClicked(story: ListStoryItem, storyBinding: ListStoryBinding) {
                    val extras = FragmentNavigatorExtras(
                        storyBinding.detailImage to getString(R.string.story_image, story.id),
                        storyBinding.detailUser to getString(R.string.story_user, story.id)
                    )

                    view?.findNavController()?.navigate(
                        R.id.action_storiesFragment_to_storyDetailFragment,
                        bundleOf(
                            "story" to story
                        ),
                        null,
                        extras
                    )
                }
            })
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            listStory.visibility = visibility(!isLoading)
            storiesLoadingGroup.visibility = visibility(isLoading)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}