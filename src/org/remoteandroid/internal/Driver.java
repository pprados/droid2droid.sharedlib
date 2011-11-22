package org.remoteandroid.internal;

import org.remoteandroid.RemoteAndroidManager;

import android.content.Context;
import android.net.Uri;

public interface Driver
{
	AbstractRemoteAndroidImpl factoryBinder(Context context,RemoteAndroidManagerImpl manager,Uri uri);
}
