package net.mcshockwave.KitUHC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class CustomBlock {
	Block							block;
	Material						m;
	byte							d;
	MaterialData					bs;
	String							player;
	
	BukkitTask remTask = null;

	static ArrayList<CustomBlock>	blocks	= new ArrayList<>();

	CustomBlock(Block b, Material ma, int da, String pl) {
		block = b;
		m = ma;
		d = (byte) da;
		if (m != Material.AIR) {
			bs = b.getState().getData();
		} else {
			bs = null;
		}
		player = pl;
	}

	public void setTime(int time) {
		remTask = Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
			public void run() {
				regen();
			}
		}, time * 20);
	}

	public void regen() {
		if (bs != null) {
			setState(this, bs);
		} else {
			getBlock().setType(m);
			getBlock().setData(d);
		}
		removeBlock(this);
		
		if (remTask != null) {
			remTask.cancel();
		}
	}

	public Block getBlock() {
		return block;
	}

	public Material getType() {
		return m;
	}

	public byte getData() {
		return d;
	}

	public String getPlayerName() {
		return player;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(getPlayerName());
	}

	public static CustomBlock[] getBlocks() {
		return blocks.toArray(new CustomBlock[0]);
	}

	public static CustomBlock getBlockAt(Block block) {
		for (CustomBlock b : getBlocks()) {
			if (b.getBlock() == block) {
				return b;
			}
		}
		return null;
	}

	public static void removeBlock(CustomBlock b) {
		blocks.remove(b);
		if (b.remTask != null) {
			b.remTask.cancel();
		}
	}

	public static CustomBlock[] getBlocksFromPlayer(String pl) {
		ArrayList<CustomBlock> ret = new ArrayList<>();
		for (CustomBlock b : getBlocks()) {
			if (b.getPlayerName().equals(pl)) {
				ret.add(b);
			}
		}
		return ret.toArray(new CustomBlock[0]);
	}

	public static Block setState(CustomBlock b, MaterialData md) {
		return setState(b.getBlock(), md);
	}

	public static Block setState(Block b, MaterialData md) {
		b.setType(md.getItemType());
		b.setData(md.getData());
		b.getState().setData(md);

		return b;
	}
}
