// IAppHome.aidl
package net.waterfoul.gooverlay.interop;

interface IAppHome {
    String getSettingsIntent();
    void enable();
    void disable();
}
