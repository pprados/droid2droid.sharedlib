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

import static org.droid2droid.internal.Constants.BUFFER_SIZE_FOR_DOWNLOAD;
import static org.droid2droid.internal.Constants.D;
import static org.droid2droid.internal.Constants.E;
import static org.droid2droid.internal.Constants.PREFIX_LOG;
import static org.droid2droid.internal.Constants.TAG_CLIENT_BIND;
import static org.droid2droid.internal.Constants.TAG_INSTALL;
import static org.droid2droid.internal.Constants.TAG_SECURITY;
import static org.droid2droid.internal.Constants.UPDATE_PARCEL;
import static org.droid2droid.internal.Constants.V;
import static org.droid2droid.internal.Constants.W;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;

import org.apache.http.MethodNotSupportedException;
import org.droid2droid.Droid2DroidManager;
import org.droid2droid.RemoteAndroid;
import org.droid2droid.RemoteAndroidInfo;
import org.droid2droid.internal.Messages.Type;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

// NAT file:///home/pprados/Bureau/J%20ICE/index.html
/** @hide */
public abstract class AbstractRemoteAndroidImpl implements RemoteAndroid,IRemoteAndroid, IBinder
{
	public static final int BIND_OID = 1;

	public static final int FINALIZE_OID = 2;

	public static final int PING_BINDER = 3;

	public static final int IS_BINDER_ALIVE = 4;
	
	public static final int UNBIND_SRV = 5;

	public static final int STATUS_REFUSE_CONNECTION_MODE=-1;
	public static final int STATUS_REFUSE_ANONYMOUS=-2;
	public static final int STATUS_REFUSE_NO_BOUND=-3;
	public static final int STATUS_INVALIDE_COOKIE=-4;
	public static final int STATUS_OK=0;
	
	@SuppressWarnings("unused")
	final private WeakReference<IRemoteAndroid> mSelf = new WeakReference<IRemoteAndroid>(
			this);

	public Droid2DroidManager mManager;
	protected Uri mUri;
	
	protected AbstractRemoteAndroidImpl(Droid2DroidManager manager,Uri uri)
	{
		mManager=manager;
		mUri=uri;
	}
	protected abstract int getConnectionId();
	
	public RemoteAndroidInfoImpl mInfo;

	@Override
	public RemoteAndroidInfo getInfos()
	{
		return mInfo;
	}
	class Rebind
	{
		RemoteBinderProxy proxy;
		Intent service;
		ComponentName name;
		ServiceConnection conn;
		int flags;
	};
	
	@Override
	public void disconnect(int connid)
	{
		synchronized (mBinders)
		{
			for (Binded binded:mBinders.values())
			{
				binded.conn.onServiceDisconnected(binded.name);
			}
			mBinders.clear();
		}		
		if (mDeathRecipient!=null)
			mDeathRecipient.binderDied();
	}
    @Override 
    public boolean isClosed()
    {
    	return mBinders.size()==0;
    }
	
	// --- IBinder interface ----------------------------------------------
	protected DeathRecipient mDeathRecipient; // FIXME: Use mDeathRecipient 

	private IInterface mOwner;

	private String mDescriptor;

	@Override
	public void dump(FileDescriptor fd, String[] args) throws RemoteException
	{
		throw new UnsupportedOperationException();
	}
	//@Override
	@Override
	public void dumpAsync(FileDescriptor arg0, String[] arg1) throws RemoteException
	{
		RemoteException re=new RemoteException();
		re.initCause(new MethodNotSupportedException("dumpAsync"));
		throw re;
	}

	/**
	 * Convenience method for associating a specific interface with the Binder.
	 * After calling, queryLocalInterface() will be implemented for you to
	 * return the given owner IInterface when the corresponding descriptor is
	 * requested.
	 */
	public void attachInterface(IInterface owner, String descriptor)
	{
		mOwner = owner;
		mDescriptor = descriptor;
	}

	@Override
	public String getInterfaceDescriptor() throws RemoteException
	{
		return mDescriptor;
	}

	@Override
	public IInterface queryLocalInterface(String descriptor)
	{
		return mOwner;
	}

