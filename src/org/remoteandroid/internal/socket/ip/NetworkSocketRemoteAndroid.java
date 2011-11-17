package org.remoteandroid.internal.socket.ip;

import java.io.IOException;
import java.net.UnknownHostException;

import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.internal.socket.AbstractSocketRemoteAndroid;

import android.net.Uri;
import android.os.RemoteException;


public class NetworkSocketRemoteAndroid extends AbstractSocketRemoteAndroid<NetworkSocketBossSender>
{
	public NetworkSocketRemoteAndroid(RemoteAndroidManager manager,Uri uri)
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
	    	mBootstrap=new NetworkSocketBossSender(mUri,mHandler);
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