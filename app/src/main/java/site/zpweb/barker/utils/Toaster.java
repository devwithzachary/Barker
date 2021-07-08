package site.zpweb.barker.utils;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

    public void sendErrorToast(Context context, String errorMessage){
        Toast.makeText(context,
                "Error: " + errorMessage,
                Toast.LENGTH_LONG).show();
    }
}
