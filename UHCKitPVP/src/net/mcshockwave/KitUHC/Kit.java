package net.mcshockwave.KitUHC;

import net.mcshockwave.KitUHC.Menu.ItemMenu;
import net.mcshockwave.KitUHC.Menu.ItemMenu.Button;
import net.mcshockwave.KitUHC.Menu.ItemMenu.ButtonRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Random;

public enum Kit {

	Default(
		"§b[Def]",
		Material.IRON_CHESTPLATE,
		1,
		0,
		Material.IRON_HELMET,
		Material.IRON_CHESTPLATE,
		Material.IRON_LEGGINGS,
		Material.IRON_BOOTS,
		i(Material.DIAMOND_SWORD),
		i(Material.BOW),
		i(Material.GOLDEN_APPLE, 3),
		i(Material.COOKED_BEEF, 64),
		i(Material.ARROW, 64)),
	Half_Heart_Warrior(
		"§c[1/2♥]",
		Material.POTION,
		1,
		8261,
		Material.IRON_HELMET,
		Material.IRON_CHESTPLATE,
		Material.IRON_LEGGINGS,
		Material.IRON_BOOTS,
		i(Material.DIAMOND_SWORD),
		i(Material.BOW),
		i(Material.COOKED_BEEF, 64),
		i(Material.ARROW, 64)),
	Sword_Rusher(
		"§a[SR]",
		Material.DIAMOND_SWORD,
		1,
		0,
		Material.IRON_HELMET,
		Material.IRON_CHESTPLATE,
		Material.IRON_LEGGINGS,
		Material.IRON_BOOTS,
		i(Material.DIAMOND_SWORD),
		i(Material.GOLDEN_APPLE, 5),
		i(Material.COOKED_BEEF, 64)),
	Half_Health_Tank(
		"§e[HHT]",
		Material.DIAMOND_CHESTPLATE,
		1,
		0,
		Material.IRON_HELMET,
		Material.DIAMOND_CHESTPLATE,
		Material.DIAMOND_LEGGINGS,
		Material.IRON_BOOTS,
		i(Material.DIAMOND_SWORD),
		i(Material.BOW),
		i(Material.GOLDEN_APPLE),
		i(Material.COOKED_BEEF, 64),
		i(Material.ARROW, 64)),
	Troll(
		"§3[Trl]",
		Material.STONE_SWORD,
		1,
		0,
		Material.AIR,
		Material.AIR,
		Material.AIR,
		Material.AIR,
		i(Material.STONE_SWORD),
		i(Material.BOW),
		i(Material.COOKED_BEEF, 64),
		i(Material.GOLDEN_APPLE, 5),
		i(Material.SNOW_BALL, 16),
		i(Material.COBBLESTONE, 64),
		i(Material.ARROW, 30));

	public Material		m;
	public String		name;
	public int			am;
	public short		data;
	public ItemStack[]	acontents;
	public ItemStack[]	give;

	public Team			team	= null;

	private Kit(String scpre, Material ico, int am, int da, Material h, Material c, Material l, Material b,
			ItemStack... kit) {
		this.m = ico;
		this.name = name().replace('_', ' ');
		this.am = am;
		this.data = (short) da;
		acontents = new ItemStack[] { i(b), i(l), i(c), i(h) };
		give = kit;

		Scoreboard s = Bukkit.getScoreboardManager().getMainScoreboard();
		Team te = s.getTeam(shorten(name(), 10));
		if (te != null) {
			te.unregister();
		}
		if (!KitUHC.isUHCEnabled()) {
			Team t = s.registerNewTeam(shorten(name(), 10));
			t.setPrefix(scpre + "§r ");
			t.setSuffix("§r");
			t.setAllowFriendlyFire(true);
			t.setCanSeeFriendlyInvisibles(false);
			team = t;
		}
	}

	public static Kit getCurrentKit() {
		return values()[KitUHC.currentid];
	}

	public static void cycle(World kitworld) {
		KitUHC.currentid = rand.nextInt(values().length);

		Kit cur = getCurrentKit();

		for (Player p : kitworld.getPlayers()) {
			if (KitUHC.isInArena(p.getLocation())) {
				cur.onSelect(p, false);
			}
			p.sendMessage("§aKit cycled to: §6" + cur.name);
		}
	}

	private static ItemStack i(Material m) {
		return new ItemStack(m);
	}

	private static ItemStack i(Material m, int amount) {
		return new ItemStack(m, amount);
	}

	@SuppressWarnings("deprecation")
	public void onSelect(Player p, boolean enter) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(acontents);
		for (ItemStack it : give) {
			if (it.getType() == Material.ARROW) {
				p.getInventory().setItem(17, it);
			} else if (it.getType() == Material.GOLDEN_APPLE) {
				p.getInventory().setItem(7, it);
			} else {
				p.getInventory().addItem(it);
			}
		}
		p.getInventory().setItem(8, new ItemStack(Material.STONE_PICKAXE));
		if (enter) {
			p.teleport(getSpawn(p.getWorld()));
			if (team != null) {
				team.addPlayer(p);
				p.setPlayerListName(shorten(p.getName(), 11) + "§r");
			}
		}

		double health = 20;
		double maxhealth = 20;
		if (this == Half_Heart_Warrior) {
			health = 1;
			maxhealth = 6;
		}
		if (this == Half_Health_Tank) {
			health = 9;
			maxhealth = 16;
		}
		if (p.getHealth() > health) {
			p.setHealth(health);
		}
		p.setMaxHealth(maxhealth);
		KitUHC.updateHealth(p);

		p.updateInventory();
	}

	public static String shorten(String s, int len) {
		if (s.length() > len) {
			return s.substring(0, len);
		}
		return s;
	}

	public static ItemMenu getMenu(Player p) {
		ItemMenu m = new ItemMenu("Kits", values().length);

		for (int i = 0; i < values().length; i++) {
			final Kit k = values()[i];
			Button b = new Button(true, k.m, k.am, k.data, k.name, "", "Click to use");
			b.setOnClick(new ButtonRunnable() {
				public void run(Player p, InventoryClickEvent event) {
					k.onSelect(p, true);
				}
			});
			m.addButton(b, i);
		}

		return m;
	}

	public static int				radius		= 180;
	public static int				spawnRadius	= 20;
	public static Random			rand		= new Random();

	public static final Material[]	nospawn		= { Material.STATIONARY_WATER, Material.WATER,
			Material.STATIONARY_LAVA, Material.LAVA, Material.STONE, Material.GRAVEL, Material.BEDROCK,
			Material.COAL_ORE, Material.IRON_ORE };

	public static Location getSpawn(World w) {
		boolean done = false;
		Location fin = w.getSpawnLocation();
		while (!done) {
			Location l = w.getSpawnLocation();

			l.add(rand.nextInt(radius * 2) - radius, 0, rand.nextInt(radius * 2) - radius);
			l.add(l.getX() > 0 ? spawnRadius : -spawnRadius, 0, l.getZ() > 0 ? spawnRadius : -spawnRadius);
			l.setY(w.getHighestBlockYAt(l.getBlockX(), l.getBlockZ()) + 2);

			if (!Arrays.asList(nospawn).contains(w.getHighestBlockAt(l).getRelative(0, -1, 0).getType())) {
				done = true;
				fin = l;
			}
		}

		return fin;
	}

}
