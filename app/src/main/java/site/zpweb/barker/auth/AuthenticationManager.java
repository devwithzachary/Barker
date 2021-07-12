package site.zpweb.barker.auth;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;

import site.zpweb.barker.db.CloudDBManager;
import site.zpweb.barker.model.User;
import site.zpweb.barker.utils.AuthType;
import site.zpweb.barker.utils.Toaster;

public class AuthenticationManager {

    Toaster toaster = new Toaster();
    Context context;
    int authType;
    String contactString;
    boolean isLogin;
    CloudDBManager dbManager;

    public AuthenticationManager(Context context, int authType, String contactString, boolean isLogin, CloudDBManager dbManager){
        this.context = context;
        this.authType = authType;
        this.contactString = contactString;
        this.isLogin = isLogin;
        this.dbManager = dbManager;
    }

    public void sendVerifyCode() {
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30)
                .locale(Locale.ENGLISH)
                .build();

        if (authType == AuthType.EMAIL) {
            sendEmailCode(contactString, settings);
        } else if (authType == AuthType.PHONE) {
            sendPhoneCode(contactString, settings);
        } else {
            toaster.sendErrorToast(context, "please enter either email or phone number");
        }
    }

    private void sendEmailCode(String emailString, VerifyCodeSettings settings) {
        Task<VerifyCodeResult> task = EmailAuthProvider.requestVerifyCode(emailString, settings);
        executeTask(task);
    }


    private void sendPhoneCode(String phoneString, VerifyCodeSettings settings){
        Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode("44", phoneString, settings);
        executeTask(task);
    }

    private void executeTask(Task<VerifyCodeResult> task) {
        task.addOnSuccessListener(TaskExecutors.uiThread(),
                verifyCodeResult -> authCodeDialog()).addOnFailureListener(TaskExecutors.uiThread(),
                e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
    }

    private void authCodeDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText authCodeField =  new EditText(context);
        alert.setMessage("Enter your auth code below");
        alert.setTitle("Authentication Code");

        alert.setView(authCodeField);

        alert.setPositiveButton("Login", (dialog, which) -> {
            String authCode = authCodeField.getText().toString();

            if (isLogin) {
                AGConnectAuthCredential credential = null;
                if (authType == AuthType.EMAIL) {
                    credential = EmailAuthProvider.credentialWithVerifyCode(
                            contactString,
                            null,
                            authCode);
                } else if (authType == AuthType.PHONE) {
                    credential = PhoneAuthProvider.credentialWithVerifyCode(
                            "44",
                            contactString,
                            null,
                            authCode);
                }
                signIn(credential);
            } else {
                register(authCode);
            }
        });

        alert.setNegativeButton("Cancel",
                (dialog, which) -> toaster.sendErrorToast(context, "Registration Cancelled"));

        alert.show();
    }

    private void signIn(AGConnectAuthCredential credential) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(signInResult -> {
                    getUser();
                })
                .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));

    }

    private void register(String authCode) {
        if (authType == AuthType.EMAIL) {
            EmailUser emailUser = new EmailUser.Builder()
                    .setEmail(contactString)
                    .setVerifyCode(authCode)
                    .build();

            AGConnectAuth.getInstance().createUser(emailUser).addOnSuccessListener(signInResult -> {
                User user = new User();
                user.setId(dbManager.getMaxUserID() + 1);
                user.setUid(signInResult.getUser().getUid());
                dbManager.upsertUser(user, context);

                getUser();
            }).addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
        } else if (authType == AuthType.PHONE) {
            PhoneUser phoneUser = new PhoneUser.Builder()
                    .setPhoneNumber(contactString)
                    .setCountryCode("44")
                    .setVerifyCode(authCode)
                    .build();

            AGConnectAuth.getInstance().createUser(phoneUser).addOnSuccessListener(signInResult -> {
                User user = new User();
                user.setId(dbManager.getMaxUserID() + 1);
                user.setUid(signInResult.getUser().getUid());
                dbManager.upsertUser(user, context);
                getUser();
            }).addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
        }
    }

    private void getUser(){
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        toaster.sendSuccessToast(context, user.getUid());

    }
}
