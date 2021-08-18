package site.zpweb.barker.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.List;
import java.util.Locale;

import site.zpweb.barker.FeedActivity;
import site.zpweb.barker.db.CloudDBManager;
import site.zpweb.barker.model.LoginRegisterData;
import site.zpweb.barker.model.db.User;
import site.zpweb.barker.utils.AuthType;
import site.zpweb.barker.utils.Toaster;

public class AuthenticationManager implements CloudDBManager.DBCallBack<User> {

    Toaster toaster = new Toaster();
    Context context;
    int authType;
    LoginRegisterData loginRegisterData;
    boolean isLogin;
    private final CloudDBManager dbManager;
    private String loginUserUID = "0";

    public AuthenticationManager(Context context, int authType, LoginRegisterData loginRegisterData, boolean isLogin){
        this.context = context;
        this.authType = authType;
        this.loginRegisterData = loginRegisterData;
        this.isLogin = isLogin;

        dbManager = new CloudDBManager<User>(context, this, new User());
        dbManager.createObjectType();
        dbManager.openCloudDBZoneV2();
    }

    public void sendVerifyCode() {
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30)
                .locale(Locale.ENGLISH)
                .build();

        if (authType == AuthType.EMAIL) {
            sendEmailCode(loginRegisterData.getEmail(), settings);
        } else if (authType == AuthType.PHONE) {
            sendPhoneCode(loginRegisterData.getPhoneNumber(), settings);
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
                            loginRegisterData.getEmail(),
                            null,
                            authCode);
                } else if (authType == AuthType.PHONE) {
                    credential = PhoneAuthProvider.credentialWithVerifyCode(
                            "44",
                            loginRegisterData.getPhoneNumber(),
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
                .addOnSuccessListener(signInResult -> getUser())
                .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));

    }

    private void register(String authCode) {
        if (authType == AuthType.EMAIL) {
            EmailUser emailUser = new EmailUser.Builder()
                    .setEmail(loginRegisterData.getEmail())
                    .setVerifyCode(authCode)
                    .build();

            AGConnectAuth.getInstance().createUser(emailUser).addOnSuccessListener(this::saveRegisteredUser)
                    .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
        } else if (authType == AuthType.PHONE) {
            PhoneUser phoneUser = new PhoneUser.Builder()
                    .setPhoneNumber(loginRegisterData.getPhoneNumber())
                    .setCountryCode("44")
                    .setVerifyCode(authCode)
                    .build();

            AGConnectAuth.getInstance().createUser(phoneUser).addOnSuccessListener(this::saveRegisteredUser)
                    .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
        }
    }

    private void saveRegisteredUser(SignInResult signInResult){
        User user = new User();
        user.setId(dbManager.getMaxUserID() + 1);
        user.setUid(signInResult.getUser().getUid());
        user.setUsername(loginRegisterData.getUsername());
        user.setDisplayname(loginRegisterData.getDisplayName());
        dbManager.upsert(user);
    }

    private void getUser(){
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        loginUserUID = user.getUid();
        CloudDBZoneQuery<User> snapshotQuery = CloudDBZoneQuery.where(User.class).equalTo("uid", loginUserUID);
        dbManager.query(snapshotQuery);
    }

    private void saveLoginDetail(User user) {
        SharedPreferences preferences = context.getSharedPreferences("loginDetail", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", user.getId());
        editor.apply();
    }

    private void proceedToFeed(){
        context.startActivity(new Intent(context, FeedActivity.class));
    }

    @Override
    public void onUpsert(User user){
        saveLoginDetail(user);
        proceedToFeed();
    }

    @Override
    public void onQuery(List<User> userList) {
        if (userList.size() == 1) {
            User user = userList.get(0);
            if (user.getUid().equals(loginUserUID)){
                saveLoginDetail(user);
                proceedToFeed();
            }
        }
    }

    @Override
    public void onDelete(List<User> userList) {

    }

    @Override
    public void onError(String errorMessage) {
        toaster.sendErrorToast(context, errorMessage);
    }
}
