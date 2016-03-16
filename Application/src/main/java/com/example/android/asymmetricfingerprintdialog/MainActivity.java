/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.example.android.asymmetricfingerprintdialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.library.BluetoothState;
import com.example.bluetooth.library.Bluetoothinit;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import javax.inject.Inject;

/**
 * Main entry point for the sample, showing a backpack and "Purchase" button.
 */
@SuppressLint("NewApi")
public class MainActivity extends BaseActivity {

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final int REQUEST_FINE_LOCATION=0;
    private Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            if(msg.what== BluetoothState.MESSAGE_UI){
                String data=(String)msg.obj;
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                Log.e("date",data);

            }
        }
    };
    private    Bluetoothinit bluetoothinit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mayRequestLocation();
        Button purchaseButton = (Button) findViewById(R.id.purchase_button);
        bluetoothinit=new Bluetoothinit(this,handler);
        bluetoothinit.onstatrt();
        bluetoothinit.initBluetooth();
        bluetoothinit.setisAndroid(false);
        this.setBluetoothinit(bluetoothinit);

        if(Build.VERSION.SDK_INT>=23){
            ((InjectedApplication) getApplication()).inject(this);
            //对指纹和屏幕锁开启的状态进行判断
            fingerprintLockStage();
            //创建公钥
            createKeyPair();

        }
        purchaseButton.setEnabled(true);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>=23) {
                    findViewById(R.id.confirmation_message).setVisibility(View.GONE);
                    findViewById(R.id.encrypted_message).setVisibility(View.GONE);
                    showFingerprintAuthenticationDialogFragment();
                }else{
                    SharedPreferences Fist_SharedPreferences_lollipop=getSharedPreferences("Fist_SharedPreferences_lollipop",MODE_PRIVATE);
                    boolean isFist=Fist_SharedPreferences_lollipop.getBoolean("Fist",true);
                    if (isFist){
                        dialogFragment_lollipop.setStage(DialogFragment_lollipop.Stage.FISIST);
                        dialogFragment_lollipop.show(getFragmentManager(),"ABC");
                    }else {
                        dialogFragment_lollipop.setStage(DialogFragment_lollipop.Stage.FINGERPRINTLOLLIPOP);
                        dialogFragment_lollipop.show(getFragmentManager(),"ABC");
                    }


                }

            }
        });
    }



    private void mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                //判断是否需要 向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(this,"ble_need", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE_LOCATION);
                return;
            }else{

            }
        } else {

        }
    }
    public void onPurchased(byte[] signature) {
        showConfirmation(signature);
    }

    public void onPurchaseFailed() {
        Toast.makeText(this, R.string.purchase_fail, Toast.LENGTH_SHORT).show();
    }

    // Show confirmation, if fingerprint was used show crypto information.
    private void showConfirmation(byte[] encrypted) {
        findViewById(R.id.confirmation_message).setVisibility(View.VISIBLE);
        if (encrypted != null) {
            TextView v = (TextView) findViewById(R.id.encrypted_message);
            v.setVisibility(View.VISIBLE);
            v.setText(Base64.encodeToString(encrypted, 0 /* flags */));
            String a="123456";
            String base=Base64.encodeToString(a.getBytes(),Base64.DEFAULT);
            Log.e("encodeToString 加密",base);
            Log.e("decode 解密",new String((Base64.decode(base,Base64.DEFAULT))));
            try {
                Log.e("SHA 512 加密",encodeSHA512("123456".getBytes()));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static String encodeSHA512(byte[] data) throws Exception {
        // 初始化MessageDigest,SHA即SHA-1的简称
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        // 执行摘要方法
        byte[] digest = md.digest(data);
        return new String(digest);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetoothinit.Result(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The requested permission is granted.

                } else{
                    // The user disallowed the requested permission.
                }
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id==R.id.action_add){
            bluetoothinit.connet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
