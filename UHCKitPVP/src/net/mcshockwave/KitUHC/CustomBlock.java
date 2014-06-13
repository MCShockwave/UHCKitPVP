package net.mcshockwave.KitUHC;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class CustomBlock {
	BlockState						state;
	String							player;

	BukkitTask						remTask	= null;

	static ArrayList<CustomBlock>	blocks	= new ArrayList<>();

	CustomBlock(Block b, String pl, boolean isAir) {
		state = b.getState();
		player = pl;

		if (isAir) {
			state.setType(Material.AIR);
		}

		blocks.add(this);
	}

	public void setTime(int time) {
		remTask = Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
			public void run() {
				regen();
			}
		}, time * 20);
	}

	public void regen() {
		state.getWorld().playEffect(state.getLocation(), Effect.STEP_SOUND,
				state.getType() == Material.AIR ? state.getBlock().getType() : state.getType());

		state.update(true);
		removeBlock(this);

		if (remTask != null) {
			remTask.cancel();
		}
	}

	public BlockState getState() {
		return state;
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

	public static CustomBlock getBlockAt(Location l1) {
		for (CustomBlock b : getBlocks()) {
			Location l2 = b.state.getLocation();
			if (isSameLoc(l1, l2)) {
				return b;
			}
		}
		return null;
	}

	private static boolean isSameLoc(Location loc1, Location loc2) {
		return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY()
				&& loc1.getBlockZ() == loc2.getBlockZ() && loc1.getWorld() == loc2.getWorld();
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
}