	@Override
	public void linkToDeath(DeathRecipient recipient, int flags)
	{
		// Ignore flags;
		mDeathRecipient = recipient;
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient recipient, int flags)
	{
		// Ignore flags
		assert (mDeathRecipient == recipient);
		mDeathRecipient = null;
		return true;
	}

	// -------------------------------------------------

	private long mExecuteTimeout=10000L; // FIXME
	
	@Override
	public void setExecuteTimeout(long bindTimeout)
	{
		mExecuteTimeout=bindTimeout;
	}

	/** Send transactions to remote cloud. */
	@Override
	public int bindOID(int connid,int serviceConnectionOID,Intent intent,int flags,ComponentName[] name,long timeout) throws RemoteException
	{
		if (D) Log.d(TAG_CLIENT_BIND, PREFIX_LOG+"BindOID...");
		Parcel data = Parcel.obtain();
		Parcel reply = Parcel.obtain();
		try
		{
			
			data.writeInt(serviceConnectionOID);
			NormalizeIntent.writeIntent(intent,data, 0);
			data.writeInt(flags);
			transactRemoteAndroid(connid, BIND_OID, data, reply, 0,timeout);
			if (D) Log.d(TAG_CLIENT_BIND, PREFIX_LOG+"BindOID ok");
			name[0]=(ComponentName)reply.readParcelable(ComponentName.class.getClassLoader());
			return reply.readInt();
		}
		finally
		{
			if (data != null)
				data.recycle();
			if (reply != null)
				reply.recycle();
		}
	}
	@Override
	public boolean unbindService(ServiceConnection conn)
	{
		synchronized (mBinders)
		{
			Binded binder=mBinders.get(conn);
			if (binder!=null)
			{
				if (D) Log.d(TAG_CLIENT_BIND, PREFIX_LOG+"UnBindOID...");
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				try
				{
					
					
					try
					{
						data.writeInt(System.identityHashCode(conn));
						transactRemoteAndroid(getConnectionId(), UNBIND_SRV, data, reply, 0,mExecuteTimeout);
						if (D) Log.d(TAG_CLIENT_BIND, PREFIX_LOG+"UnBindOID ok");
					}
					catch (RemoteException e)
					{
						if (W && !D) Log.w(TAG_CLIENT_BIND,"Error when unbind ("+e.getMessage()+")");
						if (D) Log.w(TAG_CLIENT_BIND,"Errer when unbind",e);
					}
				}
				finally
				{
					if (data != null)
						data.recycle();
					if (reply != null)
						reply.recycle();
				}
				mBinders.remove(conn);
			}
			else
				return false;
		}
		
		return true;
	}
	

