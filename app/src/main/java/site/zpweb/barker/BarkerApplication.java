package site.zpweb.barker;

import android.app.Application;

import site.zpweb.barker.db.CloudDBManager;

public class BarkerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CloudDBManager.initCloudDB(this);
    }
}
