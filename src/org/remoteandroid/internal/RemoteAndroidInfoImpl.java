package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.I;
import static org.remoteandroid.internal.Constants.PREFIX_LOG;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.UUID;

import org.remoteandroid.RemoteAndroidInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RemoteAndroidInfoImpl implements RemoteAndroidInfo
{
	private static String	TAG	= "DiscoverAndroid";

	/** Identity. */
	public UUID				uuid;

	/** Public current name. */
	public String			name;

	/** Public key; */
	public PublicKey		publicKey;

	/** Android version. */
	public int				version;

	/** Capability. */
	public int				capability;
	
	/** Bluetooth mac. */
	public String			bluetoothid;

	/** Current inet address. */
	public InetAddress		address;

	/** Ethernet mac address. */
	public String			ethernetMac;

	public String			os	= "android";			// Reserve for port to other os

	public boolean			isBonded;
	
	public boolean			acceptAnonymous;
	
	public boolean			isDiscoverBT;
	public boolean			isDiscoverEthernet;
	public boolean			isDiscoverGSM;
	
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
		if (capability!=info.capability)
		{
			merged=true;
			capability=info.capability;
		}
		if (info.bluetoothid!=null && !info.bluetoothid.equals(bluetoothid))
		{
			merged=true;
			bluetoothid=info.bluetoothid;
		}
		if (info.address!=null && !info.address.equals(address))
		{
			merged=true;
			address=info.address;
		}
		if (remove)
		{
			merged=true;
			address=null;
		}
		if (info.ethernetMac!=null && info.ethernetMac.length()!=0 && !info.ethernetMac.equals(ethernetMac))
		{
			merged=true;
			ethernetMac=info.ethernetMac;
		}
		if (!isBonded)
		{
			merged=true;
			isBonded=info.isBonded;
		}
		if (isDiscoverBT!=info.isDiscoverBT)
		{
			merged=true;
			isDiscoverBT=info.isDiscoverBT;
		}
		if (isDiscoverEthernet!=info.isDiscoverEthernet)
		{
			merged=true;
			isDiscoverEthernet=info.isDiscoverEthernet;
		}
		if (isDiscoverGSM!=info.isDiscoverGSM)
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
	public int getCapability()
	{
		return capability;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBonded()
	{
		return isBonded;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDiscover()
	{
		return isDiscoverBT || isDiscoverEthernet || isDiscoverGSM;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemovable()
	{
		return !isBonded && !isDiscover();
	}

	public void clearDiscover()
	{
		address=null;
		isDiscoverBT=isDiscoverEthernet=false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBluetoothId()
	{
		return bluetoothid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InetAddress[] getInetAddresses()
	{
		return (address!=null) ? new InetAddress[]{address} : new InetAddress[]{}; // TODO: Manage multiple addresses.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEthernetMac()
	{
		return ethernetMac;
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
		ArrayList<String> uris = new ArrayList<String>();
		if (address != null)
		{
			uris.add("tcp://" + address.getHostAddress()); // TODO: Gestion du port
		}
		if (Compatibility.VERSION_SDK_INT>=Compatibility.VERSION_ECLAIR)
		{
			if ((bluetoothid != null) && bluetoothid.length() != 0)
			{
				try
				{
					BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
					if (ba.isEnabled())
					{
						boolean isBonded=false;
						for (BluetoothDevice bonded : ba.getBondedDevices())
						{
							if (bonded.getBondState() == BluetoothDevice.BOND_BONDED
									&& bonded.getAddress().equals(bluetoothid))
							{
								uris.add("bts://" + bonded.getName());
								isBonded=true;
								break;
							}
						}
						if (!isBonded && Compatibility.VERSION_SDK_INT > Compatibility.VERSION_GINGERBREAD && version > Compatibility.VERSION_GINGERBREAD) // Accept anonymous bluetooth
						{
							uris.add("bt://" + bluetoothid);
						}
					}
				}
				catch (SecurityException e)
				{
					// Ignore
					if (I)
						Log.i(TAG, PREFIX_LOG+'[' + Build.MANUFACTURER
								+ "] Add android.permission.BLUETOOTH in manifest");
				}
			}
		}
		return uris.toArray(new String[uris.size()]);
	}
	@Override
	public void remoteUri(String uri)
	{
		if (uri.startsWith("tcp"))
		{
			address=null;
		}
		else
		{
			bluetoothid=null;
		}
	}
	public boolean isConnectableWithBluetooth()
	{
		return (BluetoothAdapter.getDefaultAdapter().isEnabled() && (bluetoothid != null) && bluetoothid.length() != 0);
	}
	public boolean isConnectableWithIP()
	{
		return address!=null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append(name).append("->");
		String[] uris = getUris();
		if (uris.length > 0)
		{
			for (String s : uris)
			{
				buf.append(s).append(',');
			}
			buf.setLength(buf.length() - 1);
		}
		else
		{
			buf.append("Impossible to connect");
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
		dest.writeInt(capability);
		dest.writeString(bluetoothid);
		dest.writeByteArray(address==null ? null : address.getAddress());
		dest.writeString(ethernetMac);
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
			capability=parcel.readInt();
			bluetoothid = parcel.readString();
			byte[] add = parcel.createByteArray();
			try
			{
				if (add != null)
					address = InetAddress.getByAddress(add);
			}
			catch (UnknownHostException e)
			{
				// Ignore
				Log.i(TAG,'['+ Build.MANUFACTURER+ "] UnknownHostException",e);
			}
			ethernetMac = parcel.readString();
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

}
