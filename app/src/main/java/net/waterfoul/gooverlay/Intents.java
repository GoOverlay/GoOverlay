package net.waterfoul.gooverlay;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.waterfoul.gooverlay.interop.IntentStrings;

public final class Intents extends IntentStrings {
    public static String currentlyOpen = null;

    public static void broadcast(String intentName, Context context) {
        if(intentName.contains(".open") && !intentName.equals(POKEMON_GO_OPEN)) {
            if(currentlyOpen != null && currentlyOpen.equals(intentName)) {
                return;
            }
            closeCurrent(context);
            currentlyOpen = intentName;
        }
        Log.d("Intent", "Sending " + intentName);
        Intent intent = new Intent(intentName);
        intent.setAction(intentName);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }

    public static void closeApp(Context context) {
        closeCurrent(context);
        broadcast(POKEMON_GO_CLOSE, context);
    }

    public static void closeCurrent(Context context) {
        if(currentlyOpen != null) {
            broadcast(currentlyOpen.replace(".open", ".close"), context);
            currentlyOpen = null;
        }
    }
}
