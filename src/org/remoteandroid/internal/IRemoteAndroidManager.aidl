package org.remoteandroid.internal;

import org.remoteandroid.internal.RemoteAndroidInfoImpl;
import android.nfc.NdefMessage;
interface IRemoteAndroidManager
{

	RemoteAndroidInfoImpl getInfo();
	void startDiscover(int flags,long timeToDiscover);
	void cancelDiscover();
	boolean isDiscovering();
	
	List<RemoteAndroidInfoImpl> getBondedDevices();
	boolean isBonded(in RemoteAndroidInfoImpl info);
	long getCookie(int flags,String uri);
	void removeCookie(String uri);
	NdefMessage createNdefMessage();
	void setLog(int type,boolean state);
}