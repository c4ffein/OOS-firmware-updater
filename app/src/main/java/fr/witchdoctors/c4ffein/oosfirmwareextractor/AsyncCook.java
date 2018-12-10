package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AsyncCook extends AsyncTask<Void, Integer, Integer> {
    private final WeakReference<MainActivity> callerActivityWR;
    private File sourceDirectory;
    private String zipFilePath;
    private static AsyncTask<Void, Integer, Integer> myAsyncTaskInstance = null;

    private AsyncCook(Activity callerActivity, File sourceDirectory, String zipFilePath) {
        this.callerActivityWR = new WeakReference<>((MainActivity) callerActivity);
        this.sourceDirectory = sourceDirectory;
        this.zipFilePath = zipFilePath;
    }

    public static AsyncTask<Void, Integer, Integer> getInstance(Activity callerActivity, String zipFilePath, File sourceDirectory) {
        if (myAsyncTaskInstance != null && myAsyncTaskInstance.getStatus() == Status.RUNNING) {
            if (myAsyncTaskInstance.isCancelled())
                Toast.makeText(callerActivity, "A task is already running cancelled, still using open files. Try later or restart app.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(callerActivity, "A task is already running, try later", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (myAsyncTaskInstance != null && myAsyncTaskInstance.getStatus() == Status.PENDING) {
            Toast.makeText(callerActivity, "Task already pending.", Toast.LENGTH_LONG).show();
            return null;
        }
        if (myAsyncTaskInstance != null && myAsyncTaskInstance.getStatus() == Status.FINISHED)
            myAsyncTaskInstance = new AsyncCook(callerActivity, sourceDirectory, zipFilePath);
        if (myAsyncTaskInstance == null)
            myAsyncTaskInstance = new AsyncCook(callerActivity, sourceDirectory, zipFilePath);
        return myAsyncTaskInstance;
    }

    // Adapted from https://stackoverflow.com/a/14868161
    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            FileOutputStream dest = new FileOutputStream(zipFilePath);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            if (sourceDirectory.isDirectory())
                zipSubFolder(out, sourceDirectory, sourceDirectory.getPath().length());
            else
                Log.d("Zipping", "Source File is not a directory");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    // From https://stackoverflow.com/a/14868161
    private void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;
        File[] fileList = folder.listFiles();
        BufferedInputStream origin;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // To keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
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
            callerActivity.setTextCooked();
            callerActivity.setAllEnabled();
            callerActivity.stopWheel();
        }
    }
}