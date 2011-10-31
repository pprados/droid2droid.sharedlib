package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.TIMEOUT_FINALIZE;
import static org.remoteandroid.internal.Constants.TIMEOUT_IS_BINDER_ALIVE;
import static org.remoteandroid.internal.Constants.TIMEOUT_PING_BINDER;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

import org.apache.http.MethodNotSupportedException;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/** @hide */

// Binder to remote android
public final class RemoteBinderProxy implements IBinder
{
	private IInterface mOwner;

	private String mDescriptor = "org.remoteandroid.RemoteAndroidBinderProxy";

	final private WeakReference<RemoteBinderProxy> mSelf = new WeakReference<RemoteBinderProxy>(
			this);

	private DeathRecipient mDeathRecipient; // TODO: Utiliser le mDeathRecipient

	private AbstractRemoteAndroidImpl mProvider;

	protected int mObject = 0; // Cloud OID for remote instance

	protected long mTimeoutTransact=50000L;
	protected RemoteBinderProxy(AbstractRemoteAndroidImpl provider, int oid)
	{
		mProvider = provider;
		mObject = oid;
	}

	@Override
	public boolean pingBinder()
	{
		try
		{
			return mProvider != null && mProvider.pingBinder(mProvider.getConnectionId(),mObject,TIMEOUT_PING_BINDER);
		}
		catch (RemoteException e)
		{
			return false;
		}
	}

	@Override
	public boolean isBinderAlive()
	{
		try
		{
			return mObject!=-1 && mProvider != null && mProvider.isBinderAlive(mProvider.getConnectionId(),mObject,TIMEOUT_IS_BINDER_ALIVE);
		}
		catch (RemoteException e)
		{
			return false;
		}
	}

	@Override
	public IInterface queryLocalInterface(String descriptor)
	{
		if (mDescriptor.equals(descriptor))
			return mOwner;
		return null;
	}

	public void attachInterface(IInterface owner, String descriptor)
	{
		mOwner = owner;
		mDescriptor = descriptor;
	}

	@Override
	public String getInterfaceDescriptor() throws RemoteException
	{
		return "org.remoteandroid.RemoteAndroidBinderProxy";
	}

	/** Send transactions to remote cloud. */
	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException
	{
		return mProvider.transactBinder(mProvider.getConnectionId(),mObject, code, data, reply, flags,mTimeoutTransact);
	}

	@Override
	public void linkToDeath(DeathRecipient recipient, int flags)
			throws RemoteException
	{
		mDeathRecipient = recipient;
		// Ignore flags ?
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient recipient, int flags)
	{
		mDeathRecipient = null;
		// Ignore flags ?
		return true;
	}

	@Override
	public void dump(FileDescriptor fd, String[] args) throws RemoteException
	{
		Parcel data = Parcel.obtain();
		data.writeFileDescriptor(fd);
		data.writeStringArray(args);
		try
		{
			transact(DUMP_TRANSACTION, data, null, 0);
		}
		finally
		{
			data.recycle();
		}
	}

	@Override
	public void finalize()
	{
		if (mProvider != null)
			mProvider.finalizeOID(mProvider.getConnectionId(), mObject,TIMEOUT_FINALIZE);
	}
	//@Override
	public void dumpAsync(FileDescriptor arg0, String[] arg1) throws RemoteException
	{
		RemoteException re=new RemoteException();
		re.initCause(new MethodNotSupportedException("dumpAsync"));
		throw re;
	}
}
