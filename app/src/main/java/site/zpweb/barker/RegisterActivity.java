package site.zpweb.barker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import site.zpweb.barker.auth.AuthenticationManager;
import site.zpweb.barker.model.LoginRegisterData;
import site.zpweb.barker.utils.AuthType;
import site.zpweb.barker.utils.Toaster;

public class RegisterActivity extends AppCompatActivity {

    EditText email, phone, username, displayName;
    Button register;

    AuthenticationManager authManager;
    Toaster toaster = new Toaster();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.editTextTextEmailAddress);
        phone = findViewById(R.id.editTextPhone);
        username = findViewById(R.id.editTextTextUsername);
        displayName = findViewById(R.id.editTextTextDisplayName);

        register = findViewById(R.id.registerBtn2);

        register.setOnClickListener(v -> {
            String emailString = email.getText().toString().trim();
            String phoneString = phone.getText().toString().trim();
            String usernameString = username.getText().toString().trim();
            String displayNameString = displayName.getText().toString().trim();

            LoginRegisterData registerData = new LoginRegisterData(phoneString, emailString, usernameString, displayNameString);

            if (!emailString.isEmpty()) {
                authManager = new AuthenticationManager(RegisterActivity.this,
                        AuthType.EMAIL,
                        registerData,
                        false);
                authManager.sendVerifyCode();
            } else if (!phoneString.isEmpty()) {
                authManager = new AuthenticationManager(RegisterActivity.this,
                        AuthType.PHONE,
                        registerData,
                        false);
                authManager.sendVerifyCode();
            } else {
                toaster.sendErrorToast(RegisterActivity.this, "please enter either email or phone number");
            }
        });


    }
}