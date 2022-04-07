package com.example.testfit.util;

import static com.example.testfit.MainActivity.REQUEST_CHECK_PERMISSION_RECOGNITION;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.testfit.MainActivity;
import com.example.testfit.WorkingCount;
import com.google.android.gms.fitness.data.DataType;

public class PermissionCheck extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;
    private MainActivity mMainActivity;
    private WorkingCount mWorkingCount;
    //public static final int REQUEST_CHECK_PERMISSION_RECOGNITION = 10001;
    //public static final int REQUEST_OAUTH_REQUEST_CODE = 1001;
    public void checkRecognition(Context context, Activity activity, WorkingCount workingCount){
        mContext  = context;
        mActivity = activity;
        mMainActivity = new MainActivity();
        mWorkingCount = workingCount;

        //운동권한정책
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(mContext,"ddfdf",Toast.LENGTH_SHORT).show();
            mActivity.requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_CHECK_PERMISSION_RECOGNITION);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            {

                mWorkingCount.checkFitnessPermission(mContext, mActivity);
            }
        }
        else
        {
            workingCount = new WorkingCount();
            workingCount.checkFitnessPermission(mContext, mActivity);
        }
    }



}
