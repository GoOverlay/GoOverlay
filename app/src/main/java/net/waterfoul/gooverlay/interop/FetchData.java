package net.waterfoul.gooverlay.interop;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class FetchData implements IFetchData, ServiceConnection {
    private IFetchData connection = null;
    private Context ctx;

    @Override
    public IBinder asBinder() {
        return null;
    }

    public FetchData(Context ctx) {
        this.connect(ctx);
        this.ctx = ctx;
    }

    public void onDestroy() {
        ctx.unbindService(this);
    }

    private void connect(Context ctx) {
        Intent intent = new Intent(IntentStrings.APP_HOME);
        intent.setPackage("net.waterfoul.gooverlay");
        ctx.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        connection = IFetchData.Stub.asInterface((IBinder) service);;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        connection = null;
        this.connect(ctx);
    }

    @Override
    public SinglePokemon fetchSinglePokemon() throws RemoteException {
        if(this.connection == null) {
            return null;
        }
        return this.connection.fetchSinglePokemon();
    }
}
