package com.example.imagerecognition;

import android.os.AsyncTask;

import com.cloudmersive.client.RecognizeApi;
import com.cloudmersive.client.invoker.ApiException;
import com.cloudmersive.client.model.ImageDescriptionResponse;

import java.io.File;

class threadDescribeImage extends AsyncTask<File,Integer, ImageDescriptionResponse> {


    @Override
    protected ImageDescriptionResponse doInBackground(File... files) {

        RecognizeApi apiInstance = new RecognizeApi();
        try {
            ImageDescriptionResponse result = apiInstance.recognizeDescribe(files[0]);

            return result;

        } catch (ApiException e) {
            System.err.println("Exception when calling RecognizeApi#recognizeDescribe");
            e.printStackTrace();
        }

        return null;
    }
}
