package net.mcshockwave.KitUHC;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
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

	public static CustomBlock getBlockAt(Block block) {
		for (CustomBlock b : getBlocks()) {
			if (b.state.getBlock().equals(block)) {
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
}
