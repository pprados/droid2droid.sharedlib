package org.remoteandroid.internal;

import android.content.Context;
import android.net.Uri;

public interface Driver
{
	AbstractRemoteAndroidImpl factoryBinder(Context context,RemoteAndroidManagerImpl manager,Uri uri);
}
