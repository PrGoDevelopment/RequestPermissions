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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    static final int STORAGE_PERMISSION_CODE = 100;

    TextView txt_info, txt_Permissions;
    EditText etxt_nameNewFolder;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_info = findViewById(R.id.txt_info);
        txt_Permissions = findViewById(R.id.txt_Permissions);
        etxt_nameNewFolder = findViewById(R.id.etxt_nameNewFolder);

        Button btn_save = findViewById(R.id.btn_save);
        Button btn_requestAllPermissions = findViewById(R.id.btn_requestAllPermissions);

        if (CheckPermission()) {
            txt_info.setText("Permissions already granted");
        } else {
            txt_info.setText("Permissions was not granted, try create folder to receive a request");
        }

        btn_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (CheckPermission()) {
                    txt_info.setText("Permissions already granted");
                    CreateFolder();
                } else {
                    txt_info.setText("Permissions was not granted, request");
                    RequestPermission();
                }
            }
        });

        // REQUESTING ALL PERMISSIONS --------------------------------------------------------------
        if(!CheckAllAppPermissions())
            txt_Permissions.setText("Not all permissions were granted.");

        btn_requestAllPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RequestAllAppPermissions())
                    txt_Permissions.setText("Not all permissions were granted.");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void CreateFolder(){
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

    private void RequestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // ANDROID IS 11(R) OR ABOVE
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }
            catch (Exception e){
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

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        //Android is 11(R) or above
                        if (Environment.isExternalStorageManager()) {
                            // MANAGE EXTERNAL STORAGE PERMISSION IS GRANTED
                            txt_info.setText("Manage External Storage Permission is granted");
                            CreateFolder();
                        }
                        else
                            // MANAGE EXTERNAL STORAGE PERMISSION IS DENIED
                            txt_info.setText("Manage External Storage Permission is denied");
                    }
                    else {
                        // ANDROID IS BELOW 11(R)
                    }
                }
            }
    );

    public boolean CheckPermission(){
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
                // CHECK EACH PERMISSION IF GRANTED OR NOT
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (write && read){
                    // EXTERNAL STORAGE PERMISSIONS GRANTED
                    txt_info.setText("External Storage permissions granted");
                    CreateFolder();
                }
                else
                    // EXTERNAL STORAGE PERMISSION DENIED
                    txt_info.setText("External Storage permission denied");
            }
        }
    }

    // todo REQUESTING PERMISSIONS IN SEQUENCE -----------------------------------------------------

    static int PERMISSIONS_CODE = 1;

    String[] PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
    };

    @SuppressLint("SetTextI18n")
    boolean CheckAllAppPermissions(){
        List<String> requiredPermissions = new ArrayList<>();

        for(String permission : PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        txt_Permissions.setText("All permissions are activated!");

        return true;
    }

    @SuppressLint("SetTextI18n")
    boolean RequestAllAppPermissions(){
        List<String> requiredPermissions = new ArrayList<>();

        for(String permission : PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                requiredPermissions.add(permission);
            }
        }

        if(requiredPermissions.size() != 0){
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(
                    new String[requiredPermissions.size()]), PERMISSIONS_CODE);

            return false;
        }

        txt_Permissions.setText("All permissions were granted!");

        return true;
    }
}