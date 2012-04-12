package org.remoteandroid.internal;

import org.remoteandroid.internal.RemoteAndroidInfoImpl;
import android.nfc.NdefMessage;
interface IRemoteAndroidManager
{

	RemoteAndroidInfoImpl getInfo();
	void startDiscover(int flags,long timeToDiscover);
	void cancelDiscover();
	boolean isDiscovering();
	
	List<RemoteAndroidInfoImpl> getBoundedDevices();
	long getCookie(String uri);
	void removeCookie(String uri);
	NdefMessage createNdefMessage();
	void setLog(int type,boolean state);
}