package net.mcshockwave.KitUHC;

import net.mcshockwave.KitUHC.Menu.ItemMenuListener;
import net.mcshockwave.UHC.UltraHC;
import net.mcshockwave.UHC.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class KitUHC extends JavaPlugin {

	public static KitUHC	ins;

	public static boolean	enabled	= true;

	public void onEnable() {
		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);
		Bukkit.getPluginManager().registerEvents(new ItemMenuListener(), this);

		saveDefaultConfig();

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
		if (label.equalsIgnoreCase("spawn") && sender instanceof Player) {
			Player p = (Player) sender;

			int r = 25;
			for (Entity e : p.getNearbyEntities(r, r, r)) {
				if (e instanceof Player && e != p) {
					p.sendMessage("§cYou cannot do that while players are nearby!");
					return true;
				}
			}

			p.teleport(p.getWorld().getSpawnLocation());
			p.sendMessage("§aTeleported to spawn");
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setHealth(20);
			updateHealth(p);
			p.setLevel(0);

			return true;
		}

		if (label.equalsIgnoreCase("arenatog")) {

			if (sender.isOp()) {
				enabled = !enabled;

				Bukkit.broadcastMessage("§cArena " + (enabled ? "enabled" : "disabled") + " by " + sender.getName());
				
				for (CustomBlock cb : CustomBlock.getBlocks()) {
					cb.regen();
				}
			}

			return true;
		}

		// if (label.equalsIgnoreCase("tog") && args.length > 0) {
		// String toTog = args[0];
		//
		// if (toTog.equalsIgnoreCase("holo")) {
		// boolean is = isHoloEnabled(sender.getName());
		// SQLTable.Settings.set("Enable_Holo", "" + (is ? 0 : 1), "Username",
		// sender.getName());
		//
		// sender.sendMessage("§aHolograms toggled " + (is ? "OFF" : "ON"));
		// }
		//
		// return true;
		// }

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

			if (args[0].equalsIgnoreCase("setupworld")) {
				World w = KitUHC.isUHCEnabled() ? Multiworld.getKit() : p.getWorld();

				setUp(w);
			}

			if (isUHCEnabled() && args[0].equalsIgnoreCase("regenWorld")) {
				for (Player p2 : Multiworld.getKit().getPlayers()) {
					p2.teleport(Multiworld.getLobby().getSpawnLocation());
				}

				p.sendMessage("Deleting...");
				UltraHC.deleteWorld(Multiworld.getKit());
				p.sendMessage("Loading...");
				Multiworld.loadAll();
			}
		}
		return false;
	}

	public static void updateHealth(final Player p) {
		if (isUHCEnabled()) {
			UltraHC.updateHealthFor(p);
			return;
		}

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			@SuppressWarnings("deprecation")
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
		Bukkit.broadcastMessage("§a§nSetting up world...§r\n ");

		Bukkit.broadcastMessage("§eSetting gamerules...");
		String[] rules = { "doMobSpawning:false", "doDaylightCycle:false", "doFireTick:false", "doMobLoot:false",
				"doTileDrops:false", "mobGriefing:false" };
		for (String s : rules) {
			String[] ss = s.split(":");
			w.setGameRuleValue(ss[0], ss[1]);
		}

		Bukkit.broadcastMessage("§eSetting up border...");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + w.getName() + " set 200 0 0");

		Bukkit.broadcastMessage("§eStarting fill task...");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + w.getName() + " fill");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm");

		Bukkit.broadcastMessage("§eGenerating walls...");
		int rad = 200;
		EditSession es = new EditSession(new BukkitWorld(w), Integer.MAX_VALUE);

		Region r = new CuboidRegion(new Vector(-rad, 0, -rad), new Vector(rad, 256, rad));
		try {
			es.makeCuboidWalls(r, new SingleBlockPattern(new BaseBlock(7)));
			Bukkit.broadcastMessage("§bWalls Generated");
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
			Bukkit.broadcastMessage("§cERROR GENERATING WALLS: " + e.getMessage());
		}

		Bukkit.broadcastMessage("§eLoading schematics...");
		Location lobby = new Location(w, 0, 32, 0);
		loadSchematic("uhcKitSpawn", lobby);

		Location ench = new Location(w, 0, w.getHighestBlockYAt(0, 0), 0);
		loadSchematic("uhcKitEnchanting", ench);

		Bukkit.broadcastMessage("§eSetting spawn point...");
		w.setSpawnLocation(0, 32, 0);

		Bukkit.broadcastMessage("§a§lDone!");
	}

	public static void loadSchematic(String name, Location l) {
		File f = new File(ins.getDataFolder(), name + ".schematic");

		if (!f.exists()) {
			Bukkit.broadcastMessage("§cSchematic not found: " + name + ".schematic");
			return;
		}

		SchematicFormat schematic = SchematicFormat.getFormat(f);

		EditSession session = new EditSession(new BukkitWorld(l.getWorld()), Integer.MAX_VALUE);
		try {
			CuboidClipboard clipboard = schematic.load(f);
			clipboard.paste(session, BukkitUtil.toVector(l), false);
			session.flushQueue();
		} catch (Exception e) {
			Bukkit.broadcastMessage("§cError while loading schem: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// public static boolean isHoloEnabled(String pl) {
	// return SQLTable.Settings.getInt("Username", pl, "Enable_Holo") == 1;
	// }

	public static boolean isUHCEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("MCShockwaveUHC");
	}

}
