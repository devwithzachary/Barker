package site.zpweb.barker.db;

import android.content.Context;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.ObjectTypeInfo;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import site.zpweb.barker.model.db.Comment;
import site.zpweb.barker.model.db.Follow;
import site.zpweb.barker.model.db.HasID;
import site.zpweb.barker.model.db.Like;
import site.zpweb.barker.model.db.ObjectTypeInfoHelper;
import site.zpweb.barker.model.db.Post;
import site.zpweb.barker.model.db.User;
import site.zpweb.barker.utils.Toaster;

public class CloudDBManager<T extends CloudDBZoneObject & HasID> {

    private final OnSnapshotListener<T> snapshotListener = (cloudDBZoneSnapshot, e) -> processResults(cloudDBZoneSnapshot);
    protected int maxID = 0;

    Toaster toaster = new Toaster();

    private final AGConnectCloudDB cloudDB;
    protected CloudDBZone cloudDBZone;

    protected final Context context;
    protected final DBCallBack callBack;

    private final Class<T> objectClass;

    public CloudDBManager(Context context, DBCallBack<T> callBack, Class<T> objectClass){
        cloudDB = AGConnectCloudDB.getInstance();
        this.context = context;
        this.callBack = callBack;
        this.objectClass = objectClass;
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
        }).addOnFailureListener(e -> {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        });
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
            ObjectTypeInfo info = ObjectTypeInfoHelper.getObjectTypeInfo();
            cloudDB.createObjectType(info);
        } catch (Exception e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        }
     }

    public void addSubscription(){

        CloudDBZoneQuery snapshotQuery = CloudDBZoneQuery.where(objectClass).equalTo("id", 0);

        try {
            ListenerHandler handler = cloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                    snapshotListener);
        } catch (Exception e) {
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        }
    }

    public void getAll(){
        query((CloudDBZoneQuery.where(objectClass)));
    }

    public void query(CloudDBZoneQuery query) {
        Task<CloudDBZoneSnapshot<T>> task = cloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        task.addOnSuccessListener(this::processResults)
                .addOnFailureListener(e -> toaster.sendErrorToast(context, e.getLocalizedMessage()));
    }

    public void upsert(T object) {
        Task<Integer> upsertTask = cloudDBZone.executeUpsert(object);
        upsertTask.addOnSuccessListener(integer -> {
            updateMaxID(object);
            callBack.onUpsert(object);
        }).addOnFailureListener(e -> callBack.onError(e.getLocalizedMessage()));
    }

    public void delete(T object){
        cloudDBZone.executeDelete(object);
    }

    public int getMaxUserID(){
        return maxID;
    }

    private void updateMaxID(T object ){
        if (maxID < object.getId()) {
            maxID = object.getId();
        }
    }

    private void processResults(CloudDBZoneSnapshot<T> userCloudDBZoneSnapshot) {
        if (userCloudDBZoneSnapshot == null) {
            return;
        }
        CloudDBZoneObjectList<T> cursor = userCloudDBZoneSnapshot.getSnapshotObjects();
        List<T> objectList = new ArrayList<>();

        try {
            while (cursor.hasNext()) {
                T object = cursor.next();
                updateMaxID(object);
                objectList.add(object);
            }
            callBack.onQuery(objectList);
        } catch (AGConnectCloudDBException e) {
            callBack.onError(e.getLocalizedMessage());
            toaster.sendErrorToast(context, e.getLocalizedMessage());
        } finally {
            userCloudDBZoneSnapshot.release();
        }
    }

    public interface DBCallBack<T> {
        void onUpsert(T upsertedObject);
        void onQuery(List<T> queriedObjects);
        void onDelete(List<T> deletedObjects);
        void onError(String errorMessage);
    }

}
