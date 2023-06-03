package com.jorgetargz.projectseeker.framework.login

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.FragmentLoginBinding
import com.jorgetargz.projectseeker.framework.common.BaseAuthFragment
import com.jorgetargz.projectseeker.framework.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class LoginFragment() : BaseAuthFragment() {

    // View Binding
    private val binding: FragmentLoginBinding by lazy {
        FragmentLoginBinding.inflate(layoutInflater)
    }

    // View Models
    private val viewModel: LoginViewModel by viewModels()

    // Google One Tap
    private lateinit var oneTapClient: SignInClient
    private val oneTapSignInContract = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            val username = credential.id
            val password = credential.password
            when {
                idToken != null -> {
                    viewModel.firebaseAuthWithGoogle(idToken)
                    Timber.d("Got ID token.")
                }

                password != null -> {
                    viewModel.firebaseAuthWithEmailAndPassword(username, password)
                    Timber.d("Got password.")
                }

                else -> {
                    // Shouldn't happen.
                    Timber.d("No ID token or password!")
                }
            }
        } else {
            Timber.d("One Tap Sign in failed.")
        }
    }

    // Google Sign In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:" + account.id)
                viewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w("Google sign in failed", e)
                showSnackbar(getString(R.string.google_sign_in_failed))
            }
        } else {
            showSnackbar(getString(R.string.google_sign_in_failed))
        }
    }

    // Phone Sign in
    private lateinit var phone: String

    inner class PhoneLoginCallBacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            viewModel.firebaseAuthWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Timber.d("onVerificationFailed", e)
            if (e is FirebaseAuthInvalidCredentialsException) {
                showSnackbar(getString(R.string.invalid_phone_number))
            } else if (e is FirebaseTooManyRequestsException) {
                showSnackbar(getString(R.string.too_many_requests))
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            phoneVerificationCodeDialog(token, verificationId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        observeViewModel()

        lockDrawer()
        hideTopBar()

        if (FirebaseAuth.getInstance().currentUser == null) googleOneTapInit()
        googleLoginButton()
        emailLoginButton()
        phoneLoginButton()
        singUpButton()

        return binding.root
    }

    private fun singUpButton() {
        binding.registerButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterUserFragment()
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        observeStateFlowOnStarted {
            viewModel.logged.collect { user ->
                user?.let {
                    logInDone(it)
                    viewModel.userLoggedHandled()
                }
            }
        }
        observeLoading(viewModel.isLoading)
        observeErrorString(viewModel.errorString) { viewModel.errorStringHandled() }
        observeErrorResourceCode(viewModel.errorResourceCode) { viewModel.errorResourceCodeHandled() }
    }

    private fun lockDrawer() {
        val activity = requireActivity()
        val drawer: DrawerLayout = activity.findViewById(R.id.drawer_layout)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun hideTopBar() {
        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.hide()
    }

    private fun googleOneTapInit() {
        oneTapClient = Identity.getSignInClient(binding.root.context)
        val signInRequest = BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false).build()
        ).setAutoSelectEnabled(false).build()

        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
            try {
                oneTapSignInContract.launch(
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                )
            } catch (e: IntentSender.SendIntentException) {
                Timber.e("Couldn't start One Tap UI: ${e.localizedMessage}")
            }
        }.addOnFailureListener { e ->
            Timber.d(e.localizedMessage)
        }
    }

    private fun googleLoginButton() {
        binding.googleLoginButton.setOnClickListener {
            googleSignInClient.signOut()
            googleSignInContract.launch(googleSignInClient.signInIntent)
        }
    }

    private fun emailLoginButton() {
        binding.mailLoginButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(R.layout.dialog_login_email)
                setPositiveButton(getString(R.string.login)) { dialog1, _ ->
                    val dialog = dialog1 as Dialog
                    val email = dialog.findViewById<TextInputEditText>(R.id.etEmail).text.toString()
                    val password =
                        dialog.findViewById<TextInputEditText>(R.id.etPassword).text.toString()
                    viewModel.firebaseAuthWithEmailAndPassword(email, password)
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                setNeutralButton(getString(R.string.forgot_password)) { loginDialog, _ ->
                    loginDialog.dismiss()
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setView(R.layout.dialog_change_password_by_email)
                        setPositiveButton(getString(R.string.send_email)) { dialog1, _ ->
                            val dialog = dialog1 as Dialog
                            val email =
                                dialog.findViewById<TextInputEditText>(R.id.etEmail).text.toString()
                            viewModel.firebaseSendPasswordResetEmail(email)
                        }
                        setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        show()
                    }
                }
                show()
            }
        }
    }

    private fun phoneLoginButton() {
        binding.phoneLoginButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_login_phone, null)
            val ccp = dialogView.findViewById<CountryCodePicker>(R.id.ccp)
            ccp.detectSIMCountry(true)

            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(dialogView)
                setPositiveButton("Send code") { dialog1, _ ->
                    val dialog = dialog1 as Dialog
                    phone = ccp.selectedCountryCodeWithPlus +
                            dialog.findViewById<TextInputEditText>(R.id.etPhone).text.toString()

                    sendVerificationCode()
                }
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }
    }

    private fun phoneVerificationCodeDialog(
        token: PhoneAuthProvider.ForceResendingToken,
        verificationId: String,
    ) {
        val dialogView = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.dialog_phone_verification, null)
        val etCode = dialogView.findViewById<TextInputEditText>(R.id.etCode)
        dialogView.findViewById<TextInputEditText>(R.id.etPhone).setText(phone)

        MaterialAlertDialogBuilder(binding.root.context)
            .setView(dialogView)
            .setPositiveButton("Verify code") { _, _ ->
                val credential = PhoneAuthProvider.getCredential(
                    verificationId,
                    etCode.text.toString()
                )
                viewModel.firebaseAuthWithPhoneAuthCredential(credential)
            }
            .setNeutralButton("Resend code") { _, _ ->
                resendVerificationCode(token)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(PhoneLoginCallBacks())
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(token: PhoneAuthProvider.ForceResendingToken) {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(PhoneLoginCallBacks())
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showToolbar()
    }

    private fun showToolbar() {
        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.show()
    }
}