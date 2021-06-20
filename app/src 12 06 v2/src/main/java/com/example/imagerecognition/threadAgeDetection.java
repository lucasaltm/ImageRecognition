package com.example.imagerecognition;

import android.os.AsyncTask;

import com.cloudmersive.client.FaceApi;
import com.cloudmersive.client.invoker.ApiException;
import com.cloudmersive.client.model.AgeDetectionResult;

import java.io.File;

class threadAgeDetection extends AsyncTask<File,Integer, AgeDetectionResult> {


    @Override
    protected AgeDetectionResult doInBackground(File... files) {

        FaceApi apiInstance = new FaceApi();
        try {
            AgeDetectionResult  result = apiInstance.faceDetectAge(files[0]);
            return result;

        } catch (ApiException e) {
            System.err.println("Exception when calling RecognizeApi#recognizeDescribe");
            e.printStackTrace();
        }

        return null;
    }
}
