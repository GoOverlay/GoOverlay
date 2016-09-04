package net.waterfoul.gooverlay;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import net.waterfoul.gooverlay.interop.IFetchData;
import net.waterfoul.gooverlay.interop.SinglePokemon;

public class FetchDataService extends Service {
    static final String LOG_TAG = "PluginService1";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SVC", "Start");
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        Log.d("SVC", "Destroy");
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        Log.d("SVC", "Bind");
        return addBinder;
    }

    private final IFetchData.Stub addBinder =
            new IFetchData.Stub() {
                @Override
                public SinglePokemon fetchSinglePokemon() throws RemoteException {
                    if(OCRService.currentInstance == null) {
                        return null;
                    }

                    try {
                        return OCRService.currentInstance.fetchSinglePokemon();
                    } catch (Exception e) {
                        throw e;
                    }
                }
            };
}

