package com.example.testfit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testfit.util.PermissionCheck;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CHECK_PERMISSION_RECOGNITION = 10001;
    public static final int REQUEST_OAUTH_REQUEST_CODE = 1001;
    private static final String TAG = "YYYM";
    public FitnessOptions fitnessOptions ;
    String timeTotalStep="0";
    long totalTime = 0;
    int totalStep = 0;
    int totalCal = 0;
    float totalDis = 0;

    public WorkingCount workingCount;
    public static TextView tv;
    public static TextView tv_totalCnt;
    public static ProgressBar pr_totalCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSetting();
        //TestUi testUi = new TestUi(tv);
        //TestUi testUi = new TestUi();
        workingCount = new WorkingCount(getApplicationContext(), MainActivity.this);
        PermissionCheck permissionCheckc = new PermissionCheck();
        permissionCheckc.checkRecognition(getApplicationContext(), MainActivity.this, workingCount);


    }//end onCreate

    private void initSetting() {
        tv = findViewById(R.id.working_date);
        tv_totalCnt = findViewById(R.id.stepCnt);
        pr_totalCnt = findViewById(R.id.stepCircle);

        tv_totalCnt.setText("dfdfdffdfdf");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 로그인 성공시
        Log.d("YYYM", "onActivityResult: "+resultCode+", data:"+data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_PERMISSION_RECOGNITION){
            Log.d("YYYM", "onActivityResult 운동권한 : "+resultCode+", data:"+data);
            workingCount.checkFitnessPermission(getApplicationContext(), MainActivity.this);
        }
        else if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("YYYM", "onActivityResult:  구글핏권한: " + resultCode + ", :" + data);
                workingCount.subscribe(DataType.TYPE_STEP_COUNT_DELTA, getApplicationContext());
                //workingCount.subscribe(DataType.TYPE_DISTANCE_DELTA, getApplicationContext());
                //workingCount.subscribe(DataType.TYPE_CALORIES_EXPENDED, getApplicationContext());
                //readData();
            }else if (resultCode == Activity.RESULT_CANCELED)
            {
                workingCount.subscribe(DataType.TYPE_STEP_COUNT_DELTA, getApplicationContext());
                //workingCount.subscribe(DataType.TYPE_DISTANCE_DELTA, getApplicationContext());
                //workingCount.subscribe(DataType.TYPE_CALORIES_EXPENDED, getApplicationContext());
                Log.d("YYYM", "onActivityResult:  구글핏권한 취소: " + resultCode + ", :" + data);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("YYYM", "onRequestPermissionsResult: "+requestCode+" , grantResults:"+grantResults);
        switch (requestCode)
        {
            case REQUEST_CHECK_PERMISSION_RECOGNITION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    workingCount.checkFitnessPermission(getApplicationContext(), MainActivity.this);
                }
                else
                {
                }
            break;

        }
    }

    public void updateProgressBarStep(Integer stepCnt){
        int goalStep = 1000;
        int userStep = stepCnt;
        int percentValue = 0;

        percentValue = (int)( (double)userStep/ (double)goalStep * 100.0 );
        Log.d("YYYM", "updateProgressBarStep: "+percentValue + " , userStep:"+userStep);

        if(tv_totalCnt == null){
            //Toast.makeText(getApplicationContext(), "dd:", Toast.LENGTH_SHORT).show();
            tv_totalCnt = findViewById(R.id.stepCnt);
            tv_totalCnt.setText(userStep);
            pr_totalCnt.setProgress(percentValue);
        }
        else {
            tv_totalCnt.setText(Integer.toString(userStep));
            pr_totalCnt.setProgress(percentValue);
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
                Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
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
}