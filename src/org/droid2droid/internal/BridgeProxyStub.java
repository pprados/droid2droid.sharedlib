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
	@Override
	public void dumpAsync(FileDescriptor arg0, String[] arg1) throws RemoteException
	{
		RemoteException re=new RemoteException();
		re.initCause(new MethodNotSupportedException("dumpAsync"));
		throw re;
	}
}
