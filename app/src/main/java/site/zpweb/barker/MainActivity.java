package site.zpweb.barker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import site.zpweb.barker.auth.AuthenticationManager;
import site.zpweb.barker.model.LoginRegisterData;
import site.zpweb.barker.utils.AuthType;

public class MainActivity extends AppCompatActivity {

    Button register, emailLogin, phoneLogin;
    EditText phone, email;

    AuthenticationManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        register = findViewById(R.id.registerBtn);
        emailLogin = findViewById(R.id.emailLogin );
        phoneLogin = findViewById(R.id.phoneLogin);

        phone = findViewById(R.id.editTextPhone2);
        email = findViewById(R.id.editTextTextEmailAddress2);

        register.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        phoneLogin.setOnClickListener(v -> {
            login(AuthType.PHONE);
        });

        emailLogin.setOnClickListener(v -> {
            login(AuthType.EMAIL);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = this.getSharedPreferences("loginDetail", 0);
        if(preferences.getBoolean("isLoggedIn", false)) {
            this.startActivity(new Intent(this, FeedActivity.class));
        }
    }

    private void login(int authType) {
        authManager = new AuthenticationManager(MainActivity.this,
                authType,
                getLoginRegisterData(),
                true);
        authManager.sendVerifyCode();
    }

    private LoginRegisterData getLoginRegisterData() {
        String emailString = email.getText().toString().trim();
        String phoneString = phone.getText().toString().trim();

        return new LoginRegisterData(phoneString, emailString, "", "");
    }
}