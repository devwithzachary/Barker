package site.zpweb.barker.db;

import android.content.Context;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import site.zpweb.barker.model.User;
import site.zpweb.barker.utils.Toaster;

public class CloudDBManager {

    private int maxUserID = 0;

    Toaster toaster = new Toaster();

    private final AGConnectCloudDB cloudDB;
    private CloudDBZone cloudDBZone;

    public CloudDBManager(){
        cloudDB = AGConnectCloudDB.getInstance();
    }

    public static void initCloudDB(Context context){
        AGConnectCloudDB.initialize(context);
    }

    public void openCloudDBZone(Context context){
        CloudDBZoneConfig config = new CloudDBZoneConfig("Barker",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        config.setPersistenceEnabled(true);

        try {
            cloudDBZone = cloudDB.openCloudDBZone(config, true);
        } catch (AGConnectCloudDBException e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        }
    }

    public void closeCloudDBZone(Context context){
        try {
            cloudDB.closeCloudDBZone(cloudDBZone);
        } catch (AGConnectCloudDBException e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        }
     }

    public void upsertUser(User user, Context context) {
        Task<Integer> upsertTask = cloudDBZone.executeUpsert(user);
        executeTask(upsertTask, context);
    }

    public void upsertUsers(List<User> users,Context context) {
        Task<Integer> upsertTask = cloudDBZone.executeUpsert(users);
        executeTask(upsertTask, context);
    }

    private void executeTask(Task<Integer> task,Context context) {
        task.addOnSuccessListener(integer -> toaster.sendSuccessToast(context, "upsert successful"))
                .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
    }

    public void deleteUser(User user){
        cloudDBZone.executeDelete(user);
    }

    public int getMaxUserID(){
        return maxUserID;
    }

    private void updateMaxUserID(User user){
        if (maxUserID < user.getId()) {
            maxUserID = user.getId();
        }
    }

    public void getAllUsers(Context context){
        queryUsers(CloudDBZoneQuery.where(User.class), context);
    }

    public void queryUsers(CloudDBZoneQuery<User> query, Context context) {
        Task<CloudDBZoneSnapshot<User>> task = cloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        task.addOnSuccessListener(userCloudDBZoneSnapshot -> processResults(userCloudDBZoneSnapshot, context))
                .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
    }

    private void processResults(CloudDBZoneSnapshot<User> userCloudDBZoneSnapshot, Context context) {
        CloudDBZoneObjectList<User> userCursor = userCloudDBZoneSnapshot.getSnapshotObjects();
        List<User> userList = new ArrayList<>();

        try {
            while (userCursor.hasNext()) {
                User user = userCursor.next();
                updateMaxUserID(user);
                userList.add(user);
            }
            //HAVE USER LIST
        } catch (AGConnectCloudDBException e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        } finally {
            userCloudDBZoneSnapshot.release();
        }
    }

}
