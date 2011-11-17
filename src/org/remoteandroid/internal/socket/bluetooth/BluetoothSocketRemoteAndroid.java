package org.remoteandroid.internal.socket.bluetooth;

import static org.remoteandroid.internal.Constants.E;
import static org.remoteandroid.internal.Constants.PREFIX_LOG;
import static org.remoteandroid.internal.Constants.TAG_CLIENT_BIND;

import java.io.IOException;
import java.net.UnknownHostException;

import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.internal.socket.AbstractSocketRemoteAndroid;

import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;


public class BluetoothSocketRemoteAndroid extends AbstractSocketRemoteAndroid<BluetoothSocketBossSender>
{
	public BluetoothSocketRemoteAndroid(RemoteAndroidManager manager,Uri uri)
	{
		super(manager,uri);
	}
	@Override
	public boolean connect(boolean forPairing,long timeout) throws UnknownHostException, IOException, RemoteException
	{
		return super.connect(forPairing,timeout);
	}
	@Override
	protected void initBootstrap() throws UnknownHostException, IOException
	{
		if (mBootstrap==null) // FIXME : Multi-thread
		{
	    	mBootstrap=new BluetoothSocketBossSender(mUri,mHandler);
	    	mBootstrap.start();
		}
	}
	@Override
	public void disconnect(int connid)
	{
		try
		{
			super.disconnect(connid);
			if (mBootstrap!=null)
				mBootstrap.close();
			mBootstrap=null;
		}
		catch (IOException e)
		{
			if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Error when disconnect ("+Thread.currentThread().getId()+")",e);
		}
	}
}