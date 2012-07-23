package org.remoteandroid.internal;
import org.remoteandroid.Friend;
import org.remoteandroid.ListRemoteAndroidInfo;
import org.remoteandroid.ListRemoteAndroidInfo.DiscoverListener;
import org.remoteandroid.RemoteAndroidManager.ManagerListener;

import android.content.Context;

public class FactoriesImpl
extends Friend.Factories
{
	public FactoriesImpl()
	{
		
	}
	@Override
	public ListRemoteAndroidInfo newDiscoveredAndroid(Context context,DiscoverListener callback)
    {
    	return new ListRemoteAndroidInfoImpl(context,callback);
    }

	@Override
	public void newManager(Context context, ManagerListener listener)
	{
		new RemoteAndroidManagerImpl(context.getApplicationContext(),listener);
	}

}
