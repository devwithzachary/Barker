package site.zpweb.barker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    EditText email, phone;
    Button register;

    int authType = AuthType.EMAIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.editTextTextEmailAddress);
        phone = findViewById(R.id.editTextPhone);

        register = findViewById(R.id.registerBtn2);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        if (!emailString.isEmpty()) {
            authType = AuthType.EMAIL;

            Task<VerifyCodeResult> task = EmailAuthProvider.requestVerifyCode(emailString, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    authCodeDialog();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(RegisterActivity.this,
                            "Error, code sending failed: " + e,
                            Toast.LENGTH_LONG).show();
                }
            });

        } else if (!phoneString.isEmpty()) {
            authType = AuthType.PHONE;

            Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode("44", phoneString, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    authCodeDialog();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(RegisterActivity.this,
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

        alert.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String authCode = authCodeField.getText().toString();
                register(authCode);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(RegisterActivity.this,
                        "Registration Cancelled",
                        Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

    private void register(String authCode) {
        String emailString = email.getText().toString().trim();
        String phoneString = phone.getText().toString().trim();

        if (authType == AuthType.EMAIL) {
            EmailUser emailUser = new EmailUser.Builder()
                    .setEmail(emailString)
                    .setVerifyCode(authCode)
                    .build();

            AGConnectAuth.getInstance().createUser(emailUser).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                @Override
                public void onSuccess(SignInResult signInResult) {
                    Toast.makeText(RegisterActivity.this,
                            "Register Successful: " + signInResult.getUser().getUid(),
                            Toast.LENGTH_LONG);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(RegisterActivity.this,
                            "Registering failed " + e,
                            Toast.LENGTH_LONG);
                }
            });
        } else if (authType == AuthType.PHONE) {
            PhoneUser phoneUser = new PhoneUser.Builder()
                    .setPhoneNumber(phoneString)
                    .setCountryCode("44")
                    .setVerifyCode(authCode)
                    .build();

            AGConnectAuth.getInstance().createUser(phoneUser).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                @Override
                public void onSuccess(SignInResult signInResult) {
                    Toast.makeText(RegisterActivity.this,
                            "Register Successful: " + signInResult.getUser().getUid(),
                            Toast.LENGTH_LONG);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(RegisterActivity.this,
                            "Registering failed " + e,
                            Toast.LENGTH_LONG);
                }
            });
        }
    }
}