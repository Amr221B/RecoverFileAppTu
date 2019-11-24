package tr.easyrecover.turk.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tr.easyrecover.turk.R;
import tr.easyrecover.turk.activity.VideoActivity;
import tr.easyrecover.turk.activity.VideoListActivity;
import com.cleveroad.androidmanimation.LoadingAnimationView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class VideoScanner extends AsyncTask<Void, HashMap<String, ArrayList<String>>, Void> {

    public static HashMap<String, ArrayList<String>> folderImage = new HashMap();
    private Activity activity;
    int count = 0;

    private TextView number_text;
    private LoadingAnimationView progressBar;
    private ImageView scan_icon;
    private LinearLayout show_button;
    private TextView show_text;
    long total_size = 0;


    public VideoScanner(Activity atv, TextView num_text, ImageView icon) {

        this.activity = atv;
        this.number_text = num_text;
        this.scan_icon = icon;
        this.count = 0;

    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Void doInBackground(Void... params) {

        Stack<String> stack = new Stack();
        HashSet<String> storages = Utils.getExternalMounts();
        if (storages != null && storages.size() > 0) {
            Iterator iterator = storages.iterator();
            while (iterator.hasNext()) {
                File[] folders = new File((String) iterator.next()).listFiles();
                if (folders != null && folders.length >= 1) {
                    for (File path : folders) {
                        stack.push(path.getAbsolutePath());
                    }
                }
            }
        }
        while (!stack.isEmpty()) {
            String currentPath = (String) stack.pop();
            File file = new File(currentPath);
            if (!file.isFile()) {
                File[] fileList = file.listFiles();
                if (fileList != null && fileList.length >= 1) {
                    ArrayList<String> listImage = new ArrayList();
                    for (File i : fileList) {
                        if (i.isDirectory()) {
                            stack.push(i.getAbsolutePath());
                        } else if (currentPath.contains("/.") && Utils.isVideoFile(i)) {
                            listImage.add(i.getAbsolutePath());
                            this.total_size += i.length();
                            this.count++;
                        }
                    }
                    if (listImage.size() > 0) {
                        folderImage.put(currentPath, listImage);
                        publishProgress(new HashMap[]{folderImage});
                    }
                }
            }
        }
        return null;
    }

    protected void onProgressUpdate(HashMap<String, ArrayList<String>>... values) {

        super.onProgressUpdate(values);
        if (this.count != 0) {
            this.number_text.setText(this.activity.getResources().getString(R.string.number_video) + " " + this.count + " " + this.activity.getResources().getString(R.string.video) + " (" + convertStorage(this.total_size) + ")");
        } else {
            this.number_text.setText(R.string.no_video);
        }

    }

    @SuppressLint({"WrongConstant"})
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (this.count != 0) {


            Intent intent = new Intent(VideoScanner.this.activity, VideoListActivity.class);
            intent.putExtra("size", VideoScanner.this.count);
            VideoScanner.this.activity.startActivity(intent);

            VideoScanner.this.number_text.setVisibility(4);
            return;
        } else {

            VideoActivity.videoActivity.finish();
            this.number_text.setText(this.activity.getResources().getString(R.string.no_video));
        }


    }

    public static String convertStorage(long size) {

        if (size >= 1073741824) {
            return String.format("%.1f GB", new Object[]{Float.valueOf(((float) size) / 1.07374182E9f)});
        } else if (size >= 1073741824) {
            return String.format(((float) size) / 1048576.0f > 100.0f ? "%.0f MB" : "%.1f MB", new Object[]{Float.valueOf(((float) size) / 1048576.0f)});
        } else if (size >= 1 << 10) {
            return String.format(((float) size) / 1024.0f > 100.0f ? "%.0f KB" : "%.1f KB", new Object[]{Float.valueOf(((float) size) / 1024.0f)});
        } else {
            return String.format("%d B", new Object[]{Long.valueOf(size)});
        }
    }
}
