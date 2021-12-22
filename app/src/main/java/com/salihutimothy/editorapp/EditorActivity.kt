package com.salihutimothy.editorapp

import android.os.Bundle
import com.onegravity.rteditor.RTEditText
import com.onegravity.rteditor.RTManager
import com.onegravity.rteditor.RTToolbar
import com.onegravity.rteditor.api.RTApi
import com.onegravity.rteditor.api.RTMediaFactoryImpl
import com.onegravity.rteditor.api.RTProxyImpl
import android.content.Intent
import android.view.*

import com.salihutimothy.editorapp.FileHelper.pickDirectory
import com.salihutimothy.editorapp.FileHelper.pickFile
import java.io.File
import com.onegravity.rteditor.api.format.RTFormat

import android.app.Activity
import android.widget.EditText
import android.widget.Toast

import com.onegravity.rteditor.media.MediaUtils
import com.salihutimothy.editorapp.FileHelper.load
import com.salihutimothy.editorapp.FileHelper.save


class EditorActivity : EditorBaseActivity() {

    private val REQUEST_LOAD_FILE = 1
    private val REQUEST_SAVE_FILE = 2


    private lateinit var noteEditor : RTEditText
    private lateinit var signatureEditor : RTEditText
    private lateinit var subjectEditor : EditText
    private lateinit var rtManager : RTManager
    private var mUseDarkTheme = false
    private var mSplitToolbar = false

    override fun onCreate(savedInstanceState: Bundle?) {

        val subject: String
        var message: String? = null
        var signature: String? = null

        if (savedInstanceState == null) {
            val intent = intent
            subject = getStringExtra(intent, PARAM_SUBJECT)
            message = getStringExtra(intent, PARAM_MESSAGE)
            signature = getStringExtra(intent, PARAM_SIGNATURE)
            mUseDarkTheme = intent.getBooleanExtra(PARAM_DARK_THEME, false)
            mSplitToolbar = intent.getBooleanExtra(PARAM_SPLIT_TOOLBAR, false)
        } else {
            subject = savedInstanceState.getString(PARAM_SUBJECT, "")
            mUseDarkTheme = savedInstanceState.getBoolean(PARAM_DARK_THEME, false)
            mSplitToolbar = savedInstanceState.getBoolean(PARAM_SPLIT_TOOLBAR, false)
        }

        setTheme(if (mUseDarkTheme) R.style.RTE_ThemeDark else R.style.RTE_ThemeLight)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_editor)

        val rtApi = RTApi(this, RTProxyImpl(this), RTMediaFactoryImpl(this, true))
        rtManager = RTManager(rtApi, savedInstanceState)

        val toolbarContainer = findViewById<View>(R.id.rte_toolbar_container) as ViewGroup

        val rtToolbar = findViewById<View>(R.id.rte_toolbar) as RTToolbar
//        val rtToolbar1 = findViewById<View>(R.id.rte_toolbar_character) as RTToolbar
//        val rtToolbar2 = findViewById<View>(R.id.rte_toolbar_paragraph) as RTToolbar

        rtManager.registerToolbar(toolbarContainer, rtToolbar)
//        rtManager.registerToolbar(toolbarContainer, rtToolbar1)
//        rtManager.registerToolbar(toolbarContainer, rtToolbar2)

        noteEditor = findViewById(R.id.rtEditText_1)
        subjectEditor = findViewById(R.id.subject)
        signatureEditor = findViewById(R.id.rtEditText_2)

        subjectEditor.setText(subject)

        rtManager.registerToolbar(toolbarContainer, rtToolbar)
        rtManager.registerEditor(noteEditor, true)
        rtManager.registerEditor(signatureEditor, true)

        noteEditor.setRichTextEditing(true, message)
        noteEditor.setRichTextEditing(true, signature)

        noteEditor.requestFocus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        rtManager.onSaveInstanceState(outState)

        val subject: String = subjectEditor.text.toString()
        outState.putString(PARAM_SUBJECT, subject)

