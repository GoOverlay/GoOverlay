package net.waterfoul.gooverlay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class PackageSettings {
    public boolean enabled;

    public PackageSettings(JSONObject obj) throws JSONException {
        enabled = obj.getBoolean("enabled");
    }
    public PackageSettings(boolean enabled) {
        enabled = enabled;
    }
}

public class Prefs {
    private static final String MAIN_PREFS = "net.waterfoul.gooverlay.APP_HOME";

    private static final String APPS_PREF = "Applications";

    private static SharedPreferences settings = null;
    private static HashMap<String, PackageSettings> Applications = null;

    public static void init(Activity activity) {
        if(settings == null) {
            settings = activity.getSharedPreferences(MAIN_PREFS, 0);
        }
    }


    public static void saveApplications() {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String result = gson.toJson(Applications);
        Log.d("Prefs", "Saving: " + result);
        editor.putString(APPS_PREF, result);
        editor.apply();
    }

    private static HashMap<String, PackageSettings> getApplications() {
        if(Applications == null) {
            Gson gson = new Gson();
            String setting = settings.getString(APPS_PREF, null);
            Log.d("Prefs", "Setting: " + (setting == null ? "null" : setting));
            Applications = gson.fromJson(
                    setting,
                    new TypeToken<HashMap<String, PackageSettings>>(){}.getType()
            );
            Log.d("Prefs", "Applications: " + (Applications == null ? "null": Applications.toString()));
            if(Applications == null) {
                Applications = new HashMap<>();
            }
        }
        return Applications;
    }

    public static PackageSettings getApplication(String name, boolean enabled) {
        PackageSettings app = getApplications().get(name);
        if(app == null) {
            app = new PackageSettings(enabled);
            getApplications().put(name, app);
            saveApplications();
        }
        return app;
    }
}
