package site.zpweb.barker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button register, emailLogin, phoneLogin;
    EditText phone, email;

    int authType = AuthType.EMAIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        register = findViewById(R.id.registerBtn);
        emailLogin = findViewById(R.id.emailLogin );
        phoneLogin = findViewById(R.id.phoneLogin);

        phone = findViewById(R.id.editTextPhone2);
        email = findViewById(R.id.editTextTextEmailAddress2);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authType = AuthType.PHONE;
                sendVerifyCode();
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authType = AuthType.EMAIL;
                sendVerifyCode();
            }
        });
    }

    private void sendVerifyCode() {
        String emailString = email.getText().toString().trim();
        String phoneString = phone.getText().toString().trim();

        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30)
                .locale(Locale.ENGLISH)
                .build();

        if (authType == AuthType.EMAIL) {
            Task<VerifyCodeResult> task = EmailAuthProvider.requestVerifyCode(emailString, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    authCodeDialog();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Error, code sending failed: " + e,
                            Toast.LENGTH_LONG).show();
                }
            });

        } else if (authType == AuthType.EMAIL) {
            Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode("44", phoneString, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    authCodeDialog();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Error, code sending failed: " + e,
                            Toast.LENGTH_LONG).show();
                }
            });

        } else {
            Toast.makeText(this,
                    "Error, please enter either email or phone number",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void authCodeDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText authCodeField =  new EditText(this);
        alert.setMessage("Enter your auth code below");
        alert.setTitle("Authentication Code");

        alert.setView(authCodeField);

        alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String authCode = authCodeField.getText().toString();

                AGConnectAuthCredential credential = null;
                if (authType == AuthType.EMAIL) {
                    credential = EmailAuthProvider.credentialWithVerifyCode(
                            email.getText().toString().trim(),
                            null,
                            authCode);
                } else if (authType == AuthType.PHONE) {
                    credential = PhoneAuthProvider.credentialWithVerifyCode(
                            "44",
                            phone.getText().toString().trim(),
                            null,
                            authCode);
                }
                signIn(credential);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,
                        "Registration Cancelled",
                        Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

    private void signIn(AGConnectAuthCredential credential) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        Toast.makeText(MainActivity.this, "Sign in successful: " +
                                signInResult.getUser().getUid(), Toast.LENGTH_LONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "sign in failed:" + e, Toast.LENGTH_LONG);
                    }
                });

    }
}
