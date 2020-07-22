package com.nalsasupport.nalsaacademy.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.jarvanmo.exoplayerview.media.SimpleMediaSource;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;
import com.nalsasupport.nalsaacademy.R;
import com.nalsasupport.nalsaacademy.model.QualityDetails;
import com.nalsasupport.nalsaacademy.model.Videos;

import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;

public class ExoplayerActivity extends AppCompatActivity {

    private ExoVideoView videoView;
    private View contentView;
    private String quality360, quality720, quality1080;
    private ArrayList<QualityDetails> qualityList;
    private ArrayList<String> qualityNames;
    private ProgressDialog progressDialog;
    private Spinner spinner;
    private static Videos video;
    //private List<ExoMediaSource.Quality> qualities;
    private SimpleMediaSource mediaSource;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        toolbar = findViewById(R.id.toolbar_ExoplayerActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        contentView = findViewById(R.id.activity_exoplayer);
        videoView = findViewById(R.id.videoView);
        spinner = findViewById(R.id.spinner_quality_ExoplayerActivity);
        qualityList = new ArrayList<>();
        qualityNames = new ArrayList<>();
        progressDialog = new ProgressDialog(ExoplayerActivity.this);
        progressDialog.show();
        progressDialog.setTitle("Loading...");
        if (getIntent().getSerializableExtra("video") != null) {
            video = (Videos) getIntent().getSerializableExtra("video");
        }

        videoView.setBackListener((view, isPortrait) -> {
            if (isPortrait) {
                finish();
            }
            return false;
        });


        videoView.setOrientationListener(orientation -> {
            Log.i("CUSTOM", "orientation changed");
            if (orientation == SENSOR_PORTRAIT) {
                Log.i("CUSTOM", "orientation = P");
                changeToPortrait();
            } else if (orientation == SENSOR_LANDSCAPE) {
                Log.i("CUSTOM", "orientation = L");
                changeToLandscape();
            }
        });

        extractYoutubeUrl();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mediaSource = new SimpleMediaSource(qualityList.get(position).getLink());
                mediaSource.setDisplayName(video.getTitle());
                videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                videoView.play(mediaSource, true);

                if (qualityList.get(position).getLink().equals(mediaSource.uri().toString())) {
                    Log.i("CUSTOM", "TRUE, uri and url is the same.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CUSTOM", "clicked on = " + v.getTag() + "\n - " + v.getId());
            }
        });

/*videoView.setMultiQualitySelectorNavigator(new MultiQualitySelectorAdapter.MultiQualitySelectorNavigator() {
            @Override
            public boolean onQualitySelected(ExoMediaSource.Quality quality) {
                Uri uri = null;
                Log.i("CUSTOM","quality = "+quality.getDisplayName());
                if (quality.getDisplayName().equals("360p")) {
                    uri = qualities.get(0).getUri();
                } else {
                    uri = qualities.get(1).getUri();
                }
                quality.setUri(uri);
                return false;
            }
        });


play.setOnClickListener(view -> {
            videoView.play(mediaSource);
            play.setVisibility(View.INVISIBLE);
        });

    }*/

    }

    private void extractYoutubeUrl() {
        Log.i("KARAN", "extractYoutubeUrl()");
        try {
            @SuppressLint("StaticFieldLeak") YouTubeExtractor mExtractor = new YouTubeExtractor(this) {
                @Override
                protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                    if (sparseArray != null) {
                        //playVideo();
                        quality360 = sparseArray.get(18).getUrl();
                        quality720 = sparseArray.get(22).getUrl();
                        //quality1080 = sparseArray.get(137).getUrl();
                        qualityList.clear();
                        qualityNames.clear();
                        qualityNames.add("360p");
                        qualityNames.add("720p");
                        //qualityNames.add("1080p");
                        qualityList.add(new QualityDetails("360p", sparseArray.get(18).getUrl()));
                        qualityList.add(new QualityDetails("720p", sparseArray.get(22).getUrl()));
                        //qualityList.add(new QualityDetails("1080p", sparseArray.get(137).getUrl()));
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ExoplayerActivity.this, android.R.layout.simple_list_item_1,
                                qualityNames);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(1, false);

                        /*qualities = new ArrayList<>();
                        ExoMediaSource.Quality quality;
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.WHITE);
                        SpannableString spannableString = new SpannableString("360p");
                        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        quality = new SimpleQuality(spannableString, Uri.parse(sparseArray.get(18).getUrl()));
                        qualities.add(quality);
                        spannableString = new SpannableString("720p");
                        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        quality = new SimpleQuality(spannableString, Uri.parse(sparseArray.get(22).getUrl()));
                        qualities.add(quality);

                        mediaSource = new SimpleMediaSource(sparseArray.get(18).getUrl());
                        mediaSource.setQualities(qualities);
*/


                        //videoView.play(mediaSource, false);
                    } else {
                        Log.i("KARAN", "sparse array null");
                    }
                    progressDialog.dismiss();
                }
            };
            String videoid = video.getVideoId();
            String baseurl = "https://www.youtube.com";
            String mYoutubeLink = baseurl + "/watch?v=" + videoid;
            Log.i("KARAN", "video link = " + mYoutubeLink);
            mExtractor.extract(mYoutubeLink, true, true);
        } catch (Exception e) {
            Log.i("KARAN", "exception = " + e.getMessage());
        }
    }

    private void changeToPortrait() {

        WindowManager.LayoutParams attr = getWindow().getAttributes();
//        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.setAttributes(attr);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        spinner.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
    }

    private void changeToLandscape() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = getWindow();
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        spinner.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT > 23) {
            videoView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Build.VERSION.SDK_INT <= 23)) {
            videoView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= 23) {
            videoView.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > 23) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.releasePlayer();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("CUSTOM", "keycode = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i("CUSTOM", "keycode back");
            return videoView.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setCancelable(true);
        String msg = "\nVideo Title :\n" + video.getTitle() + "\n\nDescription :\n" + video.getDescription();
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(msg);
        stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.nalsa_blue)),
                msg.indexOf("Video Title"), ("Video Title".length() + msg.indexOf("Video Title")), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.nalsa_blue)),
                msg.indexOf("Description"), ("Description".length() + msg.indexOf("Description")), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setMessage(stringBuilder)
                .setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
