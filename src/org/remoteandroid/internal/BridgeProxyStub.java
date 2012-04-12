package org.remoteandroid.internal;

import java.io.FileDescriptor;

import org.apache.http.MethodNotSupportedException;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public final class BridgeProxyStub implements IBinder
{
	Binder mBinder;

	@Override
	public String getInterfaceDescriptor() throws RemoteException
	{
		return mBinder.getInterfaceDescriptor();
	}

	@Override
	public boolean pingBinder()
	{
		return mBinder.pingBinder();
	}

	@Override
	public boolean isBinderAlive()
	{
		return mBinder.isBinderAlive();
	}

	@Override
	public IInterface queryLocalInterface(String descriptor)
	{
		return mBinder.queryLocalInterface(descriptor);
	}

	@Override
	public void dump(FileDescriptor fd, String[] args) throws RemoteException
	{
		mBinder.dump(fd, args);
	}

	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException
	{
		return mBinder.transact(code, data, reply, flags);
	}

	@Override
	public void linkToDeath(DeathRecipient recipient, int flags)
			throws RemoteException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient recipient, int flags)
	{
		// TODO Auto-generated method stub
		return false;
	}

	//@Override
	public void dumpAsync(FileDescriptor arg0, String[] arg1) throws RemoteException
	{
		RemoteException re=new RemoteException();
		re.initCause(new MethodNotSupportedException("dumpAsync"));
		throw re;
	}
}
