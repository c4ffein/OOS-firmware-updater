package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AsyncUnzip extends AsyncTask<Void, Integer, Integer> {
    private WeakReference<MainActivity> callerActivityWR;
    private Uri zipFileUri;
    private File targetDirectory;

    AsyncUnzip(Activity callerActivity, Uri zipFileUri, File targetDirectory) {
        this.callerActivityWR = new WeakReference<>((MainActivity) callerActivity);
        this.zipFileUri = zipFileUri;
        this.targetDirectory = targetDirectory;
    }

    // Adapted from https://stackoverflow.com/a/10997886
    @Override
    protected Integer doInBackground(Void... voids) {
        deleteFolderContent(targetDirectory);
        try {
            MainActivity callerActivity = callerActivityWR.get();
            if (callerActivity != null) {
                InputStream inputStream = callerActivity.getApplicationContext().getContentResolver().openInputStream(zipFileUri);
                if (inputStream == null)
                    return -2;
                try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))) {
                    ZipEntry ze;
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((ze = zis.getNextEntry()) != null) {
                        if(!isCancelled()) {
                            if (!ze.getName().matches("^((RADIO)|(firmware-update)|(META-INF)).*"))
                                continue;
                            File file = new File(targetDirectory, ze.getName());
                            File dir = ze.isDirectory() ? file : file.getParentFile();
                            if (!dir.isDirectory() && !dir.mkdirs())
                                throw new FileNotFoundException("Failed to ensure directory : " + dir.getAbsolutePath());
                            if (ze.isDirectory())
                                continue;
                            try (FileOutputStream fout = new FileOutputStream(file)) {
                                while ((count = zis.read(buffer)) != -1)
                                    fout.write(buffer, 0, count);
                            }
                        }
                    }
                    modifyUpdaterScript(targetDirectory);
                } catch (Exception e) {
                    return -1;
                }
                File emptyBootFile = new File(targetDirectory, "boot.img");
                if(!emptyBootFile.createNewFile())
                    return -5;
                File emptySystemDirectory = new File(targetDirectory, "system");
                if (!emptySystemDirectory.mkdirs()) {
                    return -4;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean deleteFolderContent(File targetDirectory) {
        if (!targetDirectory.isDirectory())
            return false;
        String[] children = targetDirectory.list();
        for (String aChildren : children) {
            File temp = new File(targetDirectory, aChildren);
            if (temp.isDirectory())
                if (!deleteFolderContent(temp))
                    return false;
            if (!temp.delete())
                return false;
        }
        return true;
    }

    private void modifyUpdaterScript(File targetDirectory) {
        StringBuilder updaterScript = new StringBuilder();
        File updaterScriptFile = new File(targetDirectory, "/META-INF/com/google/android/updater-script");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(updaterScriptFile));
            String line;
            boolean inBackup = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.matches("^(getprop\\(\"ro\\.display\\.series\"\\) ==).*")) {
                    updaterScript.append(line).append("\n");
                    updaterScript.append("ui_print(\"Flashing firmware...\");\n");
                }
                else if (line.matches("^ifelse\\(msm.boot_update\\(\"backup\"\\),\\s*\\(.*"))
                    inBackup = true;
                else if (line.matches("^\\),\\s*\"\"\\);.*"))
                    inBackup = false;
                else if (line.matches("^package_extract_file\\(\\s*\"((firmware-update)|(RADIO)).*"))
                    if (!inBackup)
                        updaterScript.append(line).append("\n");
            }
            updaterScript.append("ui_print(\"Firmware successfully flashed.\");\n");
            updaterScript.append("set_progress(1.000000);\n");
            bufferedReader.close();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(updaterScriptFile, false));
            bufferedWriter.write(updaterScript.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null)
            callerActivity.freeze();
    }

    @Override
    protected void onPostExecute(Integer result) {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null) {
            if (result == -2) {
                callerActivity.setTextOpenInputStreamError();
                callerActivity.setExtractAndFileSearchEnabled();
            } else if (result == -1) {
                callerActivity.setTextZipError();
                callerActivity.setExtractAndFileSearchEnabled();
            } else {
                callerActivity.setTextExtracted();
                callerActivity.setAllEnabled();
            }
            callerActivity.stopWheel();
        }
    }
}