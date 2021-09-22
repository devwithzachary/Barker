package site.zpweb.barker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.PhoneAuthProvider;

import java.util.List;

import site.zpweb.barker.db.CloudDBManager;
import site.zpweb.barker.model.LoginRegisterData;
import site.zpweb.barker.model.db.Post;
import site.zpweb.barker.utils.AuthType;
import site.zpweb.barker.utils.Toaster;

public class PostManager implements CloudDBManager.DBCallBack<Post>{

    Toaster toaster = new Toaster();
    Context context;
    private final CloudDBManager dbManager;
    private final PostCallBack postCallBack;

    public PostManager(Context context, PostCallBack postCallBack){
        this.context = context;
        this.postCallBack = postCallBack;

        dbManager = new CloudDBManager<Post>(context, this, Post.class);
        dbManager.createObjectType();
        dbManager.openCloudDBZoneV2();
    }

    public void makePost(){
        postDialog();
    }

    private void postDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText postField =  new EditText(context);
        alert.setTitle("Write Post");

        alert.setView(postField);

        alert.setPositiveButton("Post", (dialog, which) -> {
            String post = postField.getText().toString();
            savePost(post);
        });

        alert.setNegativeButton("Cancel", null);

        alert.show();
    }

    private void savePost(String post) {
        SharedPreferences preferences = context.getSharedPreferences("loginDetail", 0);

        Post postToSave = new Post();
        postToSave.setId(dbManager.getMaxUserID() + 1);
        postToSave.setContent(post);
        postToSave.setUser_id(preferences.getInt("userId", 0));

        dbManager.upsert(postToSave);
    }

    @Override
    public void onUpsert(Post upsertedObject) {
        postCallBack.onUpsert(upsertedObject);
    }

    @Override
    public void onQuery(List<Post> queriedObjects) {
        postCallBack.onQuery(queriedObjects);
    }

    @Override
    public void onDelete(List<Post> deletedObjects) {
        postCallBack.onDelete(deletedObjects);
    }

    @Override
    public void onError(String errorMessage) {
        postCallBack.onError(errorMessage);
    }

    public interface PostCallBack {
        void onUpsert(Post upsertedObject);
        void onQuery(List<Post> queriedObjects);
        void onDelete(List<Post> deletedObjects);
        void onError(String errorMessage);
    }
}
