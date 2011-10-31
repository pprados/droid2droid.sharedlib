package org.remoteandroid.internal;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/** @hide */
public class PostTools
{
	static void postServiceConnected(final ServiceConnection conn,final ComponentName name,final IBinder service)
	{
		RemoteAndroidManagerImpl.sHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				conn.onServiceConnected(name, service);
			}
		});
	}
	static void postServiceDisconnected(final ServiceConnection conn,final ComponentName name)
	{
		RemoteAndroidManagerImpl.sHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				conn.onServiceDisconnected(name);
			}
		});
	}
	

}
