package com.pfaeff_and_cdkrot.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;

import com.pfaeff_and_cdkrot.ForgeMod;
import com.pfaeff_and_cdkrot.api.benchmark.BenchmarkRegistry;
import com.pfaeff_and_cdkrot.tileentity.TileEntityBenchmark;
import com.pfaeff_and_cdkrot.util.Utility;

import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler implements
		cpw.mods.fml.common.network.IPacketHandler
{

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		System.out.println(side);
		try
		{
			//set benchmark text
			if (side.isServer() && packet.channel.equals("mechanics|1"))
			{
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
				int worldid = dis.readInt();
				int x = dis.readInt(); int y = dis.readInt(); int z = dis.readInt();			
				WorldServer ws = MinecraftServer.getServer().worldServers[worldid];	

				String text = dis.readUTF();
				TileEntityBenchmark tile = (TileEntityBenchmark) ws.getBlockTileEntity(x,y,z);
				if (BenchmarkRegistry.instance.onTextChanged(tile, text, player))
					tile.s=text;
				return;
			}
			if (packet.channel.equals("mechanics|2"))
			{
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
				GamePosition position = GamePosition.readFromStream(dis);

				//!server player request GUI
				if (side.isServer())
				{
					TileEntityBenchmark tile = (TileEntityBenchmark)position.getTileEntity
							(MinecraftServer.getServer());
					if (BenchmarkRegistry.instance.requestEditor(tile, player))
					{
                		ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);
						position.writeToStream(dos);
						dos.writeUTF(tile.s);                    				
                    	PacketDispatcher.sendPacketToPlayer
                    	(new Packet250CustomPayload("mechanics|2", baos.toByteArray()), player);
					}
				}
				//!client - open gui
				else
				{
					SidedNetworkStuff.openBenchmarkGUI(position, dis.readUTF());
				}		
		
			}
		}
		catch (Throwable thr)
		{
			ForgeMod.modLogger.log(Level.SEVERE, "Exception during packet processing. Somebody is trying to hack or Network goes wrong?", thr);
		}
		return;
	}
	
}