package com.example.imagerecognition;

import android.os.AsyncTask;

import com.cloudmersive.client.FaceApi;
import com.cloudmersive.client.invoker.ApiException;
import com.cloudmersive.client.model.GenderDetectionResult;

import java.io.File;

class threadGenderDetection extends AsyncTask<File,Integer, GenderDetectionResult> {


    @Override
    protected GenderDetectionResult doInBackground(File... files) {

        FaceApi apiInstance = new FaceApi();
        try {
            GenderDetectionResult result = apiInstance.faceDetectGender(files[0]);
            return result;

        } catch (ApiException e) {
            System.err.println("Exception when calling RecognizeApi#recognizeDescribe");
            e.printStackTrace();
        }

        return null;
    }
}