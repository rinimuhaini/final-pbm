package com.example.storyapp.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.fragment.app.*
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.storyapp.R
import com.example.storyapp.MainViewModel
import com.example.storyapp.views.EditText
import com.example.storyapp.data.SessionPreferences
import com.example.storyapp.dataStore
import com.example.storyapp.databinding.FragmentLoginBinding
import com.example.storyapp.utils.hideSoftKeyboard
import com.example.storyapp.utils.showSnackbar
import com.example.storyapp.utils.visibility
import com.example.storyapp.ui.register.RegisterFragment


class LoginFragment : Fragment() {
    private val viewModel by viewModels<LoginViewModel>()
    private val sharedViewModel by activityViewModels<MainViewModel> {
        MainViewModel.Factory(SessionPreferences.getInstance(context?.dataStore as DataStore))
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        playanimation()
        return binding.root
    }

    private fun playanimation(){
        ObjectAnimator.ofFloat(binding.ivLogin, View.TRANSLATION_X, -40f, 40f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val welcome = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(420)
        val tvLogin = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(420)
        val email = ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(420)
        val password = ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1f).setDuration(420)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(420)
        val toRegister = ObjectAnimator.ofFloat(binding.goToRegister, View.ALPHA, 1f).setDuration(420)

        val together = AnimatorSet().apply {
            playTogether(welcome, tvLogin)
        }


        AnimatorSet().apply {
            playSequentially(together, email, password, btnLogin, toRegister)
            start()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Loading(false)

        setFragmentResultListener(RegisterFragment.REGISTER_RESULT) { _, bundle ->
            val email = bundle.getString(RegisterFragment.EMAIL, "")
            binding.etEmail.setText(email)
        }

        binding.apply {
            goToRegister.setOnClickListener {
                findNavController().navigate(
                    R.id.action_loginFragment_to_registerFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(
                        etEmail to etEmail.transitionName,
                        etPassword to etPassword.transitionName,
                        btnLogin to btnLogin.transitionName,
                        goToRegister to goToRegister.transitionName
                    )
                )
            }

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

            btnLogin.setOnClickListener {
                Login()
            }
        }

        viewModel.apply {
            isLoading.observe(viewLifecycleOwner) {
                Loading(it)
            }

            token.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let {
                    loggedIn(it)
                }
            }

            error.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { message ->
                    showSnackbar(binding.root, message)
                }
            }
        }

        sharedViewModel.getToken().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                goToStories(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private fun goToStories(token: String) {
        val navigateAction = LoginFragmentDirections
            .actionLoginFragmentToStoriesFragment()
        navigateAction.token = token

        findNavController().navigate(navigateAction)
    }

    private fun Loading(isLoading: Boolean) {
        binding.apply {
            loginGroup.visibility = visibility(!isLoading)
            loginLoadingGroup.visibility = visibility(isLoading)
        }
    }

    private fun Login() {
        hideSoftKeyboard(activity as FragmentActivity)

        with(binding) {
            val isEmailValid = etEmail.validateInput()
            val isPasswordValid = etPassword.validateInput()

            if (!isEmailValid || !isPasswordValid) {
                showSnackbar(root, getString(R.string.validation_error))
                return
            }

            viewModel.login(etEmail.text.toString(), etPassword.text.toString())
        }
    }

    private fun loggedIn(token: String) {
        sharedViewModel.saveToken(token)
        showSnackbar(binding.root, getString(R.string.login_success))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}