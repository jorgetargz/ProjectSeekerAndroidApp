package com.jorgetargz.projectseeker.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.jorgetargz.projectseeker.data.UsersRepository
import com.jorgetargz.projectseeker.data.dto.users.AddDeviceDTO
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MyFirebaseMessagingService() : FirebaseMessagingService() {

    @Inject
    lateinit var usersRepository: UsersRepository

    override fun onNewToken(token: String) {
        Timber.tag("FCM_TOKEN").d(token)

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser == null) {
            //Wait for login to send the token
            Timber.d("User not logged in yet")
            return
        }

        token?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val addDeviceDTO = AddDeviceDTO(token)
                usersRepository.addDevice(addDeviceDTO).catch {
                    Timber.e("Error sending token")
                }.collect { networkResult ->
                    when (networkResult) {
                        is NetworkResult.Success -> {
                            Timber.d("Token sent")
                        }

                        is NetworkResult.Error -> {
                            Timber.e("Error sending token")
                        }

                        is NetworkResult.Loading -> {}//DO Nothing
                    }
                }
            }
        }
    }
}