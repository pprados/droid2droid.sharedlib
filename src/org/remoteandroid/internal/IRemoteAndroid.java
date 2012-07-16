package org.remoteandroid.internal;

import java.io.IOException;
import java.net.UnknownHostException;

import org.remoteandroid.internal.Messages.Type;

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
