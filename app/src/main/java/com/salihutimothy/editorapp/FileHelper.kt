package com.salihutimothy.editorapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.onegravity.rteditor.utils.Helper
import com.onegravity.rteditor.utils.io.IOUtils
import java.io.*
import java.lang.Exception
import android.os.StrictMode
import java.lang.reflect.Method


object FileHelper {
    private val PICK_DIRECTORY_INTENTS = arrayOf(
        arrayOf("org.openintents.action.PICK_DIRECTORY", "file://"),
        arrayOf("com.estrongs.action.PICK_DIRECTORY", "file://"),
        arrayOf(Intent.ACTION_PICK, "folder://"),
        arrayOf("com.androidworkz.action.PICK_DIRECTORY", "file://")
    )

    /**
     * Tries to open a known file browsers to pick a directory.
     *
     * @return True if a filebrowser has been found (the result will be in the onActivityResult), False otherwise
     */

    fun pickDirectory(activity: Activity, startPath: File, requestCode: Int): Boolean {
        val packageMgr = activity.packageManager
        for (intent in PICK_DIRECTORY_INTENTS) {
            val intentAction = intent[0]
            val uriPrefix = intent[1]

//            val uri = FileProvider.getUriForFile(
//                activity,
//                BuildConfig.APPLICATION_ID + ".provider",
//                startPath
//            )


            val startIntent = Intent(intentAction)
//                .putExtra("org.openintents.extra.TITLE", activity.getString(R.string.save_as))
                .putExtra("org.openintents.extra.TITLE", "Save as...")
                .setData(Uri.parse(uriPrefix + startPath.path))
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val m: Method = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
            try {
                if (startIntent.resolveActivity(packageMgr) != null) {
                    activity.startActivityForResult(startIntent, requestCode)

                    return true
                }
            } catch (e: ActivityNotFoundException) {
                showNoFilePickerError(activity, e)
            }
        }
        return false
    }

    fun pickFile(activity: Activity, requestCode: Int): Boolean {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "file/*"
            activity.startActivityForResult(intent, requestCode)
            return true
        } catch (e: ActivityNotFoundException) {
            showNoFilePickerError(activity, e)
        }
        return false
    }

    private fun showNoFilePickerError(context: Context, e: Exception) {
        val msg = "There is no app installed to handle picking a directory ${e.message}"
//        val msg = context.getString(R.string.no_file_picker, e.message)
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    fun save(context: Context, outFile: File, html: String?): String? {
        var `in`: Reader? = null
        var out: Writer? = null
        try {
            `in` = StringReader(html)
            out = FileWriter(outFile)
            IOUtils.copy(`in`, out)
            return outFile.absolutePath
        } catch (ioe: IOException) {
            val toastMsg = "Text could not be saved ${ioe.message}"
//            val toastMsg = context.getString("Text could not be saved", ioe.message)
            Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
        } finally {
            Helper.closeQuietly(`in`)
            Helper.closeQuietly(out)
        }
        return null
    }

    fun load(context: Context, filePath: String?): String? {
        val inFile = File(filePath)
        var `in`: Reader? = null
        var out: Writer? = null
        try {
            `in` = FileReader(inFile)
            out = StringWriter()
            IOUtils.copy(`in`, out)
            return out.toString()
        } catch (ioe: IOException) {
            val toastMsg = "This is not a valid rich text editor file ${ioe.message}"
//            val toastMsg = context.getString(R.string.load_failure_2, ioe.message)
            Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
        } finally {
            Helper.closeQuietly(`in`)
            Helper.closeQuietly(out)
        }
        return null
    }
}