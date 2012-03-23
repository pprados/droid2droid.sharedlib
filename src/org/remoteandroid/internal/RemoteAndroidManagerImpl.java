package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.BINDING_NB_RETRY;
import static org.remoteandroid.internal.Constants.BINDING_TIMEOUT_WAIT;
import static org.remoteandroid.internal.Constants.D;
import static org.remoteandroid.internal.Constants.E;
import static org.remoteandroid.internal.Constants.ETHERNET;
import static org.remoteandroid.internal.Constants.HACK_DEAD_LOCK;
import static org.remoteandroid.internal.Constants.I;
import static org.remoteandroid.internal.Constants.PREFIX_LOG;
import static org.remoteandroid.internal.Constants.SCHEME_TCP;
import static org.remoteandroid.internal.Constants.TAG_CLIENT_BIND;
import static org.remoteandroid.internal.Constants.TIMEOUT_CONNECT_WIFI;
import static org.remoteandroid.internal.Constants.TIME_MAX_TO_DISCOVER;
import static org.remoteandroid.internal.Constants.V;
import static org.remoteandroid.internal.Constants.VERSION;
import static org.remoteandroid.internal.Constants.W;

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

import org.remoteandroid.ListRemoteAndroidInfo;
import org.remoteandroid.ListRemoteAndroidInfo.DiscoverListener;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.internal.Messages.Type;
import org.remoteandroid.internal.socket.ip.NetworkSocketRemoteAndroid;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class RemoteAndroidManagerImpl extends RemoteAndroidManager
{
	public static ApplicationInfo sAppInfo;
	
	// Action to bind to discovery service
	public static final String ACTION_CLI="org.remoteandroid.cli.DISCOVER";
	
	public final static Handler sHandler=new Handler();

    private static final int CORE_POOL_SIZE=3;
    private static final int MAXIMUM_POOL_SIZE=16;
    private static final long KEEP_ALIVE=10;
    
    private static final String MSG_SECURITY=
    	"Add the privilege \""+RemoteAndroidManager.PERMISSION_DISCOVER_RECEIVE+"\"";
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

                public Thread newThread(Runnable r) 
                {
                    return new Thread(r, "RemoteAndroid #" + mCount.getAndIncrement());
                }
            });    
	public final Context mAppContext;
	private IRemoteAndroidManager mManager;
	private static boolean noDiscoverPrivilege=false; // Remember if the process has the privilege to discover devices.
	private ServiceConnection mServiceConnection;
	private static final Intent sIntentRemoteAndroid=new Intent(ACTION_BIND_REMOTE_ANDROID);

	@Override
	public int getVersion()
	{
		return VERSION;
	}
	
	public static void bootStrap(final Context context,final RemoteAndroidManager.ManagerListener listener)
	{
		new RemoteAndroidManagerImpl(context.getApplicationContext(),listener);
	}

	private RemoteAndroidManagerImpl(Context appContext)
	{
		mAppContext=appContext.getApplicationContext();
		
		initAppInfo(mAppContext);
	}
	// Constructor for server.
	public RemoteAndroidManagerImpl(Context appContext,IRemoteAndroidManager manager)
	{
		this(appContext);
		mManager=manager;
	}
	// Constructor for client.
	public RemoteAndroidManagerImpl(Context appContext,final ManagerListener listener)
	{
		this(appContext);
		
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR)
		{
			BluetoothAdapter.getDefaultAdapter();
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
				listener.bind(RemoteAndroidManagerImpl.this);
			}
			
			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				mManager=null;
				listener.unbind(RemoteAndroidManagerImpl.this);
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
					public AbstractRemoteAndroidImpl factoryBinder(Context context,RemoteAndroidManagerImpl manager,Uri uri)
					{
						return new NetworkSocketRemoteAndroid(context,manager,uri);
					}
				});
		}
	}
	// -------------------------------------------------
	private long mLastTimeToDiscover=-1L;
	private int mLastFlag;
	
	@Override
	public synchronized void startDiscover(int flags,long timeToDiscover)
    {
		if (noDiscoverPrivilege)
			throw new IllegalStateException(MSG_SECURITY);
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
		if (noDiscoverPrivilege)
			return;
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
		if (noDiscoverPrivilege)
			return false;
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
	
	public boolean isInit()
	{
		return mManager!=null;
	}
	
	public long getCookie(String uri)
	{
		waitBinding(); // TODO: reconnection si necessaire
		try
		{
			return mManager.getCookie(uri);
		}
		catch (SecurityException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to get cookie (pairing rejected).");
		}
		catch (RemoteException e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Impossible to get cookie.");
		}
		return 0;
	}

	public void removeCookie(String uri)
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
	public long askCookie(Uri uri) throws SecurityException, IOException
	{
		return askCookie(uri,Type.CONNECT_FOR_COOKIE);
	}
	public long askCookie(Uri uri,Type type) throws SecurityException, IOException
	{
		Pair<RemoteAndroidInfoImpl,Long> msg=askMsgCookie(uri,type);
		if (msg==null) return 0;
		return msg.second;
	}
	public Pair<RemoteAndroidInfoImpl,Long> askMsgCookie(Uri uri) throws IOException, SecurityException
	{
		return askMsgCookie(uri,Type.CONNECT_FOR_COOKIE);
	}
	public Pair<RemoteAndroidInfoImpl,Long> askMsgCookie(Uri uri,Type type) throws IOException, SecurityException
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
			binder=driver.factoryBinder(mAppContext,RemoteAndroidManagerImpl.this,uri);
			return binder.connectWithAuthent(uri,type,TIMEOUT_CONNECT_WIFI);
		}
		catch (SecurityException e)
		{
			if (W && !D) Log.w(TAG_CLIENT_BIND,"Remote device refuse anonymous connection.");
			if (D) Log.d(TAG_CLIENT_BIND,"Remote device refuse anonymous connection.",e);
			throw e;
		}
		catch (SocketException e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection impossible for ask cookie. Imcompatible with ipv6? ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection impossible for ask cookie. Imcompatible with ipv6?",e);
			throw new SocketException(e.getMessage());
		}
		catch (IOException e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection impossible for ask cookie ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection impossible for ask cookie.",e);
			throw new IOException(e.getMessage(),e);
		}
		catch (Exception e)
		{
			if (E && !D) Log.e(TAG_CLIENT_BIND,"Connection impossible for ask cookie ("+e.getMessage()+")");
			if (D) Log.d(TAG_CLIENT_BIND,"Connection impossible for ask cookie.",e);
			throw new IOException("Connection impossible for ask cookie");
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
			int flags)
	{
    	final Uri uri=service.getData();
    	final ComponentName name=new ComponentName(RemoteAndroidManager.class.getName(),uri.toString());
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
    				AbstractRemoteAndroidImpl binder=driver.factoryBinder(mAppContext,RemoteAndroidManagerImpl.this,uri);
    					
    				final AbstractRemoteAndroidImpl fbinder=binder;
    				binder.connect(Type.CONNECT,0,TIMEOUT_CONNECT_WIFI);
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
    public ListRemoteAndroidInfo getBoundedDevices()
    {
		waitBinding();
		ListRemoteAndroidInfo rc=new ListRemoteAndroidInfoImpl(this);
		try
		{
			List<RemoteAndroidInfoImpl> boundedDevices=mManager.getBoundedDevices();
			for (int i=0;i<boundedDevices.size();++i)
			{
				rc.add(boundedDevices.get(i));
			}
		}
		catch (RemoteException e)
		{
			Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Remote exception "+e.getMessage(),e);
		}
		return rc; 
    }
    
	private void waitBinding() //TODO: reconnection
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
    public ListRemoteAndroidInfo newDiscoveredAndroid(DiscoverListener callback)
    {
		if (noDiscoverPrivilege)
			throw new IllegalStateException(MSG_SECURITY);
    	return new ListRemoteAndroidInfoImpl(this,callback);
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