        outState.putBoolean(PARAM_DARK_THEME, mUseDarkTheme)
        outState.putBoolean(PARAM_SPLIT_TOOLBAR, mSplitToolbar)
    }

    override fun onDestroy() {
        super.onDestroy()
        rtManager.onDestroy(isFinishing)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // configure theme item
        var item: MenuItem = menu.findItem(R.id.theme)
        item.setTitle(if (mUseDarkTheme) R.string.menu_light_theme else R.string.menu_dark_theme)

        // configure split toolbar item
        item = menu.findItem(R.id.split_toolbar)
        item.setTitle(if (mSplitToolbar) R.string.menu_single_toolbar else R.string.menu_split_toolbar)

//        item = menu.findItem(R.id.editor_activity)
//        item?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId: Int = item.itemId
        when (itemId) {
            R.id.load -> {
                pickFile(this, REQUEST_LOAD_FILE)
                return true
            }
            R.id.save -> {
                /*
                 * Note that you need a third party file explorer that
                 * supports a pick directory Intent (like ES File Explorer or any
                 * Open Intent file explorer).
                 */
                val targetDir: File? = getExternalFilesDir(null)
                if (targetDir != null) {
                    pickDirectory(this, targetDir, REQUEST_SAVE_FILE)
                }
                return true
            }
            R.id.theme -> {
                mUseDarkTheme = !mUseDarkTheme
                startAndFinish(javaClass)
                return true
            }
            R.id.split_toolbar -> {
                mSplitToolbar = !mSplitToolbar
                startAndFinish(javaClass)
                return true
            }
            else -> return false
        }
    }

    private fun startAndFinish(clazz: Class<out Activity?>) {
        val subject: String = subjectEditor.text.toString()
        val signature: String = signatureEditor.getText(RTFormat.HTML)
        val message: String = noteEditor.getText(RTFormat.HTML)

        val intent = Intent(this, clazz)
            .putExtra(PARAM_DARK_THEME, mUseDarkTheme)
            .putExtra(PARAM_SPLIT_TOOLBAR, mSplitToolbar)
            .putExtra(PARAM_MESSAGE, message)
            .putExtra(PARAM_SUBJECT, subject)
            .putExtra(PARAM_SIGNATURE, signature)

        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && data.data != null && data.data!!
                .path != null
        ) {
            var filePath = data.data!!.path
            if (requestCode == REQUEST_SAVE_FILE) {
                /*
                 * Save file.
                 *
                 * Of course this is a hack but since this is just a demo
                 * to show how to integrate the rich text editor this is ok ;-)
                 */

                // write subject
                var targetFile = MediaUtils.createUniqueFile(File(filePath!!), "subject.html", true)
                val fileName = save(this, targetFile, subjectEditor.text.toString())

                // write message
                targetFile = File(targetFile.absolutePath.replace("subject_", "message_"))
                save(this, targetFile, noteEditor.getText(RTFormat.HTML))

                // write signature
                targetFile = File(targetFile.absolutePath.replace("message_", "signature_"))
                save(this, targetFile, signatureEditor.getText(RTFormat.HTML))

                if (fileName != null) {
                    val toastMsg = getString(R.string.save_as_success, fileName)
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
                }

            } else if (requestCode == REQUEST_LOAD_FILE) {
                /*
                 * Load File
                 *
                 * A hack, I know ...
                 */
                if (filePath!!.contains("message_")) {
                    filePath = filePath.replace("message_", "subject_")
                } else if (filePath.contains("signature_")) {
                    filePath = filePath.replace("signature_", "subject_")
                }
                if (filePath.contains("subject_")) {
                    // load subject
                    var s = load(this, filePath)
                    subjectEditor.setText(s)

                    // load message
                    filePath = filePath.replace("subject_", "message_")
                    s = load(this, filePath)
                    noteEditor.setRichTextEditing(true, s)

                    // load signature
                    filePath = filePath.replace("message_", "signature_")
                    s = load(this, filePath)
                    signatureEditor.setRichTextEditing(true, s)
                } else {
                    Toast.makeText(this, R.string.load_failure_1, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}