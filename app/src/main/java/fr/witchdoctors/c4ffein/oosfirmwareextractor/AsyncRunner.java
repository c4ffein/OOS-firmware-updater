package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.File;

public class AsyncRunner {
    private static AsyncTask myAsyncTaskInstance = null;

    private static boolean checkInstanceReady(Activity callerActivity) {
        if (myAsyncTaskInstance != null && myAsyncTaskInstance.getStatus() == AsyncTask.Status.RUNNING) {
            if (myAsyncTaskInstance.isCancelled())
                Toast.makeText(callerActivity, "A task is already running cancelled, still using open files. Try later or restart app.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(callerActivity, "A task is already running, try later", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (myAsyncTaskInstance != null && myAsyncTaskInstance.getStatus() == AsyncTask.Status.PENDING) {
            Toast.makeText(callerActivity, "Task already pending.", Toast.LENGTH_LONG).show();
            return false;
        }
        return (myAsyncTaskInstance != null && myAsyncTaskInstance.getStatus() == AsyncTask.Status.FINISHED) || myAsyncTaskInstance == null;
    }

    public static AsyncTask getMD5Instance(Activity callerActivity, Uri fileUri) {
        if(checkInstanceReady(callerActivity))
            return myAsyncTaskInstance = new AsyncMD5(callerActivity, fileUri);
        return null;
    }

    public static AsyncTask getUnzipInstance(Activity callerActivity, Uri zipFileUri, File targetDirectory) {
        if(checkInstanceReady(callerActivity))
            return myAsyncTaskInstance = new AsyncUnzip(callerActivity, zipFileUri, targetDirectory);
        return null;
    }

    public static AsyncTask getCookInstance(Activity callerActivity, File sourceDirectory, String zipFilePath) {
        if(checkInstanceReady(callerActivity))
            return myAsyncTaskInstance = new AsyncCook(callerActivity, sourceDirectory, zipFilePath);
        return null;
    }
}