package com.sadataljony.app.android.inappupdate

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.sadataljony.app.android.inappupdate.databinding.ActivityInAppUpdateBinding

class InAppUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInAppUpdateBinding
    private lateinit var mAppUpdateManager: AppUpdateManager
    private val RC_APP_UPDATE = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_update)
        updateApp()
    }

    private fun updateApp() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        mAppUpdateManager.registerListener(installStateUpdatedListener)
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)//AppUpdateType.IMMEDIATE
            ) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,//AppUpdateType.IMMEDIATE
                        this,
                        RC_APP_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbar()
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else")
            }
        }
    }

    private var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackbar()
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    mAppUpdateManager.unregisterListener(this)
                } else {
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus())
                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: app download failed")
            }
        }
    }

    private fun popupSnackbar() {
        val snackbar: Snackbar = Snackbar.make(
            binding.root,
            "New app is ready!",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("Install") { view ->
            if (mAppUpdateManager != null) {
                mAppUpdateManager.completeUpdate()
            }
        }
        snackbar.setActionTextColor(resources.getColor(R.color.black))
        snackbar.show()
    }

}