package org.remoteandroid.internal.socket;

import static org.remoteandroid.internal.Constants.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.remoteandroid.RemoteAndroidManager;
import static org.remoteandroid.RemoteAndroidManager.*;

import org.remoteandroid.internal.AbstractProtoBufRemoteAndroid;
import org.remoteandroid.internal.Login;
import org.remoteandroid.internal.Pairing;
import org.remoteandroid.internal.RemoteAndroidManagerImpl;
import org.remoteandroid.internal.Messages.Type;
import org.remoteandroid.internal.Pair;
import org.remoteandroid.internal.RemoteAndroidInfoImpl;
import org.remoteandroid.internal.Messages.Msg;

import android.net.Uri;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public abstract class AbstractSocketRemoteAndroid<T extends BossSocketSender> extends AbstractProtoBufRemoteAndroid
{
	protected T mBootstrap;
	
	protected AbstractSocketRemoteAndroid(RemoteAndroidManager manager,Uri uri)
	{
		super(manager,uri);
	}
	protected DownstreamHandler mHandler=new DownstreamHandler()
	{
		
		@Override
		public void messageReceived(Msg msg)
		{
			ReadWait rw=mLocks.get(msg.getThreadid());
			if (rw!=null)
			{
				try
				{
					rw.lock.lock();
					rw.mResponse=msg;
			    	rw.mCondition.signal();
				}
				finally
				{
					if (rw!=null) // Message to late ? Ignore
						rw.lock.unlock();
				}
			}
		}
		
		@Override
		public void channelDisconnected(Throwable e)
		{
			if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Channel disconnected "+e);
			synchronized (mLocks)
			{
				for (ReadWait rw:mLocks.values())
				{
					try
					{
						rw.lock.lock();
						rw.mException=new IOException("Channel close");
				    	rw.mCondition.signal();
					}
					finally
					{
						rw.lock.unlock();
					}
				}
				mLocks.clear();
			}
			if (mDeathRecipient!=null)
				mDeathRecipient.binderDied();
		}
	};	
    
    @Override
	public void close()
	{
    	disconnect(getConnectionId());
	}

	@Override
	public boolean isBinderAlive()
	{
		return (mBootstrap!=null) ? mBootstrap.isConnected() : false;
	}

	@Override
	public int getConnectionId()
	{
		return System.identityHashCode(Thread.currentThread());
	}

	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException
	{
		return false;
	}

	@Override
	public Pair<RemoteAndroidInfoImpl,Long> connectWithAuthent(Uri uri,Type type,int flags,long timeout) throws UnknownHostException, IOException, RemoteException
	{
		initBootstrap();
		Pair<RemoteAndroidInfoImpl,Long> result=Login.getLogin().client(this, uri,type,flags,timeout);
		boolean tryPairing=false;
		long cookie=result.second;
		if (result.first!=null)
		{
			boolean isBonded=Pairing.isTemporaryAcceptAnonymous() || ((RemoteAndroidManagerImpl)mManager).isBonded(result.first);
			if ((cookie==COOKIE_NO) || (cookie==COOKIE_SECURITY) || (cookie==COOKIE_EXCEPTION))
			{
				if ((flags & FLAG_PROPOSE_PAIRING)!=0)
					tryPairing=true;
			}
			else
			{
				if (((flags & FLAG_ACCEPT_ANONYMOUS)==0) && !isBonded)
				{
					cookie=COOKIE_NO;
				}
				if ((cookie==COOKIE_NO) && ((flags & FLAG_PROPOSE_PAIRING)!=0) && !isBonded) 
				{
					tryPairing=true;
				}
			}
			if (tryPairing)
			{
				result=Pairing.getPairing().client(this, uri, type, flags, timeout);
				cookie=result.second;
			}
		}
		if ((cookie==COOKIE_NO) || (cookie==COOKIE_SECURITY) || (cookie==COOKIE_EXCEPTION))
		{
			if (E) Log.e(TAG_SECURITY,PREFIX_LOG+"Invalide challenge");
			throw new SecurityException("Invalide challenge");
		}

		return result;
	}
	

	@Override
	public Msg sendRequestAndReadResponse(Msg msg, long timeout) throws RemoteException
	{
    	final long threadid=Thread.currentThread().getId();
    	final ReadWait rw=new ReadWait();
    	mLocks.put(threadid, rw);

    	Type t=msg.getType();
    	if ((t!=Type.CONNECT_FOR_BROADCAST) && !isBinderAlive())
    	{
    		if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Binder is not alive");
    		throw new RemoteException();
    	}

    	try
    	{
        	rw.lock.lock();
        	mBootstrap.pushMessage(msg);
    		// Wait response
    		if (!rw.mCondition.await(timeout,TimeUnit.SECONDS))
    		{
    			if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Timeout >"+timeout+"s");
        		mLocks.remove(threadid);
        		if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"Binder timeout");
    			throw new RemoteException();
    		}
    		mLocks.remove(threadid);
    		if (rw.mException!=null)
    		{
    			if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Capture error when send and read request",rw.mException);
        		if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+rw.mException.getMessage());
    			throw new RemoteException();
    		}
    		return rw.mResponse;
    	}
		catch (InterruptedException e)
		{
			if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Read socket",e);
    		mLocks.remove(threadid);
    		throw new RemoteException(); // TODO: Retourner une interruptidRemoteException
		}
    	finally
    	{
    		rw.lock.unlock();
    	}
	}
}
