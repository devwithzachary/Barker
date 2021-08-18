package site.zpweb.barker.model.db;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

public class BaseCloudDBZoneObject extends CloudDBZoneObject {
    private Integer id;

    protected BaseCloudDBZoneObject(Class<? extends CloudDBZoneObject> aClass) {
        super(aClass);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
