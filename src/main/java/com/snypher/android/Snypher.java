/*
 * Copyright (c) 2013 Daniël W. Crompton info+snypher@specialbrands.net, Snypher
 *
 *                 This program is distributed in the hope that it will be useful,
 *                 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.snypher.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


/**
 * @author webhat
 * @version 1.1
 */
public class Snypher extends Activity {
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_IMAGE = 2;
    private ImageView imgView;
    private Button upload;

    public EditText getCaption() {
        return caption;
    }

    public void setCaption(EditText caption) {
        this.caption = caption;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public void setDialog(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    private EditText caption;
    private Bitmap bitmap;
    private ProgressDialog dialog;
    public static final String PREFS_NAME = "SnypherPrefs";

    private SurfaceHolder previewHolder = null;
    private Camera camera;

    private boolean cameraConfigured = true;
    private UIHandler uiHandler = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // resetPassword();
        createMemory();


    }

    protected void createMemory() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String username = settings.getString("username", "");
        String password = settings.getString("password", "");

        System.out.println("Username[" + username.length() + "]: " + username + "\nPassword[" + password.length() + "]: " + password);

        if (username.length() == 0) {// || password.length() == 0) {
            System.out.println("XXX");
            setContentView(R.layout.login);
            Button login = (Button) findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    if (((EditText) findViewById(R.id.username)).getText().toString().length() > 0
                            && ((EditText) findViewById(R.id.password)).getText().toString().length() > 0) {
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("username", ((EditText) findViewById(R.id.username)).getText().toString());
                        editor.putString("password", ((EditText) findViewById(R.id.password)).getText().toString());
                        editor.commit();

                        imageUpload();
                    } else
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.login_exception),
                                Toast.LENGTH_LONG).show();
                }
            });
        } else {
            System.out.println("YYY");
            imageUpload();
        }
    }

    protected void resetPassword() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.commit();
    }

    private void imageUpload() {
        setContentView(R.layout.imageupload);
        imgView = (ImageView) findViewById(R.id.ImageView);
        upload = (Button) findViewById(R.id.Upload);
        setCaption((EditText) findViewById(R.id.Caption));
        upload.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (getBitmap() == null) {
                    Toast.makeText(getApplicationContext(),
                            "Please select image", Toast.LENGTH_SHORT).show();
                } else {
                    setDialog(ProgressDialog.show(Snypher.this, "Uploading",
                            "Please wait...", true));
                    new ImageUploadTask(Snypher.this).execute();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.imageupload_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.ic_menu_gallery:
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, "Select Picture"),
                            PICK_IMAGE);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.exception_message),
                            Toast.LENGTH_LONG).show();
                    Log.e(e.getClass().getName(), e.getMessage(), e);
                }
                return true;
            case R.id.camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(Environment.getExternalStorageDirectory(), "test.jpg");

                Uri outputFileUri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(intent, TAKE_IMAGE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri outputFileUri = null;

        switch (requestCode) {
            case TAKE_IMAGE:
                System.out.println("Take Picture");
                File file = new File(Environment.getExternalStorageDirectory(), "test.jpg");

                outputFileUri = Uri.fromFile(file);

            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null)
                        outputFileUri = data.getData();
                    String filePath = null;

                    try {
                        // OI FILE Manager
                        String filemanagerstring = null;
                        if (outputFileUri != null) {
                            filemanagerstring = outputFileUri.getPath();
                        }

                        // MEDIA GALLERY
                        String selectedImagePath = getPath(outputFileUri);

                        if (selectedImagePath != null) {
                            filePath = selectedImagePath;
                        } else if (filemanagerstring != null) {
                            filePath = filemanagerstring;
                        } else {
                            Toast.makeText(getApplicationContext(), "Unknown path",
                                    Toast.LENGTH_LONG).show();
                            Log.e("Bitmap", "Unknown path");
                        }

                        if (filePath != null) {
                            decodeFile(filePath);
                        } else {
                            setBitmap(null);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Internal error",
                                Toast.LENGTH_LONG).show();
                        Log.e(e.getClass().getName(), e.getMessage(), e);
                    }
                }
                break;
            default:
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE &&
                    height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        setBitmap(BitmapFactory.decodeFile(filePath, o2));

        imgView.setImageBitmap(getBitmap());

    }


    private void initPreview() {
        if (camera != null && previewHolder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e("surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(Snypher.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startPreview() {
        if (cameraConfigured && camera != null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
            System.out.println("surfaceCreated");
        }

        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int width, int height) {
            System.out.println("surfaceChanged");
            initPreview();
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
            System.out.println("surfaceDestroyed");
        }
    };


    public void toastIt(String message) {
        if (uiHandler == null) {
            HandlerThread uiThread = new HandlerThread("UIHandler");
            uiThread.start();
            uiHandler = new UIHandler(this, uiThread.getLooper());
        }
        Message msg = uiHandler.obtainMessage(UIHandler.DISPLAY_UI_TOAST);
        msg.obj = message;
        uiHandler.sendMessage(msg);
    }
}
