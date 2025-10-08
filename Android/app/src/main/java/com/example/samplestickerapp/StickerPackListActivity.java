/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StickerPackListActivity extends AddStickerPackActivity {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ArrayList<StickerPack> stickerPackList;
    private static final String TAG = "AddStickerPackActivity";
    private ActivityResultLauncher<Uri> folderPickerLauncher;


    @OptIn(markerClass = UnstableApi.class) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);
        packRecyclerView = findViewById(R.id.sticker_pack_list);
        stickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
        showStickerPackList(stickerPackList);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, stickerPackList.size()));
        }
        // vamos a solicitar permiso para leer archivos
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        // El usuario ha seleccionado una carpeta.
                        androidx.media3.common.util.Log.d(TAG, "Carpeta seleccionada por el usuario: " + uri.toString());

                        // Tomar permisos persistentes para mantener el acceso después de que la app se reinicie.
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }

                        // Guardar el URI de la carpeta en SharedPreferences para uso futuro.
                        saveFolferUri(uri);

                        // Ahora que tenemos el permiso, podemos leer los archivos.
                        // Llama aquí a la función que procesa los stickers.
                        androidx.media3.common.util.Log.d(TAG, "Procediendo a leer archivos de la carpeta seleccionada.");
                        loadFilesFromFolder(uri);

                    } else {
                        // El usuario canceló la selección.
                        androidx.media3.common.util.Log.w(TAG, "El usuario no seleccionó ninguna carpeta.");
                        Toast.makeText(this, "No se seleccionó una carpeta.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        Button selectFolderButton = findViewById(R.id.id_del_boton_para_seleccionar_carpeta);
        selectFolderButton.setOnClickListener(v -> openFolderPicker());
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[0]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    protected void openFolderPicker() {
        Log.d(TAG, "openFolderPicker");
        folderPickerLauncher.launch(null);
    }

    /**
     * Guardar el URI de la carpeta en SharedPreferences
     * @param uri El uri de la carpeta a guardar
     */
    void saveFolferUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("folder_uri", uri.toString()).apply();
    }

    /**
     * Recupera el URI de la carpeta guardada desde SharedPreferences.
     * @return El URI guardado, o null si no existe.
     */
    @Nullable
    protected Uri getSavedFolderUri() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String uriString = prefs.getString("folder_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    /**
     * Lee y procesa los archivos dentro de la carpeta seleccionada
     * @param folderUri El URI de la carpeta seleccionada
     */
    void loadFilesFromFolder(Uri folderUri) {
        // Object file que representa el directorio
        DocumentFile directory = DocumentFile.fromTreeUri(this, folderUri);
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            Log.e(TAG, "Invalid folder URI: " + folderUri);
            return;
        }

        for (DocumentFile file : directory.listFiles()) {
            if (file.isFile()) {
                // Procesar el archivo
                Log.d(TAG, "Procesando archivo: " + file.getName());

                try {
                    new StickerContentProvider().openAssetFile(file.getUri(), "r");
                    InputStream inputStream = getContentResolver().openInputStream(file.getUri());
                    // Aquí puedes, por ejemplo, decodificar una imagen:
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    Log.e(TAG, "Error al procesar archivo: " + file.getName(), e);
                }
            } else if (file.isDirectory()) {
                // También puedes manejar subcarpetas si es necesario
                Log.d(TAG, "Subdirectorio encontrado: " + file.getName());
            }
        }
    }

    private void showStickerPackList(List<StickerPack> stickerPackList) {
        allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList, onAddButtonClickedListener);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(RecyclerView.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }


    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> addStickerPackToWhatsApp(pack.identifier, pack.name);


    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
            final int max = Math.max(widthOfImageRow / previewSize, 1);
            int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            int minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
            allStickerPacksListAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
        }
    }


    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return Arrays.asList(stickerPackArray);
            }
            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }
}
