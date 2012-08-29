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
package org.droid2droid.internal;

import static org.droid2droid.internal.Constants.BINDING_NB_RETRY;
import static org.droid2droid.internal.Constants.BINDING_TIMEOUT_WAIT;
import static org.droid2droid.internal.Constants.COOKIE_EXCEPTION;
import static org.droid2droid.internal.Constants.COOKIE_SECURITY;
import static org.droid2droid.internal.Constants.D;
import static org.droid2droid.internal.Constants.E;
import static org.droid2droid.internal.Constants.ETHERNET;
import static org.droid2droid.internal.Constants.HACK_DEAD_LOCK;
import static org.droid2droid.internal.Constants.I;
import static org.droid2droid.internal.Constants.PREFIX_LOG;
import static org.droid2droid.internal.Constants.SCHEME_TCP;
import static org.droid2droid.internal.Constants.TAG_CLIENT_BIND;
import static org.droid2droid.internal.Constants.TIMEOUT_CONNECT_WIFI;
import static org.droid2droid.internal.Constants.TIME_MAX_TO_DISCOVER;
import static org.droid2droid.internal.Constants.V;
import static org.droid2droid.internal.Constants.VERSION_D2D;
import static org.droid2droid.internal.Constants.W;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.ListRemoteAndroidInfo;
import org.droid2droid.RemoteAndroidInfo;
import org.droid2droid.internal.Messages.Type;
import org.droid2droid.internal.socket.ip.NetworkSocketRemoteAndroid;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;



@TargetApi(9)
public final class Droid2DroidManagerImpl extends Droid2DroidManager
{
	public static ApplicationInfo sAppInfo;
	
	// Action to bind to discovery service
	public static final String ACTION_CLI="org.droid2droid.cli.DISCOVER";
	
	public final static Handler sHandler=new Handler();

    private static final int CORE_POOL_SIZE=3;
    private static final int MAXIMUM_POOL_SIZE=16;
    private static final long KEEP_ALIVE=10;
    
    private static final String MSG_SECURITY=
    	"Add the privilege \""+PERMISSION_DISCOVER_RECEIVE+"\"";
    private static final ThreadPoolExecutor sExecutor = 
    	new ThreadPoolExecutor(
    		CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, 
            KEEP_ALIVE, 
            TimeUnit.SECONDS, 
            new LinkedBlockingQueue<Runnable>(10), 
            new ThreadFactory() 
            {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
				public Thread newThread(Runnable r) 
                {
                    return new Thread(r, "RemoteAndroid #" + mCount.getAndIncrement());
                }
            });    
	public final Context mAppContext;
	private IRemoteAndroidManager mManager;
	private ServiceConnection mServiceConnection;
	private static final Intent sIntentRemoteAndroid=new Intent(ACTION_BIND_REMOTE_ANDROID);

	@Override
	public int getVersion()
	{
		return VERSION_D2D;
	}

