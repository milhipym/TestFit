package com.example.testfit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "YYYM";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1001;
    public FitnessOptions fitnessOptions ;
    String timeTotalStep="0";
    long totalTime = 0;
    int totalStep = 0;
    int totalCal = 0;
    float totalDis = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //운동권한정책
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},0);
        }

        // 필요한 권한들 정의
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .build();

        if(!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            Fitness.getRecordingClient(this,
                    GoogleSignIn.getLastSignedInAccount(this))
                    .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "Successfully subscribed!");
                                        readData();
                                    } else {
                                        Log.w(TAG, "There was a problem subscribing.", task.getException());
                                    }
                                }
                            });
        }

    }//end onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 로그인 성공시
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
                readData();
            }
        }
    }

    private void readData() {

        final Calendar cal = Calendar.getInstance();
        Date now = Calendar.getInstance().getTime();
        cal.setTime(now);

        // 시작 시간
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long startTime = cal.getTimeInMillis();

        // 종료 시간
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 24, 0, 0);
        long endTime = cal.getTimeInMillis();

        Fitness.getHistoryClient(this,
                GoogleSignIn.getLastSignedInAccount(this))
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA) // Raw 걸음 수
                        .read(DataType.TYPE_CALORIES_EXPENDED)// 칼로리
                        .read(DataType.TYPE_DISTANCE_DELTA)   // 거리
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse response) {
                        DataSet dataStepSet = response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        DataSet dataCaloSet = response.getDataSet(DataType.TYPE_CALORIES_EXPENDED);
                        DataSet dataDistSet = response.getDataSet(DataType.TYPE_DISTANCE_DELTA);

                        Log.i(TAG, "Data returned for Data type: " + dataStepSet.getDataType().getName());
                        //스텝 카운트
                        for (DataPoint dp : dataStepSet.getDataPoints()) {
                            Log.i(TAG, "Data stepPoint------------------");
                            Log.i(TAG, "\tType: " + dp.getDataType().getName());
                            long timeLong = dp.getTimestamp(TimeUnit.MILLISECONDS);
                            long timelongHours = (timeLong/1000) / 60 / 60%24;
                            long timelongMinuts = (timeLong/1000) / 60 % 60;
                            long timelongSeconds = (timeLong/1000) % 60;
                            String time = String.format("%02d:%02d:%02d", timelongHours, timelongMinuts, timelongSeconds);
                            Log.d("YYYM", "\ttime: "+time);
                            //Log.i(TAG, "\tStart: " + dp.getStartTime(TimeUnit.MILLISECONDS) + ","+ dp.getTimestamp(TimeUnit.MILLISECONDS));
                            //Log.i(TAG, "\tEnd: " + dp.getEndTime(TimeUnit.MILLISECONDS));

                            totalTime += dp.getEndTime(TimeUnit.MILLISECONDS) - dp.getStartTime(TimeUnit.MILLISECONDS);
                            long tiemMilli = dp.getEndTime(TimeUnit.MILLISECONDS) - dp.getStartTime(TimeUnit.MILLISECONDS);
                            long timeStartStrHours = (tiemMilli/1000) / 60 / 60%24;
                            long timeStartStrMinuts = (tiemMilli/1000) / 60 % 60;
                            long timeStartStrSeconds = (tiemMilli/1000) % 60;
                            String timePerStep = String.format("%02d:%02d:%02d", timeStartStrHours, timeStartStrMinuts, timeStartStrSeconds);
                            long timeTotalHours = (totalTime/1000) / 60 / 60%24;
                            long timeTotalMinuts = (totalTime/1000) / 60 % 60;
                            long timeTotalSeconds = (totalTime/1000) % 60;
                            timeTotalStep = String.format("%02d:%02d:%02d", timeTotalHours, timeTotalMinuts, timeTotalSeconds);

                            Log.d("YYYM", "\ttimePerStep: "+timePerStep + ", timeTotalStep:"+timeTotalStep);

                            for (Field field : dp.getDataType().getFields()) {
                                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                totalStep += dp.getValue(field).asInt();
                            }
                        }
                        Log.d("YYYM", "timeTotalStep: " + timeTotalStep + ", totalStep:"+totalStep);

                        //칼로리
                        Log.d("YYYM", "Data CalPoint------------------");
                        for (DataPoint dpCal : dataCaloSet.getDataPoints()) {
                            for (Field field : dpCal.getDataType().getFields())
                            {
                                totalCal += dpCal.getValue(field).asFloat();
                                Log.d("YYYM", "totalCal: "+totalCal+" , eachStep:"+dpCal.getValue(field).asFloat());
                            }
                        }
                        int totalCalories = (int)Math.floor(totalCal);
                        Log.d("YYYM", "totalCalories: "+totalCalories);


                        //총 거리
                        Log.d("YYYM", "Data CalPoint------------------ ");
                        for (DataPoint dpDis : dataDistSet.getDataPoints()){
                            for (Field field : dpDis.getDataType().getFields()){
                                totalDis += dpDis.getValue(field).asFloat();
                                Log.d("YYYM", "totalDis: "+totalDis);
                            }
                        }
                        int totalDistance = (int) Math.floor(totalDis);
                        Log.d("YYYM", "totalDistance: "+totalDistance);
                        Log.d("YYYM", "timeTotalStep: "+timeTotalStep);
                        Log.d("YYYM", "totalStep: "+totalStep);
                        Log.d("YYYM", "totalCalories: "+totalCalories);
                        Log.d("YYYM", "totalDistance: "+totalDistance);
                    }
                });

    }


    public void subscribe()
    {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
        .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Log.d("YYYM", "onComplete: " + task.isSuccessful());
                        }
                        else{
                            Log.d("YYYM", "onFail: " + task.isSuccessful());
                        }
                    }
                }

        );
    }



}