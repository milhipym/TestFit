package com.example.testfit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "YYYM";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1001;
    public FitnessOptions fitnessOptions ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
        }*/
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
            Log.d("YYYM", "onCreate: ");
        } else {
            Log.d("YYYM", "onCreate: else");
            accessGoogleFit();
        }


        // Read the data that's been collected throughout the past week.
/*        ZonedDateTime endTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        ZonedDateTime startTime = endTime.minusWeeks(1);
        Log.i(TAG, "Range Start: $startTime");
        Log.i(TAG, "Range End: $endTime");

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return,
                // effectively combining multiple data queries into one call.
                // This example demonstrates aggregating only one data type.
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be
                // aggregated.
                // bucketByTime allows for a time span, while bucketBySession allows
                // bucketing by sessions.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build();

        DataReadRequest readRequest2 = new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByActivityType(1, TimeUnit.SECONDS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build();*/



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                //subscribe();
                Log.d("YYYM", "onActivityResult: ");
                accessGoogleFit();
            }
        }
    }

    private void accessGoogleFit() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        Log.d("YYYM", "accessGoogleFit: "+startSeconds+" , "+endSeconds);
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>(){
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                Log.d("YYYM", "onSuccess: "+dataReadResponse.toString());
                                if (dataReadResponse.getBuckets().size() > 0)
                                {
                                    for (Bucket bucket : dataReadResponse.getBuckets())
                                    {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets){
                                            for (DataPoint datapoint : dataSet.getDataPoints())
                                            {
                                                Log.d("YYYM", "onSuccess: " + datapoint.getDataType().getName());
                                                for (Field field : datapoint.getDataType().getFields())
                                                {
                                                    Log.d("YYYM", "onSuccess: "+field.getName());
                                                    Log.d("YYYM", "onSuccess: "+datapoint.getValue(field));
                                                    Log.d("YYYM", "onSuccess: "+datapoint.getOriginalDataSource().getDataType()
                                                    + ", "+datapoint.getOriginalDataSource().getStreamName()
                                                    + ", "+datapoint.getOriginalDataSource().getDevice());

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("YYYM", "onFailure: "+e);
                            }
                        }
                );
    }

}