package com.salihutimothy.editorapp

import android.Manifest
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean


open class EditorBaseActivity : AppCompatActivity() {
    private val mRequestPermissionsInProcess = AtomicBoolean()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            val tmp = savedInstanceState.getBoolean(PARAM_REQUEST_IN_PROCESS, false)
            mRequestPermissionsInProcess.set(tmp)
        }
        checkPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PARAM_REQUEST_IN_PROCESS, mRequestPermissionsInProcess.get())
    }

    private fun checkPermissions(permissions: Array<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissionInternal(permissions)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermissionInternal(permissions: Array<String>): Boolean {
        val requestPerms = ArrayList<String>()
        for (permission in permissions) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED && !userDeniedPermissionAfterRationale(
                    permission
                )
            ) {
                requestPerms.add(permission)
            }
        }
        if (requestPerms.size > 0 && !mRequestPermissionsInProcess.getAndSet(true)) {
            //  We do not have this essential permission, ask for it
            requestPermissions(requestPerms.toTypedArray(), REQUEST_PERMISSION)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            var i = 0
            val len = permissions.size
            while (i < len) {
                val permission = permissions[i]
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (Manifest.permission.WRITE_EXTERNAL_STORAGE == permission) {
                        showRationale(permission, R.string.permission_denied_storage)
                    }
                }
                i++
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showRationale(permission: String, promptResId: Int) {
        if (shouldShowRequestPermissionRationale(permission) && !userDeniedPermissionAfterRationale(
                permission
            )
        ) {

            //  Notify the user of the reduction in functionality and possibly exit (app dependent)
            AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied)
                .setMessage(promptResId)
                .setPositiveButton(R.string.permission_deny,
                    DialogInterface.OnClickListener { dialog, which ->
                        try {
                            dialog.dismiss()
                        } catch (ignore: Exception) {
                        }
                        setUserDeniedPermissionAfterRationale(permission)
                        mRequestPermissionsInProcess.set(false)
                    })
                .setNegativeButton(R.string.permission_retry,
                    DialogInterface.OnClickListener { dialog, which ->
                        try {
                            dialog.dismiss()
                        } catch (ignore: Exception) {
                        }
                        mRequestPermissionsInProcess.set(false)
                        checkPermissions(arrayOf(permission))
                    })
                .show()
        } else {
            mRequestPermissionsInProcess.set(false)
        }
    }

    private fun userDeniedPermissionAfterRationale(permission: String): Boolean {
        val sharedPrefs = getSharedPreferences(javaClass.simpleName, MODE_PRIVATE)
        return sharedPrefs.getBoolean(PREFERENCE_PERMISSION_DENIED + permission, false)
    }

    private fun setUserDeniedPermissionAfterRationale(permission: String) {
        val editor = getSharedPreferences(javaClass.simpleName, MODE_PRIVATE).edit()
        editor.putBoolean(PREFERENCE_PERMISSION_DENIED + permission, true).commit()
    }

    protected fun getStringExtra(intent: Intent, key: String?): String {
        val s = intent.getStringExtra(key)
        return s ?: ""
    }

    companion object {
        const val PARAM_SUBJECT = "subject"
        const val PARAM_MESSAGE = "message"
        const val PARAM_SIGNATURE = "signature"
        const val PARAM_DARK_THEME = "useDarkTheme"
        const val PARAM_SPLIT_TOOLBAR = "splitToolbar"
        const val PARAM_REQUEST_IN_PROCESS = "requestPermissionsInProcess"
        private const val REQUEST_PERMISSION = 3
        private const val PREFERENCE_PERMISSION_DENIED = "PREFERENCE_PERMISSION_DENIED"
    }
}