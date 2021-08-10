package site.zpweb.barker.db;

import android.content.Context;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import site.zpweb.barker.model.db.ObjectTypeInfoHelper;
import site.zpweb.barker.model.db.User;
import site.zpweb.barker.utils.Toaster;

public class CloudDBManager {

    private int maxUserID = 0;

    Toaster toaster = new Toaster();

    private final AGConnectCloudDB cloudDB;
    private CloudDBZone cloudDBZone;

    private final Context context;
    private final UserCallBack callBack;

    private final OnSnapshotListener<User> snapshotListener = (cloudDBZoneSnapshot, e) -> processResults(cloudDBZoneSnapshot);

    public CloudDBManager(Context context, UserCallBack callBack){
        cloudDB = AGConnectCloudDB.getInstance();
        this.context = context;
        this.callBack = callBack;
    }

    public static void initCloudDB(Context context){
        AGConnectCloudDB.initialize(context);
    }

    public void openCloudDBZoneV2(){
        CloudDBZoneConfig config = new CloudDBZoneConfig("Barker",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        config.setPersistenceEnabled(true);

        Task<CloudDBZone> task = cloudDB.openCloudDBZone2(config, true);
        task.addOnSuccessListener(zone -> {
            cloudDBZone = zone;
            addSubscription();
        }).addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
    }

    public void closeCloudDBZone(){
        try {
            cloudDB.closeCloudDBZone(cloudDBZone);
        } catch (AGConnectCloudDBException e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        }
     }

     public void createObjectType() {
        try {
            cloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (Exception e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        }
     }

     public void addSubscription(){
        CloudDBZoneQuery<User> snapshotQuery = CloudDBZoneQuery.where(User.class).equalTo("uid", "");
         try {
             ListenerHandler handler = cloudDBZone.subscribeSnapshot(snapshotQuery,
                     CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                     snapshotListener);
         } catch (Exception e) {
             toaster.sendErrorToast(context, e.getLocalizedMessage());
         }
     }

    public void upsertUser(User user) {
        Task<Integer> upsertTask = cloudDBZone.executeUpsert(user);
        upsertTask.addOnSuccessListener(integer -> {
            updateMaxUserID(user);
            callBack.onUpsert(user);
        }).addOnFailureListener(e -> callBack.onError(e.getLocalizedMessage()));
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

    public void getAllUsers(){
        queryUsers(CloudDBZoneQuery.where(User.class));
    }

    public void queryUsers(CloudDBZoneQuery<User> query) {
        Task<CloudDBZoneSnapshot<User>> task = cloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        task.addOnSuccessListener(this::processResults)
                .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
    }

    private void processResults(CloudDBZoneSnapshot<User> userCloudDBZoneSnapshot) {
        CloudDBZoneObjectList<User> userCursor = userCloudDBZoneSnapshot.getSnapshotObjects();
        List<User> userList = new ArrayList<>();

        try {
            while (userCursor.hasNext()) {
                User user = userCursor.next();
                updateMaxUserID(user);
                userList.add(user);
            }
            callBack.onQuery(userList);
        } catch (AGConnectCloudDBException e) {
            callBack.onError(e.getLocalizedMessage());
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        } finally {
            userCloudDBZoneSnapshot.release();
        }
    }

    public interface UserCallBack {
        void onUpsert(User user);
        void onQuery(List<User> userList);
        void onDelete(List<User> userList);
        void onError(String errorMessage);
    }

}
