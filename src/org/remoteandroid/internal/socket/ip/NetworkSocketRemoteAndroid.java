package org.remoteandroid.internal.socket.ip;

import java.io.IOException;
import java.net.UnknownHostException;

import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.internal.RemoteAndroidManagerImpl;
import org.remoteandroid.internal.socket.AbstractSocketRemoteAndroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.RemoteException;


public class NetworkSocketRemoteAndroid extends AbstractSocketRemoteAndroid<NetworkSocketBossSender>
{
	Context mContext;
	ConnectivityManager mConnectivityManager;
	public NetworkSocketRemoteAndroid(Context context,RemoteAndroidManagerImpl manager,Uri uri)
	{
		super(manager,uri);
		mContext=context.getApplicationContext();
		mConnectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	@Override
	public boolean connect(boolean forPairing,long timeout) throws UnknownHostException, IOException, RemoteException
	{
		//FIXME: pb de secu if (!mConnectivityManager.getActiveNetworkInfo().isConnected()) return false;
		return super.connect(forPairing,timeout);
	}
	
	@Override
	protected void initBootstrap() throws UnknownHostException, IOException
	{
		//FIXME: pb de secu if (!mConnectivityManager.getActiveNetworkInfo().isConnected()) return;
		if (mBootstrap==null) // FIXME : Multi-thread
		{
	    	mBootstrap=new NetworkSocketBossSender(mContext,mUri,mHandler);
	    	mBootstrap.start();
		}
	}
	
	@Override
	public void disconnect(int connid)
	{
		super.disconnect(connid);
		if (mBootstrap!=null) mBootstrap.close();
		mBootstrap=null;
	}
}