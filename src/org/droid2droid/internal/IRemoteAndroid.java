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

import java.io.IOException;
import java.net.UnknownHostException;

import org.droid2droid.internal.Messages.Type;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcel;
import android.os.RemoteException;

/** @hide */
// Les méthodes de plus au context, pour déléguer les Binders et autres traitements
public interface IRemoteAndroid
{
    int bindOID(int connid,int serviceConnectionOID,Intent intent,int flags,ComponentName[] name,long timeout) throws RemoteException;
    void finalizeOID(int connid, int oid,long timeout);
    boolean isBinderAlive(int connid,int oid,long timeout) throws RemoteException;
    boolean pingBinder(int connid,int oid,long timeout) throws RemoteException;

    boolean transactRemoteAndroid(int connid,int code, Parcel data, Parcel reply, int flags,long timeout) throws RemoteException;
    boolean transactBinder(int connid,int oid,int code, Parcel data, Parcel reply, int flags,long timeout) throws RemoteException;

    int proposeApk(int connid,String label,String packagename,int version,byte[] signature,long len,int flags,long timeout) throws RemoteException;
    boolean sendFileData(int connid,int fd,byte[] data,int len,long pos,long size,long timeout) throws RemoteException;
    void cancelFileData(int connid,int fd,long timeout) throws RemoteException;
    boolean installApk(int connid,String label,int fd,int flags,long timeout) throws RemoteException;
    
    void close();
    void disconnect(int connid);
    boolean connect(Type mode,int flags,long cookie,long timeout) throws UnknownHostException, IOException, RemoteException;
}
