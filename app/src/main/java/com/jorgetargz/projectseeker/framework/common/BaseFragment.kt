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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseFragment : Fragment() {

    protected fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    protected fun observeStateFlowOnStarted(observeStateFlow: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeStateFlow()
            }
        }
    }

    protected fun observeErrorString(errorString: StateFlow<String?>, handleErrorString: () -> Unit) {
        observeStateFlowOnStarted {
            errorString.collect { error ->
                error?.let { showSnackbar(it) }
                handleErrorString()
            }
        }
    }

    protected fun observeErrorResourceCode(errorResourceCode: StateFlow<Int?>, handleErrorCode: () -> Unit) {
        observeStateFlowOnStarted {
            errorResourceCode.collect { error ->
                error?.let { showSnackbar(getString(it)) }
                handleErrorCode()
            }
        }
    }


    protected fun observeLoading(isLoading: StateFlow<Boolean>) {
        observeStateFlowOnStarted {
            isLoading.collect { isLoading ->
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

