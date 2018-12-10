package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static fr.witchdoctors.c4ffein.oosfirmwareextractor.R.id.text_extracted;
import static fr.witchdoctors.c4ffein.oosfirmwareextractor.R.id.text_file_selected;
import static fr.witchdoctors.c4ffein.oosfirmwareextractor.R.id.text_md5;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 23;
    private Uri romUri;
    private Button extractButton;
    private Button fileSearchButton;
    private Button checkButton;
    private Button cookButton;
    private TextView textFileSelected;
    private TextView textMD5;
    private TextView textExtracted;

    /**
     * There is a bug on the emulator, you have to keep pressing the zip file and choose to open,
     * otherwise it would just traverse it. It shouldn't work that way on real device.
     */
    public void performFileChoose() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip"); // On emulator seems to bug, descending in zip file, but it should be ok on real devices
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                this.romUri = resultData.getData();
                textFileSelected.setText(getFileName(this.romUri));
                AsyncMD5 asyncMD5 = (AsyncMD5) AsyncMD5.getInstance(MainActivity.this, romUri);
                if (asyncMD5 != null) {
                    asyncMD5.execute();
                } else {
                    Log.i("AsyncMD5 caller", "try later");
                }
            }
        }
    }

    // From https://stackoverflow.com/a/25005243
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission acquired. Now you can cook!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "No WRITE_EXTERNAL_STORAGE permission :(",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void callTheCook(String outputPath, File cacheDir) {
        AsyncCook asyncCook = (AsyncCook) AsyncCook.getInstance(MainActivity.this, outputPath, cacheDir);
        if (asyncCook != null)
            asyncCook.execute();
        else
            Log.i("AsyncCook caller", "try later");
    }

    public void setMD5(String MD5) {
        textMD5.setText(MD5);
    }

    private void listFiles(File cacheDir, ArrayList<LevelAndName> list, int level) {
        if (!cacheDir.isDirectory())
            list.add(new LevelAndName(level, cacheDir.getName()));
        else {
            list.add(new LevelAndName(level, cacheDir.getName() + "/"));
            File[] files = cacheDir.listFiles();
            for (File file : files) listFiles(file, list, level + 1);
        }
    }

    private void listCache(File cacheDir, ArrayList<LevelAndName> list) {
        if (!cacheDir.isDirectory())
            return;
        File[] files = cacheDir.listFiles();
        for (File file : files) listFiles(file, list, 0);
    }

    public void checkFiles(File cacheDir) {
        StringBuilder updaterScript = new StringBuilder();
        File updaterScriptFile = new File(cacheDir, "/META-INF/com/google/android/updater-script");
        try {
            BufferedReader bis = new BufferedReader(new FileReader(updaterScriptFile));
            String line;
            while ((line = bis.readLine()) != null) {
                updaterScript.append(line).append("\n");
            }
            bis.close();
        } catch (FileNotFoundException e) {
            setTextUpdaterScriptNotFound();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<LevelAndName> fileList = new ArrayList<>();
        listCache(cacheDir, fileList);
        Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra("updaterScript", updaterScript.toString());
        intent.putExtra("fileList", fileList);
        startActivity(intent);
    }

    public void freeze() {
        extractButton.setEnabled(false);
        fileSearchButton.setEnabled(false);
        checkButton.setEnabled(false);
        cookButton.setEnabled(false);
        startWheel();
    }

    public void stopWheel() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void startWheel() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void setFileSearchEnabled() {
        fileSearchButton.setEnabled(true);
    }

    public void setExtractAndFileSearchEnabled() {
        extractButton.setEnabled(true);
        fileSearchButton.setEnabled(true);
    }

    public void setAllEnabled() {
        extractButton.setEnabled(true);
        fileSearchButton.setEnabled(true);
        checkButton.setEnabled(true);
        cookButton.setEnabled(true);
    }

    public void setTextExtracted() {
        textExtracted.setText(getString(R.string.text_extracted));
    }

    public void setTextOpenInputStreamError() {
        textExtracted.setText(getString(R.string.text_open_input_stream_error));
    }

    public void setTextZipError() {
        textExtracted.setText(getString(R.string.text_zip_error));
    }

    public void setTextUpdaterScriptNotFound() {
        textExtracted.setText(getString(R.string.text_updater_script_not_found));
    }

    public void setTextCooked() {
        textExtracted.setText(getString(R.string.text_cooked));
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final File cacheDir = getCacheDir();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String outputPath = Environment.getExternalStorageDirectory() + "/" + "firmwareupdater.zip";

        stopWheel();

        fileSearchButton = findViewById(R.id.select_button);
        fileSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileChoose();
            }
        });
        extractButton = findViewById(R.id.extract_button);
        extractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncUnzip asyncUnzip = (AsyncUnzip) AsyncUnzip.getInstance(MainActivity.this, romUri, cacheDir);
                if (asyncUnzip != null) {
                    asyncUnzip.execute();
                } else {
                    Log.i("AsyncUnzip caller", "try later");
                }
            }
        });
        checkButton = findViewById(R.id.check_button);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFiles(cacheDir);
            }
        });
        cookButton = findViewById(R.id.cook_button);
        cookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PERMISSION ERROR", "NO WRITE SDCARD");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else
                    callTheCook(outputPath, cacheDir);
            }
        });
        textFileSelected = findViewById(text_file_selected);
        textMD5 = findViewById(text_md5);
        textExtracted = findViewById(text_extracted);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("extractButton", extractButton.isEnabled());
        savedInstanceState.putBoolean("fileSearchButton", fileSearchButton.isEnabled());
        savedInstanceState.putBoolean("checkButton", checkButton.isEnabled());
        savedInstanceState.putBoolean("cookButton", cookButton.isEnabled());
        savedInstanceState.putString("romUri", romUri == null ? "" : romUri.toString());
        savedInstanceState.putString("textFileSelected", textFileSelected.toString());
        savedInstanceState.putString("textMD5", textMD5.getText().toString());
        savedInstanceState.putString("textExtracted", textExtracted.toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        extractButton.setEnabled(savedInstanceState.getBoolean("extractButton"));
        fileSearchButton.setEnabled(savedInstanceState.getBoolean("fileSearchButton"));
        checkButton.setEnabled(savedInstanceState.getBoolean("checkButton"));
        cookButton.setEnabled(savedInstanceState.getBoolean("cookButton"));
        romUri = Uri.parse(savedInstanceState.getString("romUri"));
        textMD5.setText(savedInstanceState.getString("textMD5"));
    }
}
