package com.jorgetargz.projectseeker.framework.common

import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.framework.main.MainActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.avatar.AvatarView
import timber.log.Timber

open class BaseAuthFragment : BaseFragment() {

    protected fun logInDone(currentUser: FirebaseUser) {
        unlockDrawer()
        setupDrawerData()
        goToHome()
        Timber.d("User logged in: ${currentUser.email}")
    }

    private fun unlockDrawer() {
        val activity = requireActivity() as MainActivity
        val drawer: DrawerLayout = activity.findViewById(R.id.drawer_layout)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun setupDrawerData() {
        val chatClient = ChatClient.instance()
        chatClient.getCurrentUser()?.let { currentStreamUser ->
            val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
            val headerView = navigationView.getHeaderView(0)
            val headerAvatar = headerView.findViewById<AvatarView>(R.id.avatarView)
            headerAvatar.setUserData(currentStreamUser)
            val headerName = headerView.findViewById<TextView>(R.id.name_textView)
            headerName.text = currentStreamUser.name
        }
    }

    private fun goToHome() {
        findNavController().popBackStack(R.id.homeFragment, true)
        findNavController().navigate(R.id.homeFragment)
    }
}