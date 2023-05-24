package com.jorgetargz.projectseeker.framework.register_user

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.FragmentRegisterUserBinding
import com.jorgetargz.projectseeker.framework.common.BaseAuthFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class RegisterUserFragment : BaseAuthFragment() {

    private val viewModel: RegisterUserViewModel by viewModels()
    private lateinit var binding: FragmentRegisterUserBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    inner class PhoneRegisterCallBacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Timber.d("onVerificationCompleted")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            binding.btnSendCode.isEnabled = true
            Timber.d("onVerificationFailed", e)
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    showSnackbar(getString(R.string.invalid_phone_number))
                }

                is FirebaseTooManyRequestsException -> {
                    showSnackbar(getString(R.string.too_many_requests))
                }

                else -> {
                    showSnackbar(getString(R.string.authentication_failed))
                }
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: ForceResendingToken
        ) {
            showSnackbar(getString(R.string.code_sent))
            allowSMSCodeInputAndDisablePhoneInput()
            resendButton(token)
            registerButton(verificationId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterUserBinding.inflate(layoutInflater)
        binding.ccp.detectSIMCountry(true)
        binding.ccp.showNameCode(false)

        sendCodeButtom()

        observeViewModel()

        return binding.root
    }

    private fun allowSMSCodeInputAndDisablePhoneInput() {
        with(binding) {
            tilSMSCode.isEnabled = true
            etSMSCode.inputType = InputType.TYPE_CLASS_NUMBER
            etPhone.isEnabled = false
        }
    }

    private fun resendButton(token: ForceResendingToken) {
        binding.btnSendCode.isEnabled = true
        binding.btnSendCode.text = getString(R.string.resend_code)
        binding.btnSendCode.setOnClickListener {
            val phone =
                binding.ccp.selectedCountryCodeWithPlus + binding.etPhone.text.toString()
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(PhoneRegisterCallBacks())
                .setForceResendingToken(token)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun registerButton(verificationId: String) {
        with(binding) {
            btnRegister.isEnabled = true
            btnRegister.setOnClickListener {
                val username = etUsername.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                val smsCode = etSMSCode.text.toString()
                if (validateFields()) {
                    val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
                    viewModel.registerUser(email, password, username, credential)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var validation = true
        with(binding) {
            val username = etUsername.text.toString()
            if (username.isEmpty()) {
                etUsername.error = getString(R.string.username_is_required)
                validation = false
            }
            val email = etEmail.text.toString()
            if (email.isEmpty()) {
                etEmail.error = getString(R.string.email_is_required)
                validation = false
            }
            val password = etPassword.text.toString()
            if (password.isEmpty()) {
                etPassword.error = getString(R.string.password_is_required)
                validation = false
            }
            val confirmPassword = etConfirmPassword.text.toString()
            if (password != confirmPassword) {
                etConfirmPassword.error = getString(R.string.passwords_do_not_match)
                validation = false
            }
            val smsCode = etSMSCode.text.toString()
            if (smsCode.isEmpty()) {
                etSMSCode.error = getString(R.string.sms_code_is_required)
                validation = false
            }
        }
        return validation
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userRegistered.collect { user ->
                    user?.let { viewModel.getIDTokenAndLogin() }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logged.collect { user ->
                    user?.let {
                        logInDone(it)
                    }
                }
            }
        }
        observeLoading(viewModel)
        observeErrorString(viewModel)
        observeErrorResourceCode(viewModel)
    }

    private fun sendCodeButtom() {
        binding.btnSendCode.setOnClickListener {
            binding.btnSendCode.isEnabled = false
            val phone =
                binding.ccp.selectedCountryCodeWithPlus + binding.etPhone.text.toString()
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(PhoneRegisterCallBacks())
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }
}