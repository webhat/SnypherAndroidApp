/*
 * Copyright (c) 2013 Daniël W. Crompton info+snypher@specialbrands.net, Snypher
 *
 *                 This program is distributed in the hope that it will be useful,
 *                 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.snypher.android;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

/**
 * @author webhat
 * @version 1.1
 */
class ImageUploadTask extends AsyncTask<Void, Void, String> {

    private Snypher snypher;

    public ImageUploadTask(Snypher snypher) {
        this.snypher = snypher;
    }

    @Override
    protected String doInBackground(Void... unsued) {
        SharedPreferences settings = snypher.getSharedPreferences(Snypher.PREFS_NAME, 0);
        String username = settings.getString("username", "");
        String password = settings.getString("password", "");

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(
                    snypher.getString(R.string.WebServiceURL)
                            + "snypher/test.php?method=uploadPhoto");

            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            snypher.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();
            entity.addPart("username", new StringBody(username));
            entity.addPart("password", new StringBody(password));
            entity.addPart("returnformat", new StringBody("json"));
            entity.addPart("uploaded", new ByteArrayBody(data,
                    "mySnyph.jpg"));
            entity.addPart("photoCaption", new StringBody(snypher.getCaption().getText()
                    .toString()));
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost,
                    localContext);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent(), "UTF-8"));

            return reader.readLine();
        } catch (Exception e) {
            if (snypher.getDialog().isShowing())
                snypher.getDialog().dismiss();
            Log.e(e.getClass().getName(), e.getMessage(), e);
            snypher.toastIt(snypher.getString(R.string.exception_message));

            return null;
        }
    }


    @Override
    protected void onProgressUpdate(Void... unsued) {
    }

    @Override
    protected void onPostExecute(String sResponse) {
        try {
            if (snypher.getDialog().isShowing())
                snypher.getDialog().dismiss();

            if (sResponse != null) {
                System.err.println(sResponse);
                JSONObject JResponse = new JSONObject(sResponse);
                int success = JResponse.getInt("SUCCESS");
                String message = JResponse.getString("MESSAGE");
                switch (success) {
                    case 1:
                        Toast.makeText(snypher.getApplicationContext(),
                                "Photo uploaded successfully",
                                Toast.LENGTH_SHORT).show();
                        snypher.getCaption().setText("");
                        break;
                    case 403:
                        snypher.resetPassword();
                        snypher.createMemory();
                        break;
                    case 0:
                    default:
                        Toast.makeText(snypher.getApplicationContext(), message,
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage(), e);
            Toast.makeText(snypher.getApplicationContext(),
                    snypher.getString(R.string.exception_message),
                    Toast.LENGTH_LONG).show();
        }
    }
}
