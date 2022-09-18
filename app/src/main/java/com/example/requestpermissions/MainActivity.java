package com.example.requestpermissions;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    static final int STORAGE_PERMISSION_CODE = 100;
    static final String TAG = "PERMISSION_TAG";

    TextView txt_info;
    EditText etxt_nameNewFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_info = findViewById(R.id.txt_info);
        etxt_nameNewFolder = findViewById(R.id.etxt_nameNewFolder);
        Button btn_save = findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    txt_info.setText("Permissions already granted");
                    createFolder();
                } else {
                    txt_info.setText("Permissions was not granted, request");
                    requestPermission();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void createFolder(){
        //get folder name
        String folderName = etxt_nameNewFolder.getText().toString().trim();

        //create folder using name we just input
        File file = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
        // CREATE FOLDER
        boolean folderCreated = file.mkdir();

        // SHOW IF FOLDER CREATED OR NOT
        if (folderCreated)
            txt_info.setText("Folder Created on:\n" + file.getAbsolutePath());
        else
            txt_info.setText("Folder not created!\nCheck if folder " + folderName + " already exist and choose other name!");
    }

    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // ANDROID IS 11(R) OR ABOVE
            try {
                Log.d(TAG, "requestPermission: try");

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }
            catch (Exception e){
                Log.e(TAG, "requestPermission: catch", e);
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }
        else {
            // ANDROID IS BELOW 11(R)
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    //here we will handle the result of our intent
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        //Android is 11(R) or above
                        if (Environment.isExternalStorageManager()) {
                            // MANAGE EXTERNAL STORAGE PERMISSION IS GRANTED
                            txt_info.setText("Manage External Storage Permission is granted");
                            createFolder();
                        }
                        else
                            // MANAGE EXTERNAL STORAGE PERMISSION IS DENIED
                            txt_info.setText("Manage External Storage Permission is denied");
                    }
                    else {
                        //Android is below 11(R)
                    }
                }
            }
    );

    public boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // ANDROID IS 11(R) OR ABOVE
            return Environment.isExternalStorageManager();
        }
        else{
            // ANDROID IS BELOW 11(R)
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    /* HANDLE PERMISSION REQUEST RESULTS */
    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0){
                //check each permission if granted or not
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (write && read){
                    // EXTERNAL STORAGE PERMISSIONS GRANTED
                    txt_info.setText("External Storage permissions granted");
                    createFolder();
                }
                else
                    // EXTERNAL STORAGE PERMISSION DENIED
                    txt_info.setText("External Storage permission denied");
            }
        }
    }
}