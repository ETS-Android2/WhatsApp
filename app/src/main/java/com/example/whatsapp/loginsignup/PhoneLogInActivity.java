
package com.example.whatsapp.loginsignup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.whatsapp.R;

public class PhoneLogInActivity extends AppCompatActivity {
    Button Verify,SendVerificationCode;
    EditText InputPhoneNumber,InputVerification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_log_in);
        Initialise();
        Verify.setVisibility(View.GONE);
        InputVerification.setVisibility(View.GONE);
        SendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCode.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.GONE);
                Verify.setVisibility(View.VISIBLE);
                InputVerification.setVisibility(View.VISIBLE);
            }
        });
    }

    private void Initialise() {
        Verify=findViewById(R.id.verify_btn);
        SendVerificationCode=findViewById(R.id.send_ver_code);
        InputPhoneNumber=findViewById(R.id.phone_number_input);
        InputVerification=findViewById(R.id.verification_code_input);

    }
}