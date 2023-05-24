package com.jorgetargz.projectseeker.framework.common

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.framework.main.MainActivity
import kotlinx.coroutines.launch

open class BaseFragment : Fragment() {

    protected fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    fun observeErrorString(viewModel: BaseViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorString.collect { error ->
                    error?.let { showSnackbar(it) }
                    viewModel.errorStringHandled()
                }
            }
        }
    }

    fun observeErrorResourceCode(viewModel: BaseViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorResourceCode.collect { error ->
                    error?.let { showSnackbar(getString(it)) }
                    viewModel.errorResourceCodeHandled()
                }
            }
        }
    }

    fun observeLoading(viewModel: BaseViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        (activity as MainActivity).findViewById<ProgressBar>(R.id.progressBar).visibility =
                            View.VISIBLE
                        (activity as MainActivity).findViewById<FragmentContainerView>(R.id.fragmentContainerView).visibility =
                            View.INVISIBLE
                    } else {
                        (activity as MainActivity).findViewById<ProgressBar>(R.id.progressBar).visibility =
                            View.GONE
                        (activity as MainActivity).findViewById<FragmentContainerView>(R.id.fragmentContainerView).visibility =
                            View.VISIBLE
                    }
                }
            }
        }
    }
}

