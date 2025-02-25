package com.example.wapapp2.view.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.wapapp2.R
import com.example.wapapp2.databinding.FragmentLoginBinding
import com.example.wapapp2.datastore.MyDataStore
import com.example.wapapp2.repository.*
import com.example.wapapp2.view.main.RootTransactionFragment
import com.example.wapapp2.viewmodel.MyAccountViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {
    private val myAccountViewModel by viewModels<MyAccountViewModel>({ requireActivity() })
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    var auth: FirebaseAuth? = null


    companion object {
        const val TAG = "LoginFragment"
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (!parentFragmentManager.popBackStackImmediate()) {
                requireActivity().finish()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()

        //moveMainPage(auth?.currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().viewModelStore.clear()

        auth = FirebaseAuth.getInstance()
    }

    fun emailLogin() {
        if (binding.emailEdittext.text.toString().isNullOrEmpty() ||
                binding.passwordEdittext.text.toString().isNullOrEmpty()) {
            Toast.makeText(context,
                    "이메일과 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
        } else {
            loginEmail()
        }
    }

    fun loginEmail() {
        auth?.signInWithEmailAndPassword(binding.emailEdittext.text.toString(), binding.passwordEdittext.text.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        moveMainPage(auth?.currentUser)
                    } else {
                        Toast.makeText(context, "이메일이나 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            myAccountViewModel.onSignIn()
            val rootTransactionFragment = RootTransactionFragment()

            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, rootTransactionFragment, RootTransactionFragment.TAG)
                    .commitAllowingStateLoss()
        }
    }

    fun moveSignup() {
        val signUpFragment = SignupFragment()
        val tag = SignupFragment.TAG

        parentFragmentManager
                .beginTransaction()
                .hide(this@LoginFragment)
                .add(R.id.fragment_container_view, signUpFragment, tag)
                .addToBackStack(tag)
                .commitAllowingStateLoss()
    }

    fun moveEditPassword() {
        val editPasswordFragment = EditPasswordFragment()

        parentFragmentManager
            .beginTransaction()
            .hide(this@LoginFragment)
            .add(R.id.fragment_container_view, editPasswordFragment, tag)
            .addToBackStack(tag)
            .commitAllowingStateLoss()
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    moveMainPage(auth?.currentUser)
                }
            }
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickListener { emailLogin() }
        binding.btnSignup.setOnClickListener { moveSignup() }
        binding.btnEditPassword.setOnClickListener { moveEditPassword() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroy() {
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}