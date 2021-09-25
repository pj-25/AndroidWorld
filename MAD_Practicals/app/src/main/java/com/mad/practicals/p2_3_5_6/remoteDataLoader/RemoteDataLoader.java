package com.mad.practicals.p2_3_5_6.remoteDataLoader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteDataLoader {

    public JSONArray getDataInJSON(URL url) throws IOException, JSONException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder data = new StringBuilder();
            String inData;
            while((inData = bufferedReader.readLine())!=null){
                data.append(inData);
            }
            return new JSONArray(data.toString());
        }
        return null;
    }

    public Drawable getDrawable(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return new BitmapDrawable(Resources.getSystem(), bitmap);
    }
}
