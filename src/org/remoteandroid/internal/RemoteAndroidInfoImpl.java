package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.SCHEME_TCP;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.UUID;

import org.remoteandroid.RemoteAndroidInfo;

import android.os.Parcel;
import android.os.Parcelable;

public final class RemoteAndroidInfoImpl implements RemoteAndroidInfo
{
	private static String	TAG	= "DiscoverAndroid";

	/** Identity. */
	public UUID				uuid;

	/** Public current name. */
	public String			name;

	/** Public key; */
	public PublicKey		publicKey;

	/** Os name. */
	public String			os	= "android";			// Reserve for port to other os

	/** Android version. */
	public int				version;

	/** Capability. */
	public long				feature;
	
	public boolean			isBonded;
	
	public boolean			acceptAnonymous;
	
	public boolean			isDiscoverBT;
	public boolean			isDiscoverEthernet;
	public boolean			isDiscoverGSM;
	public boolean			isDiscoverNFC;
	
	public ArrayList<String> uris=new ArrayList<String>(0);
	
	public RemoteAndroidInfoImpl()
	{
		
	}
	@Override
	public int hashCode()
	{
		return uuid.hashCode();
	}
	/**
	 * {@inheritDoc}
	 */
	public boolean merge(RemoteAndroidInfo inf)
	{
		boolean merged=false;
		boolean remove=inf.isRemovable();
		RemoteAndroidInfoImpl info=(RemoteAndroidInfoImpl)inf;
		if (!uuid.equals(info.uuid))
			throw new IllegalArgumentException("Can merge data only for the same uuid");
		if ((info.name!=null) && !name.equals(info.name))
		{
			merged=true;
			name=info.name;
		}
		if (publicKey==null && info.publicKey!=null)
		{
			merged=true;
			publicKey=info.publicKey;
		}
		if (info.version>version)
		{
			merged=true;
			version=info.version;
		}
		if (feature!=info.feature)
		{
			merged=true;
			feature=info.feature;
		}
		if (uris!=null)
		{
			// TODO: Mixed les uris ?
			merged=true;
			//FIXME: réorganise les uris lors du merge ? Messages.Candidates candidates=ProtobufConvs.toCandidates(info);
			
			for (int i=0;i<info.uris.size();++i)
			{
				String uri=info.uris.get(i);
				uris.remove(uri);
				uris.add(0,uri); // Add at top
			}
		}
		if (remove)
		{
			merged=true;
			uris.clear();
		}
		if (isBonded!=info.isBonded)
		{
			merged=true;
			isBonded=info.isBonded;
		}
		
		if (isDiscoverBT!=info.isDiscoverBT && info.isDiscoverBT)
		{
			merged=true;
			isDiscoverBT=info.isDiscoverBT;
		}
		if (isDiscoverEthernet!=info.isDiscoverEthernet && info.isDiscoverEthernet)
		{
			merged=true;
			isDiscoverEthernet=info.isDiscoverEthernet;
		}
		if (isDiscoverGSM!=info.isDiscoverGSM && info.isDiscoverGSM)
		{
			merged=true;
			isDiscoverGSM=info.isDiscoverGSM;
		}
		return merged;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID getUuid()
	{
		return uuid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PublicKey getPublicKey()
	{
		return publicKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getVersion()
	{
		return version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOs()
	{
		return os;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFeature()
	{
		return feature;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBound()
	{
		return isBonded;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDiscover()
	{
		return isDiscoverBT || isDiscoverEthernet || isDiscoverGSM || isDiscoverNFC;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemovable()
	{
		return !isBonded && !isDiscover();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object x)
	{
		if (!(x instanceof RemoteAndroidInfoImpl))
			return false;
		RemoteAndroidInfoImpl other=(RemoteAndroidInfoImpl)x;
		return uuid.equals(other.uuid);
	}
	@Override
	public void removeUri(String uri)
	{
		uris.remove(uri);
	}
	
	//TODO: Override method ?
	public boolean removeUrisWithScheme(String sheme)
	{
		boolean remove=false;
		for (int i=uris.size()-1;i>=0;--i)
		{
			if (uris.get(i).startsWith(sheme))
			{
				uris.remove(i);
				remove=true;
			}
		}
		return remove;
	}
	
	public void addUris(String uri)
	{
		uris.remove(uri);
		uris.add(0,uri);
	}
	
	public boolean isConnectableWithIP()
	{
		for (int i=uris.size()-1;i>=0;--i)
		{
			if (uris.get(i).startsWith(SCHEME_TCP) /*|| uris.get(i).startsWith(SCHEME_TCP6)*/)
				return true;
		}
		return false;
	}
	
	public void clearDiscover()
	{
		uris.clear();
		isDiscoverBT=isDiscoverEthernet=false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * Return the currents connection uris to connect to the remote android. This uris are ordered.
	 * The first one is the best.
	 * 
	 * @return An array of URIS.
	 */
	@Override
	public String[] getUris()
	{
		return uris.toArray(new String[uris.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append(name).append("->");
		if (uris.size()==0)
		{
			buf.append("Impossible to connect");
		}
		else
		{
			for (int i=0;i<uris.size();++i)
			{
				buf.append(uris.get(i)).append(',');
			}
			buf.setLength(buf.length() - 1);
		}
		if (isBonded) buf.append(" (Bonded)");
		if (isDiscoverBT) buf.append(" [BT]");
		if (isDiscoverEthernet) buf.append(" [Ethernet]");
		if (isDiscoverGSM) buf.append(" [GSM]");
		return buf.toString();
	}

	private static byte VERSION=1;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeByte(VERSION);
		dest.writeString(uuid.toString());
		dest.writeString(name);
		dest.writeByteArray(publicKey.getEncoded());
		dest.writeInt(version);
		dest.writeString(os);
		dest.writeLong(feature);
		dest.writeStringList(uris);
		dest.writeByte((byte) (isBonded ? 1 : 0));
		dest.writeByte((byte) (isDiscoverEthernet ? 1 : 0));
		dest.writeByte((byte) (isDiscoverBT ? 1 : 0));
		dest.writeByte((byte) (isDiscoverGSM ? 1 : 0));
	}
	public void readFromParcel(Parcel parcel)
	{
		try
		{
			parcel.readByte(); // VERSION
			uuid = UUID.fromString(parcel.readString());
			name = parcel.readString();
			byte[] pubBytes=parcel.createByteArray();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
			publicKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);
			version = parcel.readInt();
			os=parcel.readString();
			feature=parcel.readLong();
			uris=parcel.createStringArrayList();
			if (uris==null) uris=new ArrayList<String>(0);
			else
				uris.trimToSize();
			isBonded=(parcel.readByte()==1);
			isDiscoverEthernet=(parcel.readByte()==1);
			isDiscoverBT=(parcel.readByte()==1);
			isDiscoverGSM=(parcel.readByte()==1);
		}
		catch (InvalidKeySpecException e)
		{
			throw new Error(e);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new Error(e);
		}
	}

	
	/** The parcel creator. */
	public static final Parcelable.Creator<RemoteAndroidInfoImpl>	CREATOR	= 
		new Parcelable.Creator<RemoteAndroidInfoImpl>()
	{
		/**
		 * {@inheritDoc}
		 */
		@Override
		public RemoteAndroidInfoImpl createFromParcel(
				Parcel parcel)
		{
			RemoteAndroidInfoImpl da = new RemoteAndroidInfoImpl();
			da.readFromParcel(parcel);
			return da;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public RemoteAndroidInfoImpl[] newArray(int size)
		{
			return new RemoteAndroidInfoImpl[size];
		}
	};

}
