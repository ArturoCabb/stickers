package com.example.recuperadordestickers

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recuperadordestickers.models.Sticker

class MainActivity : AppCompatActivity() {
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Uri?>
    private val PREFS_NAME = "StickerAppPrefs"
    private val KEY_FOLDER_URI = "folder_uri"
    private lateinit var stickerAdapter: StickerAdapter
    private val stickers = mutableListOf<Sticker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.listaStickers)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        stickerAdapter = StickerAdapter(stickers)
        recyclerView.adapter = stickerAdapter

        folderPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                Log.d("MainActivity", "URI de la carpeta seleccionada: $uri")
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                saveFolderUri(uri)
                loadStickersFromUri(uri)
            } else {
                Log.d("MainActivity", "El usuario no seleccionó ninguna carpeta.")
                Toast.makeText(this, "No se seleccionó ninguna carpeta.", Toast.LENGTH_SHORT).show()
            }
        }

        val savedUri = getSavedFolderUri()
        if (savedUri != null && hasPersistentUriPermission(savedUri)) {
            Toast.makeText(this, "Acceso a carpeta restaurado.", Toast.LENGTH_SHORT).show()
            loadStickersFromUri(savedUri)
        } else {
            Toast.makeText(this, "Acceso a carpeta no disponible.", Toast.LENGTH_SHORT).show()
        }
    }

    fun askAFolder(view: View) {
        folderPickerLauncher.launch(null)
    }

    private fun loadStickersFromUri(folderUri: Uri) {
        val folder = DocumentFile.fromTreeUri(this, folderUri)
        if (folder != null && folder.isDirectory) {
            stickers.clear()
            for (file in folder.listFiles()) {
                if (file.isFile && file.name?.endsWith(".webp") == true) {
                    stickers.add(Sticker(file.uri))
                }
            }
            stickerAdapter.notifyDataSetChanged()
        }
    }

    private fun hasPersistentUriPermission(uri: Uri): Boolean {
        val persistedPermission = contentResolver.persistedUriPermissions
        return persistedPermission.any { it.uri == uri && it.isReadPermission }
    }

    private fun saveFolderUri(uri: Uri) {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FOLDER_URI, uri.toString()).apply()
        Log.d("Main Activity", "Uri saved")
    }

    private fun getSavedFolderUri(): Uri? {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_FOLDER_URI, null)
        return if (uriString != null) Uri.parse(uriString) else null
    }
}