package com.example.imagerecognition;

import android.os.AsyncTask;

import com.cloudmersive.client.RecognizeApi;
import com.cloudmersive.client.invoker.ApiException;
import com.cloudmersive.client.model.ObjectDetectionResult;

import java.io.File;

class threadDetectObjects extends AsyncTask<File,Integer, ObjectDetectionResult> {


    @Override
    protected ObjectDetectionResult doInBackground(File... files) {

        RecognizeApi apiInstance = new RecognizeApi();

        try {
            ObjectDetectionResult result = apiInstance.recognizeDetectObjects(files[0]);
            return result;
        } catch (ApiException e) {
            System.err.println("Exception when calling RecognizeApi#recognizeDetectObjects");
            e.printStackTrace();
        }

        return null;
    }
}
