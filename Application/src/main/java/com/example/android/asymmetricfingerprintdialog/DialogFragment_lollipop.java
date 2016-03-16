package com.example.android.asymmetricfingerprintdialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.bluetooth.library.Bluetoothinit;

import javax.inject.Inject;

/**
 * Created by wenxi on 16/3/9.
 */
public class DialogFragment_lollipop extends DialogFragment {

    private Stage mStage = Stage.FISIST;
    private Button password;
    private EditText editText_password;
    private SharedPreferences Fist_SharedPreferences_lollipop;
    private Bluetoothinit bluetoothinit;
    private BaseActivity baseActivity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(getString(R.string.sign_in));
        View v =inflater.inflate(R.layout.fringerprint_dialog_lollipop,container,false);
        password=(Button) v.findViewById(R.id.second_dialog_button_lollipop);
        editText_password=(EditText)v.findViewById(R.id.password_lollipop);
        Fist_SharedPreferences_lollipop=getActivity().getSharedPreferences("Fist_SharedPreferences_lollipop",getActivity().MODE_PRIVATE);
        updateStage();
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStage==Stage.FISIST){
                    SharedPreferences.Editor editor=Fist_SharedPreferences_lollipop.edit();
                    editor.putBoolean("Fist",false);
                    editor.putString("User_password",editText_password.getText().toString());
                    editor.apply();
                    mStage=Stage.FINGERPRINTLOLLIPOP;
                    updateStage();
                    editText_password.setText("");
                }else {
                        String password=editText_password.getText().toString();
                        if (TextUtils.equals(password,Fist_SharedPreferences_lollipop.getString("User_password",""))) {
                            editText_password.setText("");
                            editText_password.setHint("密码正确");
                            DialogFragment_lollipop.this.bluetoothinit=DialogFragment_lollipop.this.baseActivity.getBluetoothinit();
                            DialogFragment_lollipop.this.bluetoothinit.send("hello");
                        }else{
                           editText_password.setText("");
                           editText_password.setHint("密码错误");
                        }
                }
            }
        });
        return v;
    }
    private void updateStage(){
        switch (mStage){
            case FISIST:{
                editText_password.setHint("请输入第一次密码");
                break;
            }
            case FINGERPRINTLOLLIPOP:{
                editText_password.setHint("请输入密码");
                break;
            }

        }

    }
    public void setStage(Stage stage) {
        mStage = stage;
    }
    public enum Stage {
        FISIST,
        FINGERPRINTLOLLIPOP,

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }
}
