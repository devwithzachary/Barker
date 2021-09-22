package site.zpweb.barker;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.PhoneAuthProvider;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import site.zpweb.barker.databinding.ActivityFeedBinding;
import site.zpweb.barker.model.db.Post;
import site.zpweb.barker.utils.AuthType;

public class FeedActivity extends AppCompatActivity implements PostManager.PostCallBack {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityFeedBinding binding;

    PostManager postManager;

    List<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postManager = new PostManager(this, this);

        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarFeed.toolbar);
        binding.appBarFeed.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postManager.makePost();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_feed);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_feed);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onUpsert(Post upsertedObject) {
        posts.add(0, upsertedObject);
    }

    @Override
    public void onQuery(List<Post> queriedObjects) {
        posts = queriedObjects;
    }

    @Override
    public void onDelete(List<Post> deletedObjects) {

    }

    @Override
    public void onError(String errorMessage) {

    }
}