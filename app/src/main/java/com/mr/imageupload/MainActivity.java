package com.mr.imageupload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_MEDIA_AND_STORAGE = 10;

    private FloatingActionButton fab;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, REQUEST_MEDIA_AND_STORAGE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fileUri = getOutputMediaFileUri();
                    startActivityForResult(createChooser(getOutputMediaFile()), REQUEST_MEDIA_AND_STORAGE);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MEDIA_AND_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileUri = getOutputMediaFileUri();
                        startActivityForResult(createChooser(getOutputMediaFile()), REQUEST_MEDIA_AND_STORAGE);
                    }
                });
            } else {
                Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        //TODO: check to see if SD card is mounted or not with Environment.getExternalStorageState()

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ImageUpload" + File.separator + "Pictures");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("ImageUpload", "failed to create external directory");
                Log.d("ImageUpload", "creating to internal directory");
                mediaStorageDir = getFilesDir();
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Image captured and saved to fileUri specified in the Intent
            if (data != null) {
                Toast.makeText(this, "Image saved to:\n" +
                        data.getData().getEncodedPath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "[NO DATA]Image saved to:\n" +
                        fileUri.getEncodedPath(), Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Image capturing canceled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "OOPS and error occurred !", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent createChooser(File savedPhotoFile) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent imageFileIntent = null;
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            imageFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            imageFileIntent.setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*"}).addCategory(Intent.CATEGORY_OPENABLE);

        } else {
            if (savedPhotoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(savedPhotoFile));
            }
            imageFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageFileIntent.setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);
        }

        if (resolveInfo != null) {
            return Intent.createChooser(imageFileIntent, "Choose your option").putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        } else {
            return Intent.createChooser(imageFileIntent, "Choose your option");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
