package com.example.storyapp.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.storyapp.R
import com.example.storyapp.views.EditText
import com.example.storyapp.databinding.FragmentRegisterBinding
import com.example.storyapp.utils.hideSoftKeyboard
import com.example.storyapp.utils.showSnackbar
import com.example.storyapp.utils.visibility

class RegisterFragment : Fragment() {
    private val viewModel by viewModels<RegisterViewModel>()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        playanimation()
        return binding.root
    }

    private fun playanimation(){
        ObjectAnimator.ofFloat(binding.ivRegister, View.TRANSLATION_X, -40f, 40f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val welcome = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(400)
        val name = ObjectAnimator.ofFloat(binding.etName, View.ALPHA, 1f).setDuration(400)
        val email = ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(400)
        val password = ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1f).setDuration(400)
        val btnRegister = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(400)
        val toLogin = ObjectAnimator.ofFloat(binding.goToLogin, View.ALPHA, 1f).setDuration(400)

        AnimatorSet().apply {
            playSequentially(welcome, name, email, password, btnRegister, toLogin)
            start()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(false)

        binding.apply {
            goToLogin.setOnClickListener {
                findNavController().navigateUp()
            }

            etName.setValidationCallback(object : EditText.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.name_validation)

                override fun validate(input: String) = input.isNotEmpty()
            })

            etEmail.setValidationCallback(object : EditText.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.email_validation)

                override fun validate(input: String) = input.isNotEmpty()
                        && Patterns.EMAIL_ADDRESS.matcher(input).matches()
            })

            etPassword.setValidationCallback(object : EditText.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.password_validation)

                override fun validate(input: String) = input.length >= 6
            })

            btnRegister.setOnClickListener {
                Register()
            }
        }

        viewModel.apply {
            isLoading.observe(viewLifecycleOwner) {
                showLoading(it)
            }

            isSuccess.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { success ->
                    if (success) {
                        registered()
                    }
                }
            }

            error.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { message ->
                    showSnackbar(binding.root, message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private fun Register() {
        hideSoftKeyboard(activity as FragmentActivity)

        with(binding) {
            val isNameValid = etName.validateInput()
            val isEmailValid = etEmail.validateInput()
            val isPasswordValid = etPassword.validateInput()

            if (!isNameValid || !isEmailValid || !isPasswordValid) {
                showSnackbar(root, getString(R.string.validation_error))
                return
            }

            viewModel.register(
                etName.text.toString(),
                etEmail.text.toString(),
                etPassword.text.toString()
            )
        }
    }

    private fun registered() {
        showSnackbar(binding.root, getString(R.string.register_success))

        setFragmentResult(
            REGISTER_RESULT, bundleOf(
                EMAIL to binding.etEmail.text.toString()
            )
        )

        findNavController().navigateUp()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            registerGroup.visibility = visibility(!isLoading)
            registerLoadingGroup.visibility = visibility(isLoading)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REGISTER_RESULT = "register_result"
        const val EMAIL = "email"
    }
}