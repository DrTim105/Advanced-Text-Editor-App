package com.salihutimothy.editorapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.onegravity.rteditor.RTEditText
import com.onegravity.rteditor.RTManager
import com.onegravity.rteditor.RTToolbar
import com.onegravity.rteditor.api.RTApi
import com.onegravity.rteditor.api.RTMediaFactoryImpl
import com.onegravity.rteditor.api.RTProxyImpl

class EditorActivity : AppCompatActivity() {

    private lateinit var editor : RTEditText
    private lateinit var rtManager : RTManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.RTE_ThemeLight);
        setContentView(R.layout.activity_editor)

        val rtApi = RTApi(this, RTProxyImpl(this), RTMediaFactoryImpl(this, true))
        rtManager = RTManager(rtApi, savedInstanceState)

        val toolbarContainer = findViewById<View>(R.id.rte_toolbar_container) as ViewGroup
        val rtToolbar = findViewById<View>(R.id.rte_toolbar) as RTToolbar
        editor = findViewById(R.id.rtEditText_1)

        rtManager.registerToolbar(toolbarContainer, rtToolbar)
        rtManager.registerEditor(editor, true)

        editor.setRichTextEditing(true, "My content");


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        rtManager.onDestroy(isFinishing)
    }
}