	@Override
	public void finalizeOID(int connid, int oid,long timeout)
	{
		if (oid == 0)
		{
			if (E) Log.e(TAG_CLIENT_BIND, PREFIX_LOG+"FinalizeOID with oid=0!");
			return;
		}
		if (!isBinderAlive())
			return;
		Parcel data = Parcel.obtain();
		Parcel reply = Parcel.obtain();
		try
		{
			data.writeInt(oid);
			transactRemoteAndroid(getConnectionId(), FINALIZE_OID, data, reply, 0,timeout);
			return;
		}
		catch (RemoteException e)
		{
			if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"FinalizeOID",e);
		}
		finally
		{
			if (data != null)
				data.recycle();
			if (reply != null)
				reply.recycle();
		}

	}

	@Override
	public boolean isBinderAlive(int connid,int oid,long timeout) throws RemoteException
	{
		if (D) Log.d(TAG_CLIENT_BIND, PREFIX_LOG+"isBinderAlive...");
		if (!isBinderAlive())
		{
			if (E) Log.e(TAG_CLIENT_BIND, PREFIX_LOG+"Binder is not alive");
			throw new RemoteException();
		}
		Parcel data = Parcel.obtain();
		Parcel reply = Parcel.obtain();
		try
		{
			data.writeInt(oid);
			transactRemoteAndroid(getConnectionId(), IS_BINDER_ALIVE, data, reply, 0,timeout);
			return reply.readByte() == 1;
		}
		finally
		{
			if (data != null)
				data.recycle();
			if (reply != null)
				reply.recycle();
		}
	}

	@Override
	public boolean pingBinder(int connid,int oid,long timeout) throws RemoteException
	{
		if (D) Log.d(TAG_CLIENT_BIND, PREFIX_LOG+"pingBinder...");
		if (!isBinderAlive())
			return false;
		Parcel data = Parcel.obtain();
		Parcel reply = Parcel.obtain();
		try
		{
			data.writeInt(oid);
			transactRemoteAndroid(getConnectionId(), PING_BINDER, data, reply, 0,timeout);
			return reply.readByte() == 1;
		}
		finally
		{
			if (data != null)
				data.recycle();
			if (reply != null)
				reply.recycle();
		}
	}

	static class Binded
	{
		ComponentName name;
		ServiceConnection conn;
		RemoteBinderProxy proxy;
	}
	
	private final WeakHashMap<ServiceConnection, Binded> mBinders=new WeakHashMap<ServiceConnection, Binded>();
	
	@Override
	public boolean bindService(final Intent service, final ServiceConnection conn, final int flags)
	{

    	new AsyncTask<Void, Void, Void>()
    	{
    		@Override
    		protected Void doInBackground(Void...v)
    		{
				try
				{
					ComponentName[] name=new ComponentName[1];
					int oid=bindOID(getConnectionId(),System.identityHashCode(conn),service,flags,name,mExecuteTimeout);
					if (oid==-1)
		    			PostTools.postServiceDisconnected(conn, name[0]);
					else
					{
		    			RemoteBinderProxy proxy=
		    				new RemoteBinderProxy(AbstractRemoteAndroidImpl.this,oid);
		    			Binded disconnect=new Binded();
		    			disconnect.name=name[0];
		    			disconnect.conn=conn;
		    			disconnect.proxy=proxy;
		    			synchronized (mBinders)
		    			{
		    				mBinders.put(conn, disconnect);
		    			}
		    			PostTools.postServiceConnected(conn,name[0], proxy);
					}
				}
				catch (SecurityException e)
				{
					if (E) Log.e(TAG_SECURITY,PREFIX_LOG+"Refuse connexion",e);
					close();
				}
				catch (RemoteException e)
				{
					PostTools.postServiceDisconnected(conn,null); // FIXME: null en ComponentName ?
				}
    			return null;
    		}
    	}.execute();
		return true;
	}
	
	public abstract Pair<RemoteAndroidInfoImpl,Long> connectWithAuthent(Uri uri,Type type,int flags,long timeout) throws UnknownHostException, IOException, RemoteException;
	
	private static final void postPublishProgress(final PublishListener listener,final int progress)
	{
		Droid2DroidManagerImpl.sHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onProgress(progress);
			}
		});
	}
	private static final void postPublishError(final PublishListener listener,final Throwable error)
	{
		Droid2DroidManagerImpl.sHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onError(error);
			}
		});
	}
	private static final void postPublishFinish(final PublishListener listener,final int status)
	{
		Droid2DroidManagerImpl.sHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onFinish(status);
			}
		});
	}
	
	@Override
	public void pushMe(Context context,PublishListener listener,int flags,final long timeout) throws RemoteException, IOException
	{
		pushMe(context,listener,BUFFER_SIZE_FOR_DOWNLOAD ,flags,timeout);
	}
	public void pushMe(
			final Context context,
			final PublishListener listener,
			final int bufsize,
			final int flags,
			final long timeout
			) 
		throws RemoteException, IOException
	{
		new AsyncTask<PublishListener,Integer,Object>()
		{

			private PublishListener listener;
			
			@Override
			protected Object doInBackground(PublishListener... params)
			{
				listener=params[0];
				FileInputStream in=null;
				int fd=-1;
				int connid=-1;
		    	int s;
	        	byte[] buf=new byte[bufsize];
	        	byte[] signature=null;
		    	try
		    	{
		        	PackageManager pm=context.getPackageManager();
		        	ApplicationInfo info=Droid2DroidManagerImpl.sAppInfo;
		        	String label=context.getString(info.labelRes);
					PackageInfo pi=pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
		        	File filename=new File(info.publicSourceDir);
		        	
		        	int versionCode=pi.versionCode;
	        		signature=pi.signatures[0].toByteArray();
		            
	        		long length=filename.length();
	        		connid=getConnectionId();
	        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI propose apk "+info.packageName);
		        	fd=proposeApk(connid,label,info.packageName,versionCode,signature,length,flags,timeout);
		        	if (fd>0)
		        	{
		        		// TODO: ask sender before send datas
		        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI it's accepted");
			        	in=new FileInputStream(filename);
				    	StringBuilder prog=new StringBuilder();
				    	// TODO: multi thread for optimize read latency ?
				    	long timeoutSendFile=30000; // FIXME: Time out pour send file en dur.
				    	long pos=0;
		        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI send file");
				    	while ((s=in.read(buf,0,buf.length))>0)
				    	{
				    		if (V) 
				    		{
					    		prog.append('*');
				    			if (V) Log.v(TAG_INSTALL,PREFIX_LOG+""+prog.toString()); // TODO: A garder ?
				    		}
				    		if (!sendFileData(connid,fd,buf,s,pos,length,timeoutSendFile))
				    		{
				    			if (E) Log.e(TAG_INSTALL, PREFIX_LOG+"Impossible to send file data");
				    			throw new RemoteException();
				    		}
				    		pos+=s;
				    		publishProgress((int)((double)pos/length*10000L));
				    	}
		        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI send file done");
			    		if (installApk(connid,label,fd,flags,timeout))
			    		{
			        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI apk is installed");
			    			return 1;
			    		}
			    		else
			    		{
			        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI install apk is canceled");
			    			return new CancellationException("Install apk "+pi.packageName+" is canceled");
			    		}
		        	}
		        	else
		        	{
		        		if (V) Log.v(TAG_INSTALL,PREFIX_LOG+"CLI install apk is accepted ("+fd+")");
		        		return fd;
		        	}
		    	}
				catch (NameNotFoundException e)
				{
					cancelCurrentUpload(timeout, fd, connid);
					return e;
				}
				catch (IOException e)
				{
					cancelCurrentUpload(timeout, fd, connid);
					return e;
				}
				catch (RemoteException e)
				{
					if (D) Log.d(TAG_INSTALL,"Impossible to push apk ("+e.getMessage()+")",e);
					cancelCurrentUpload(timeout, fd, connid);
					return e;
				}
		    	finally
		    	{
		    		if (in!=null)
		    		{
						try
						{
							in.close();
						}
						catch (IOException e)
						{
							// Ignore
						}
		    		}
		    	}
			}
			private void cancelCurrentUpload(final long timeout, int fd, int connid)
			{
				if (connid!=-1 && fd!=-1)
				{
					try
					{
						cancelFileData(connid, fd, timeout);
					}
					catch (RemoteException e1)
					{
						// Ignore
					}
				}
			}
			@Override
			protected void onProgressUpdate(Integer... values)
			{
				postPublishProgress(listener,values[0]);
			}
			@Override
			protected void onPostExecute(Object result)
			{
				if (result instanceof Throwable)
				{
					postPublishError(listener,(Throwable)result);
				}
				else
					postPublishFinish(listener,((Integer)result).intValue());
			}
		}.execute(listener);
	}

	protected final void updateData(Parcel data) // TODO: a placer plutot coté serveur
	{
		if (UPDATE_PARCEL)
		{
			int v=VERSION.SDK_INT;
			data.setDataPosition(0);
			if (v>=10) // Gingerbread_MR1+
			{
				data.readInt();
			}
			String enforceInterfaceName=data.readString(); // Read the interface name (see Parcel.cpp)
			assert (enforceInterfaceName!=null);
			byte[] bufDatas=data.marshall();		// Return all the buffer (with the specific enforceInterface
			int startDatas=data.dataPosition(); // Position after the first string
			
			// Create a new one with interface name + buffers
			Parcel p=Parcel.obtain();
			p.setDataPosition(0);
			p.writeString(enforceInterfaceName);
			int sizeInterface=p.dataPosition();
			byte[] bufInterface=p.marshall();		// Part of buffer only for the string
			p.recycle();
			
			// Extract the rest of the buffer
			byte[] result=new byte[sizeInterface+bufDatas.length-startDatas];
			System.arraycopy(bufInterface, 0, result, 0, sizeInterface);
			System.arraycopy(bufDatas, startDatas, result, sizeInterface, bufDatas.length-startDatas);
			data.unmarshall(result, 0, result.length);
		}
	}
	protected final void updateReply(Parcel reply)
	{
	}
	// -------------- Pairing
}
