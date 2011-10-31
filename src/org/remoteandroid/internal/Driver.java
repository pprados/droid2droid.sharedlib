package org.remoteandroid.internal;

import org.remoteandroid.RemoteAndroidManager;

import android.net.Uri;

public interface Driver
{
	AbstractRemoteAndroidImpl factoryBinder(RemoteAndroidManager manager,Uri uri);
}
