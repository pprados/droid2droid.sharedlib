/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.internal.socket.ip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.PublicKey;

import org.droid2droid.internal.Droid2DroidManagerImpl;
import org.droid2droid.internal.socket.AbstractSocketRemoteAndroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;


public final class NetworkSocketRemoteAndroid extends AbstractSocketRemoteAndroid<NetworkSocketBossSender>
{
	Context mContext;
	ConnectivityManager mConnectivityManager;
	public NetworkSocketRemoteAndroid(Context context,Droid2DroidManagerImpl manager,Uri uri)
	{
		super(manager,uri);
		mContext=context.getApplicationContext();
		mConnectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
	
	@Override
	public PublicKey getPeerPublicKey()
	{
		return mBootstrap.getPeerPublicKey();
	}
	@Override
	public String  getPeerUUID()
	{
		// TODO Auto-generated method stub
		return mBootstrap.getPeerUUID();
	}
}