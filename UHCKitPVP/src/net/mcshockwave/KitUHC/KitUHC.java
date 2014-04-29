package net.mcshockwave.KitUHC;

import net.mcshockwave.KitUHC.HoF.HallOfFame;
import net.mcshockwave.KitUHC.Menu.ItemMenuListener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class KitUHC extends JavaPlugin {

	public static KitUHC	ins;

	public void onEnable() {
		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);
		Bukkit.getPluginManager().registerEvents(new ItemMenuListener(), this);

		SQLTable.enable();

		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
			ProtocolManager pm = ProtocolLibrary.getProtocolManager();
			PacketAdapter pa = null;
			if (pa == null) {
				pa = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.LOGIN) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (event.getPacketType() == PacketType.Play.Server.LOGIN) {
							event.getPacket().getBooleans().write(0, true);
						}
					}
				};
			}
			pm.addPacketListener(pa);
		}
	}

	public void onDisable() {
		for (CustomBlock cb : CustomBlock.getBlocks()) {
			cb.regen();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("hof")) {
			HallOfFame.getMenu().open((Player) sender);
			return true;
		}

		if (label.equalsIgnoreCase("spawn") && sender instanceof Player) {
			Player p = (Player) sender;

			p.teleport(p.getWorld().getSpawnLocation());
			p.sendMessage("§aTeleported to spawn");
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setHealth(20);
			updateHealth(p);
			p.setLevel(0);

			return true;
		}

		if (label.equalsIgnoreCase("tog") && args.length > 0) {
			String toTog = args[0];

			if (toTog.equalsIgnoreCase("holo")) {
				boolean is = isHoloEnabled(sender.getName());
				SQLTable.Settings.set("Enable_Holo", "" + (is ? 0 : 1), "Username", sender.getName());

				sender.sendMessage("§aHolograms toggled " + (is ? "OFF" : "ON"));
			}

			return true;
		}

		if (sender instanceof Player && sender.isOp()) {
			Player p = (Player) sender;

			if (args[0].equalsIgnoreCase("setspawn")) {
				Location l = p.getLocation();
				int x = l.getBlockX();
				int y = l.getBlockY();
				int z = l.getBlockZ();
				p.getWorld().setSpawnLocation(x, y, z);
				p.sendMessage(String.format("§aSet spawn location to x%s y%s z%s", x, y, z));
			}
		}
		return false;
	}

	public static void updateHealth(final Player p) {
		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				Scoreboard s = Bukkit.getScoreboardManager().getMainScoreboard();

				if (s.getObjective(DisplaySlot.BELOW_NAME) != null) {
					s.getObjective(DisplaySlot.BELOW_NAME).getScore(p).setScore((int) (p.getHealth() * 5));
				}
				if (s.getObjective(DisplaySlot.PLAYER_LIST) != null) {
					s.getObjective(DisplaySlot.PLAYER_LIST).getScore(Bukkit.getOfflinePlayer(p.getPlayerListName()))
							.setScore((int) (p.getHealth() * 5));
				}
			}
		}, 1l);
	}

	public static void setUp(World w) {
		// TODO Set up world command
	}

	public static boolean isHoloEnabled(String pl) {
		return SQLTable.Settings.getInt("Username", pl, "Enable_Holo") == 1;
	}

}