	private Droid2DroidManagerImpl(Context appContext)
	{
		mAppContext=appContext.getApplicationContext();
		
		initAppInfo(mAppContext);
	}
	// Constructor for server.
	public Droid2DroidManagerImpl(Context appContext,IRemoteAndroidManager manager)
	{
		this(appContext);
		mManager=manager;
	}
	// Constructor for client.
	public Droid2DroidManagerImpl(Context appContext,final ManagerListener listener)
	{
		this(appContext);
		
		if (VERSION.SDK_INT>=VERSION_CODES.ECLAIR)
		{
			Droid2DroidManagerImpl.getBluetoothAdapter();
		}
		mServiceConnection=new ServiceConnection()
		{
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				mManager=IRemoteAndroidManager.Stub.asInterface(service);
//				if (mManager.mLastTimeToDiscover!=-1)
//				{
//					mManagerstartDiscover(mLastFlag,mLastTimeToDiscover);
//					mManagermLastTimeToDiscover=0;
//				}
				listener.bind(Droid2DroidManagerImpl.this);
			}
			
			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				mManager=null;
				listener.unbind(Droid2DroidManagerImpl.this);
			}
		};
		appContext.bindService(sIntentRemoteAndroid,mServiceConnection,
			Context.BIND_AUTO_CREATE|Context.BIND_NOT_FOREGROUND|Context.BIND_IMPORTANT);
	}
	
	@Override
	public void close()
	{
		if (mManager!=null && mServiceConnection!=null)
		{
			mAppContext.unbindService(mServiceConnection);
		}
		mManager=null;
	}
	static private boolean mGetBluetoothAdapter;
	static private BluetoothAdapter mBluetoothAdapter;
	public static BluetoothAdapter getBluetoothAdapter()
	{
		if (mGetBluetoothAdapter)
			return mBluetoothAdapter; 
		mGetBluetoothAdapter=true;
		return mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	}

	public static void initAppInfo(final Context applicationContext) throws Error
	{
		try
		{
			sAppInfo=applicationContext.getPackageManager().getApplicationInfo(applicationContext.getPackageName(), 0);
		}
		catch (NameNotFoundException e)
		{
			throw new Error();
		}
	}

	@Override
	public Context getContext()
	{
		return mAppContext;
	}
	
	public static HashMap<String,Driver> sDrivers;
	static
	{
		sDrivers=new HashMap<String,Driver>();
		if (ETHERNET)
		{
			sDrivers.put(SCHEME_TCP,
				new Driver()
				{
	
					@Override
					public AbstractRemoteAndroidImpl factoryBinder(Context context,Droid2DroidManagerImpl manager,Uri uri)
					{
						return new NetworkSocketRemoteAndroid(context,manager,uri);
					}
				});
		}
	}
	// -------------------------------------------------
	private long mLastTimeToDiscover=-1L; // FIXME
	private int mLastFlag;
	
	@Override
	public synchronized void startDiscover(int flags,long timeToDiscover)
    {
		waitBinding();
		if (timeToDiscover==TIME_MAX_TO_DISCOVER)
			mLastTimeToDiscover=TIME_MAX_TO_DISCOVER;
		mLastTimeToDiscover=timeToDiscover;
		mLastFlag=flags;
		try
		{
			mManager.startDiscover(flags,timeToDiscover);
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to start the discovery process.");
		}
    }
	@Override
    public synchronized void cancelDiscover()
    {
		mLastTimeToDiscover=0;
		waitBinding();
		try
		{
			mManager.cancelDiscover();
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to stop the discovery process.");
		}
    }

	@Override
	public boolean isDiscovering()
	{
		//FIXME: dead-lock sur waitBinding lors de l'init à cause de l'event reseau qui arrive dans le main thread.
		if (HACK_DEAD_LOCK)
		{
			if (mManager==null) return false;
		}
		else
			waitBinding();
		//Donc stratégie anti-dead-lock
		try
		{
			return mManager.isDiscovering();
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to stop the discovery process.");
		}
		return false;
	}
	
	public final boolean isInit()
	{
		return mManager!=null;
	}
	
	public final long getCookie(int flags,String uri)
	{
		waitBinding(); // TODO: reconnection si necessaire
		try
		{
			return mManager.getCookie(flags,uri);
		}
		catch (SecurityException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to get cookie (pairing rejected).");
			return COOKIE_SECURITY;
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to get cookie.");
			return COOKIE_EXCEPTION;
		}
	}

	public final void removeCookie(String uri)
	{
		waitBinding();
		try
		{
			mManager.removeCookie(uri);
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to remove cookie.");
			// Ignore
		}
	}
	public final long askCookie(Uri uri,Type type,int flags) throws SecurityException, IOException
	{
		Pair<RemoteAndroidInfoImpl,Long> msg=askMsgCookie(uri,type,flags);
		if (msg==null) return 0;
		return msg.second;
	}
	public final Pair<RemoteAndroidInfoImpl,Long> askMsgCookie(Uri uri,int flags) throws IOException, SecurityException
	{
		return askMsgCookie(uri,Type.CONNECT_FOR_COOKIE,flags);
	}
	public final Pair<RemoteAndroidInfoImpl,Long> askMsgCookie(Uri uri,Type type,int flags) throws IOException, SecurityException
	{
		AbstractRemoteAndroidImpl binder=null;
		try
		{
			String scheme=uri.getScheme();
			if (!ETHERNET && scheme.equals(SCHEME_TCP))
				return new Pair<RemoteAndroidInfoImpl,Long>(null,0L);
			Driver driver=sDrivers.get(uri.getScheme());
			if (driver==null)
				throw new MalformedURLException("Unknown "+uri);
			binder=driver.factoryBinder(mAppContext,Droid2DroidManagerImpl.this,uri);
			return binder.connectWithAuthent(uri,type,flags,TIMEOUT_CONNECT_WIFI);
		}
		catch (SecurityException e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection refused. ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection refused.",e);
			throw (SecurityException)e.fillInStackTrace();
		}
		catch (SocketException e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection impossible. Imcompatible with ipv6? ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection impossible. Imcompatible with ipv6?",e);
			throw new SocketException(e.getMessage());
		}
		catch (IOException e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection impossible ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection impossible.",e);
			if (VERSION.SDK_INT>=VERSION_CODES.GINGERBREAD)
				throw new IOException(e.getMessage(),e);
			else
				throw new IOException(e.getMessage());
		}
		catch (Exception e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection impossible ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection impossible.",e);
			if (VERSION.SDK_INT>=VERSION_CODES.GINGERBREAD)
				throw new IOException("Connection impossible",e);
			else
				throw new IOException("Connection impossible");
		}
		finally
		{
			if (binder!=null)
				binder.close();
		}
	}
	
	// -------------------------------------------------
	
	@Override
	public boolean bindRemoteAndroid(Intent service, final ServiceConnection conn,
			final int flags)
	{
    	final Uri uri=service.getData();
    	final ComponentName name=new ComponentName(Droid2DroidManager.class.getName(),uri.toString());
    	sExecutor.execute(new Runnable()
    	{
    		@Override
    		public void run()
    		{
    			try
    			{
    				Driver driver=sDrivers.get(uri.getScheme());
    				if (driver==null)
    					throw new MalformedURLException("Unknown "+uri);
    				AbstractRemoteAndroidImpl binder=driver.factoryBinder(mAppContext,Droid2DroidManagerImpl.this,uri);
    					
    				final AbstractRemoteAndroidImpl fbinder=binder;
    				binder.connect(Type.CONNECT,flags,0,TIMEOUT_CONNECT_WIFI);
    				binder.linkToDeath(new IBinder.DeathRecipient()
					{
						
						@Override
						public void binderDied()
						{
							fbinder.unlinkToDeath(this, 0);
							PostTools.postServiceDisconnected(conn,name);
						}
					}, 0);
    				
					if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"Post service connected... ("+Thread.currentThread().getId());
    				PostTools.postServiceConnected(conn,name, binder);
    			}
    			catch (MalformedURLException e)
    			{
    				if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"When you bind a remote android, the uri "+uri+" is invalide");
    				PostTools.postServiceDisconnected(conn,name); // TODO: Maintenir le lien
    			}
    			catch (IOException e)
    			{
    				if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"bindRemote",e);
    				PostTools.postServiceDisconnected(conn,name); // TODO: Maintenir le lien
    			}
    			catch (Exception e)
    			{
    				if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"bindRemote",e);
    				PostTools.postServiceDisconnected(conn,name); // TODO: Maintenir le lien
    			}
    		}
    	});
    	return true; // TODO If Alive
	}
	
	@Override
	public RemoteAndroidInfo getInfos()
	{
		waitBinding();
		try
		{
			return mManager.getInfo();
		}
		catch (RemoteException e)
		{
			Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Remote exception "+e.getMessage(),e);
		}
		return null;
	}

	@Override
    public ListRemoteAndroidInfo getBondedDevices()
    {
		waitBinding();
		ListRemoteAndroidInfo rc=newDiscoveredAndroid(getContext(), null);
		try
		{
			List<RemoteAndroidInfoImpl> bondedDevices=mManager.getBondedDevices();
			for (int i=0;i<bondedDevices.size();++i)
			{
				rc.add(bondedDevices.get(i));
			}
		}
		catch (RemoteException e)
		{
			Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Remote exception "+e.getMessage(),e);
		}
		return rc; 
    }
    
	public boolean isBonded(RemoteAndroidInfoImpl info)
	{
		waitBinding();
		try
		{
			return mManager.isBonded(info);
		}
		catch (RemoteException e)
		{
			Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Remote exception "+e.getMessage(),e);
			return false;
		}
	}
	private final void waitBinding() //TODO: reconnection
	{
		int cnt=0;
		while (mManager==null)
		{
			try
			{
				Thread.sleep(BINDING_TIMEOUT_WAIT);
				++cnt;
			}
			catch (InterruptedException e)
			{
				// Ignore
				if (D && mManager==null) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"Binding to RemoteAndroid failed.");
			}
			if (cnt==BINDING_NB_RETRY) 
				throw new IllegalStateException("Service Remote android not found");
		}
	}

	@TargetApi(9)
	@Override
	public NdefMessage createNdefMessage()
	{
		try
		{
			return mManager.createNdefMessage();
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,"Can't create ndef",e);
			return null;
		}
	}
	
	@Override
	public void setLog(int type, boolean state)
	{
		waitBinding();
		try
		{
			mManager.setLog(type, state);
		}
		catch (RemoteException e)
		{
			Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Remote exception "+e.getMessage(),e);
		}
		setLogInternal(type, state);
	}
	public static void setLogInternal(int type,boolean state)
	{
		if ((type & (1<<0))!=0) E=state;
		if ((type & (1<<1))!=0) W=state;
		if ((type & (1<<2))!=0) I=state;
		if ((type & (1<<3))!=0) D=state;
		if ((type & (1<<4))!=0) V=state;
	}
	
}
