package site.zpweb.barker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import site.zpweb.barker.auth.AuthenticationManager;
import site.zpweb.barker.db.CloudDBManager;
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
            authManager = new AuthenticationManager(MainActivity.this,
                    AuthType.PHONE,
                    phone.getText().toString().trim(),
                    true);
            authManager.sendVerifyCode();
        });

        emailLogin.setOnClickListener(v -> {
            authManager = new AuthenticationManager(MainActivity.this,
                    AuthType.EMAIL,
                    email.getText().toString().trim(),
                    true);
            authManager.sendVerifyCode();
        });
    }
}