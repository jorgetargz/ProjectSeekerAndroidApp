package com.jorgetargz.projectseeker.framework.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jorgetargz.projectseeker.databinding.FragmentHomeBinding
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding  = FragmentHomeBinding.inflate(inflater, container, false)


        return binding.root
    }

}