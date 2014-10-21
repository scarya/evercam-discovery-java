package io.evercam.network.discovery;

import java.io.IOException;
import java.util.ArrayList;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;

/**
 * GatewayDevice
 * 
 * Searching for gateway device using UPnP and retrieve NAT table from router.
 */

public class GatewayDevice
{
	private int discoveryTimeout = 5000; // 5 secs
	private InternetGatewayDevice[] gatewayDevices;
	private boolean isRouter = false;;
	private InternetGatewayDevice IGD = null;
	private int tableSize = 0;;

	public GatewayDevice(String routerIP) throws Exception
	{
		try
		{
			gatewayDevices = InternetGatewayDevice.getDevices(discoveryTimeout);
			if (gatewayDevices != null)
			{
				for (int i = 0; i < gatewayDevices.length; i++)
				{
					String url = null;
					InternetGatewayDevice testIGD = gatewayDevices[i];

					url = testIGD.getIGDRootDevice().getPresentationURL().toString();
					if (url.contains(routerIP))
					{
						IGD = testIGD;
						tableSize = IGD.getNatTableSize();
						isRouter = true;
					}
					else
					{
						isRouter = false;
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (UPNPResponseException e)
		{
			e.printStackTrace();
		}
	}

	// Is UPnP enabled on router?
	public boolean isUPnPAvaliable()
	{
		if (IGD != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// get NAT mapped entity for specific IP
	public ArrayList<ActionResponse> getMatchedEntries(String ip)
	{
		ArrayList<ActionResponse> matchedEntrys = new ArrayList<ActionResponse>();
		for (int sizeIndex = 0; sizeIndex < tableSize; sizeIndex++)
		{
			try
			{
				ActionResponse mapEntry = IGD.getGenericPortMappingEntry(sizeIndex);
				String natIP = mapEntry
						.getOutActionArgumentValue(UpnpDiscovery.UPNP_KEY_INTERNAL_CLIENT);
				if (natIP.equals(ip))
				{
					matchedEntrys.add(mapEntry);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (UPNPResponseException e)
			{
				e.printStackTrace();
			}
		}
		return matchedEntrys;
	}

	public boolean isRouter()
	{
		return isRouter;
	}

	/**
	 * Return UPnP table as list Return an empty list if the device is not
	 * router or not UPnP enabled.
	 */
	public ArrayList<ActionResponse> getNatTableArray()
	{
		ArrayList<ActionResponse> mapEntries = new ArrayList<ActionResponse>();
		if (isRouter() && isUPnPAvaliable())
		{
			try
			{
				for (int sizeIndex = 0; sizeIndex < tableSize; sizeIndex++)
				{
					ActionResponse mapEntry = IGD.getGenericPortMappingEntry(sizeIndex);
					mapEntries.add(mapEntry);
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UPNPResponseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mapEntries;
	}
}