package com.example.testfit;

import static com.example.testfit.MainActivity.REQUEST_OAUTH_REQUEST_CODE;
import static com.example.testfit.MainActivity.tv;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.File;

public class WorkingCount {
    private Context mContext;
    private Activity mActivity;
    private MainActivity mMainActivity;

    private int dailyStepTotal =0;

    public WorkingCount() {

    }

    public void checkFitnessPermission(Context context, Activity activity) {
        mContext  = context;
        mActivity = activity;
        mMainActivity = new MainActivity();
        // 필요한 권한들 정의
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED) //칼로리
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)  //걸음수
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)    //거리
                        .build();

        //구글피트니스 권한요청
        if(!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(mContext), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    mActivity,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(mContext),
                    fitnessOptions);
        }
        //권한 기승인시
        else
        {
            subscribe(DataType.TYPE_STEP_COUNT_DELTA);
        }
    }

    public void subscribe(DataType dataType)
    {
        Fitness.getRecordingClient(mActivity, GoogleSignIn.getLastSignedInAccount(mContext))
            .subscribe(dataType)
            .addOnCompleteListener(
                new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("YYYM", "Successfully subscribed!");
                            readData(dataType);
                        } else {
                            Log.d("YYYM", "There was a problem subscribing.", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("YYYM", "onFailure: ddddd");
                    }
                });
    }

    public void readData(DataType dataType)
    {
        Fitness.getHistoryClient(mActivity, GoogleSignIn.getLastSignedInAccount(mContext))
                .readDailyTotal(dataType)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        DataSet dataStepSet = dataSet;
                        Log.d("YYYM", "dataStepSet: "+dataStepSet);
                        for (DataPoint dp : dataStepSet.getDataPoints()){
                            for (Field field : dp.getDataType().getFields()) {
                                if (!"user_input".equals(dp.getOriginalDataSource().getStreamName()))
                                {
                                    int step = dp.getValue(field).asInt();
                                    dailyStepTotal += step;
                                }
                            }
                        }
                        TextView tv2 = ((MainActivity)mContext).findViewById(R.id.working_date);
                        tv2.setText(dailyStepTotal);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "addOnFailureListener"+e, Toast.LENGTH_SHORT).show();
                    }
                });

    }




}
