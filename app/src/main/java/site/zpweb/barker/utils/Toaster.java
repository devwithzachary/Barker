package site.zpweb.barker.utils;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

    public void sendErrorToast(Context context, String errorMessage){
        sendToast(context, "Error: " + errorMessage);
    }
    public void sendSuccessToast(Context context, String successMessage){
        sendToast(context, "Success: " + successMessage);
    }

    private void sendToast(Context context, String message){
        Toast.makeText(context,
                message,
                Toast.LENGTH_LONG).show();
    }

}
