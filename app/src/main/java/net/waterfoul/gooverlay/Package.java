package net.waterfoul.gooverlay;

import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import net.waterfoul.gooverlay.interop.IAppHome;

class Package {
    Drawable icon;
    String label;
    String packageName;
    IAppHome AppHome = null;
    View rowView = null;
    OpServiceConnection opServiceConnection = null;

    private boolean enabled;
    private boolean loadEnabled = true;

    public boolean getEnabled(boolean isEnabledDefault) {
        if(loadEnabled) {
            enabled = Prefs.getApplication(packageName, isEnabledDefault).enabled;
        }
        return enabled;
    }
    public boolean setEnabled(boolean newValue) {
        enabled = newValue;
        Prefs.getApplication(packageName, newValue).enabled = newValue;
        Prefs.saveApplications();
        return newValue;
    }

    public void renderAppHome() {
        Log.d("Package", "Render Atempt");
        if(AppHome != null && rowView != null) {
            Log.d("Package", "Ready");
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.appEnabled);
            Button button = (Button) rowView.findViewById(R.id.appSettings);
            try {
                if(AppHome.getSettingsIntent() != null) {
                    button.setVisibility(View.VISIBLE);
                }
                checkBox.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
