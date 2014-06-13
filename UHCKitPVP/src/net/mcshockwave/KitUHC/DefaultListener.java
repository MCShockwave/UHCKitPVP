package net.mcshockwave.KitUHC;

import net.mcshockwave.KitUHC.Utils.ItemMetaUtils;
import net.mcshockwave.UHC.UltraHC;
import net.mcshockwave.UHC.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Random;

public class DefaultListener implements Listener {

	public HashMap<Player, Integer>	ks	= new HashMap<>();

	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		// Entity e = event.getEntity();
		if (KitUHC.isUHCEnabled()) {
			return;
		}
		if (event.getRegainReason() == RegainReason.SATIATED) {
			event.setCancelled(true);
		}
		if (event.getEntity() instanceof Player) {
			KitUHC.updateHealth((Player) event.getEntity());
		}
		// if (e instanceof Player && event.getAmount() < 100) {
		// final Player p = (Player) e;
		// final double health = p.getHealth();
		// Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
		// public void run() {
		// double healthEnd = p.getHealth();
		// double regain = healthEnd - health;
		// regain = (double) Math.round(regain * 10) / 10;
		// if (regain <= 0) {
		// return;
		// }
		// Hologram h = HoloAPI.getManager().createSimpleHologram(
		// LocUtils.addRand(p.getLocation().clone().add(0.5, 1, 0.5), 1, 0, 1),
		// 1, true,
		// "§a§l+" + (regain * 5) + "%");
		// for (Player p2 : Bukkit.getOnlinePlayers()) {
		// if (!KitUHC.isHoloEnabled(p2.getName())) {
		// h.clear(p2);
		// }
		// }
		// }
		// }, 1l);
		// }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		final Player p = event.getPlayer();

		if (p.isOp() && !KitUHC.isUHCEnabled()) {
			event.setFormat("§c[§lOP§c]§r " + event.getFormat());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		ItemStack it = event.getItem();

		if (b != null) {
			if (b.getType() == Material.WALL_SIGN) {
				Sign s = (Sign) b.getState();
				boolean cont = false;
				for (int i = 0; i < 3; i++) {
					if (s.getLine(i).toLowerCase().contains("[kit]")) {
						cont = true;
					}
				}
				if (cont) {
					if (KitUHC.enabled) {
						// Sign s = (Sign) b.getState();
						// if (s.getLine(0).equalsIgnoreCase("[Kit]")) {
						// Kit.getMenu(p).open(p);
						// }
						Kit.getCurrentKit().onSelect(p, true);
					} else {
						p.sendMessage("§cThe PVP arena is disabled!");
					}
				}
			}
		}

		if (p.getGameMode() == GameMode.CREATIVE || canUse(it) || b != null) {
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();

		if (KitUHC.isUHCEnabled() && b.getWorld() != Multiworld.getKit()) {
			return;
		}

		if (p.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
			return;
		}

		if (b.getLocation().distanceSquared(b.getWorld().getSpawnLocation()) < 18 * 18
				|| b.getType() == Material.COBBLESTONE) {
			event.setCancelled(true);
			return;
		}

		if (CustomBlock.getBlockAt(b.getLocation()) == null) {
			CustomBlock cb = new CustomBlock(b, p.getName(), false);
			cb.setTime(60);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();

		if (KitUHC.isUHCEnabled() && b.getWorld() != Multiworld.getKit()) {
			return;
		}

		if (p.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
			return;
		}

		if (b.getLocation().distanceSquared(b.getWorld().getSpawnLocation()) < 18 * 18) {
			event.setCancelled(true);
		}

		if (CustomBlock.getBlockAt(b.getLocation()) == null) {
			CustomBlock cb = new CustomBlock(b, p.getName(), true);
			cb.setTime(60);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (!KitUHC.isUHCEnabled()) {
			p.teleport(p.getWorld().getSpawnLocation());
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setHealth(20);
			KitUHC.updateHealth(p);
			p.setLevel(0);
		}

		// if (!SQLTable.Stats.has("Username", p.getName())) {
		// SQLTable.Stats.add("Username", p.getName());
		// }
		// if (!SQLTable.Settings.has("Username", p.getName())) {
		// SQLTable.Settings.add("Username", p.getName());
		// }
	}

	public boolean canUse(ItemStack it) {
		if (it != null) {
			if (it.getType() == Material.BOW || it.getType() == Material.GOLDEN_APPLE
					|| it.getType() == Material.COOKED_BEEF || it.getType() == Material.SNOW_BALL
					|| it.getType() == Material.STONE_PICKAXE) {
				return true;
			}
		}
		return false;
	}

	Random	rand	= new Random();

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player p = event.getEntity();
		Location l = event.getEntity().getLocation();

		if (KitUHC.isUHCEnabled() && l.getWorld() != Multiworld.getKit()) {
			return;
		}
		for (ItemStack it : event.getDrops()) {
			if (it.getType() == Material.GOLDEN_APPLE) {
				continue;
			}
			final Item i = l.getWorld().dropItemNaturally(l, it);
			i.setPickupDelay(Integer.MAX_VALUE);
			Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
				public void run() {
					i.remove();
				}
			}, 60l);
		}
		event.getDrops().clear();
		event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, rand.nextInt(2)));
		event.getDrops().add(
				ItemMetaUtils.setLore(
						ItemMetaUtils.setItemName(new ItemStack(Material.GOLDEN_APPLE, 1), "§6Golden Head"),
						"§eObtained from killing:", "§e§l" + p.getName()));

		// int de = SQLTable.Stats.getInt("Username", p.getName(), "Deaths");
		// de++;
		// SQLTable.Stats.set("Deaths", de + "", "Username", p.getName());
		if (p.getKiller() != null) {
			final Player k = p.getKiller();
			// int ki = SQLTable.Stats.getInt("Username", k.getName(), "Kills");
			// ki++;
			// SQLTable.Stats.set("Kills", ki + "", "Username", k.getName());

			if (!ks.containsKey(k)) {
				ks.put(k, 0);
			}
			int kis = ks.get(k);
			kis++;
			ks.remove(k);
			ks.put(k, kis);
			final int kisf = kis;

			if (kis >= 5 && kis % 5 == 0 || kis == 3) {
				Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
					public void run() {
						Bukkit.broadcastMessage("§d" + k.getName() + " is on a " + kisf + " kill streak!");
						p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 25, 1);
					}
				}, 5);
			}
			// Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
			// public void run() {
			// int high = SQLTable.Stats.getInt("Username", k.getName(),
			// "Highest_KS");
			// if (kisf > high) {
			// SQLTable.Stats.set("Highest_KS", kisf + "", "Username",
			// k.getName());
			// for (Player p2 : Bukkit.getOnlinePlayers()) {
			// if (p2 == k) {
			// p2.sendMessage("§aYou have a new highest kill streak of " + kisf
			// + "!");
			// } else {
			// p2.sendMessage("§a" + k.getName()
			// + " has earned a new highest personal kill streak of " + kisf +
			// "!");
			// }
			// }
			// }
			// }
			// }, 1l);
			// p.getWorld().strikeLightningEffect(p.getLocation());
			k.setLevel(k.getLevel() + 1);

			// final Hologram dh = new
			// HologramFactory(KitUHC.ins).withText("§c§lR.I.P.",
			// event.getDeathMessage())
			// .withLocation(p.getEyeLocation()).build();
			// for (Player p2 : Bukkit.getOnlinePlayers()) {
			// if (!KitUHC.isHoloEnabled(p2.getName())) {
			// dh.clear(p2);
			// }
			// }
			// Bukkit.getScheduler().runTaskLater(KitUHC.ins, new Runnable() {
			// public void run() {
			// dh.clearAllPlayerViews();
			// HoloAPI.getManager().stopTracking(dh);
			// HoloAPI.getManager().clearFromFile(dh);
			// }
			// }, 100l);
		}

		if (ks.containsKey(p)) {
			p.sendMessage("§bYou had " + ks.get(p) + " kills that life");
		}
		ks.remove(p);
		ks.put(p, 0);

		event.setDroppedExp(0);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(event.getPlayer().getWorld().getSpawnLocation());
		event.getPlayer().getInventory().clear();
		event.getPlayer().getInventory().setArmorContents(null);
		event.getPlayer().setMaxHealth(20);
		KitUHC.updateHealth(event.getPlayer());
		if (!KitUHC.isUHCEnabled()) {
			Team t = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(event.getPlayer());
			if (t != null) {
				t.removePlayer(event.getPlayer());
			}
		}
		event.getPlayer().setLevel(0);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if (KitUHC.isUHCEnabled() && Multiworld.getKit() != e.getWorld()) {
			return;
		}

		if (!KitUHC.isInArena(e.getLocation())) {
			event.setCancelled(true);
		}

		if (e instanceof Player) {
			final Player p = (Player) e;

			KitUHC.updateHealth(p);

			// final double health = p.getHealth();
			// Bukkit.getScheduler().runTaskLater(UltraHC.ins, new Runnable() {
			// public void run() {
			// double healthEnd = p.getHealth();
			// double damage = health - healthEnd;
			// damage = (double) Math.round(damage * 10) / 10;
			// if (damage <= 0) {
			// return;
			// }
			// Hologram h = HoloAPI.getManager().createSimpleHologram(
			// LocUtils.addRand(p.getLocation().clone().add(0.5, 1, 0.5), 1, 0,
			// 1), 1, true,
			// "§c§l-" + (damage * 5) + "%");
			// for (Player p2 : Bukkit.getOnlinePlayers()) {
			// if (!KitUHC.isHoloEnabled(p2.getName())) {
			// h.clear(p2);
			// }
			// }
			// }
			// }, 1l);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (KitUHC.isUHCEnabled()) {
			return;
		}

		final Player p = event.getPlayer();
		final ItemStack it = event.getItem();
		if (it.getType() == Material.GOLDEN_APPLE && !KitUHC.isUHCEnabled()) {
			Bukkit.getScheduler().runTask(UltraHC.ins, new Runnable() {
				public void run() {
					p.removePotionEffect(PotionEffectType.ABSORPTION);

					if (ItemMetaUtils.hasCustomName(it)
							&& ItemMetaUtils.getItemName(it).equalsIgnoreCase("§6Golden Head")) {
						p.removePotionEffect(PotionEffectType.REGENERATION);
						p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 180, 1));
					}
				}
			});
		}
	}

	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		if (KitUHC.isUHCEnabled()) {
			return;
		}
		event.setMotd("§cMCShockwave §7UHC §8- §a[Open PvP]");
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String mes = event.getMessage();

		if (mes.startsWith("/kill") && !p.isOp()) {
			event.setCancelled(true);
			p.sendMessage("No.");
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if ((de instanceof Player || de instanceof Projectile) && ee instanceof Player) {
			Player p = (Player) ee;
			Player d;
			if (de instanceof Projectile) {
				d = (Player) ((Projectile) de).getShooter();
			} else
				d = (Player) de;

			// int cr = SQLTable.Stats.getInt("Username", d.getName(),
			// "Credits");
			// cr += event.getDamage();
			// SQLTable.Stats.set("Credits", cr + "", "Username", d.getName());

			if ((KitUHC.isUHCEnabled() && !UltraHC.started || !KitUHC.isUHCEnabled())
					&& d.getLocation().distanceSquared(p.getLocation()) >= 50 * 50) {
				double dis = getRoundedDistance(p.getLocation(), d.getLocation(), 2);

				Bukkit.broadcastMessage("§e" + d.getName() + " §csniped §e" + p.getName() + " §a(" + dis + " blocks)");
			}
		}
	}

	public double getRoundedDistance(Location l, Location l2, int places) {
		double dis = l.distance(l2);

		dis = Math.round(dis * Math.pow(10, places));
		dis /= Math.pow(10, places);

		return dis;
	}

	public final int	lim	= 12;

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		Item i = event.getItem();
		ItemStack it = i.getItemStack();

		if (KitUHC.isUHCEnabled() && p.getWorld() != Multiworld.getKit()) {
			return;
		}

		if (it.getType() == Material.GOLDEN_APPLE) {
			int am = it.getAmount();
			int ha = getAmount(p.getInventory(), Material.GOLDEN_APPLE);

			int gi = (am + ha >= lim) ? (am - ha) : (am);

			event.setCancelled(true);
			i.remove();
			if (gi > 0) {
				it.setAmount(gi);
				p.getInventory().addItem(it);
				return;
			}
		}
	}

	public int getAmount(Inventory i, Material m) {
		int tot = 0;
		for (ItemStack it : i.getContents()) {
			if (it != null && it.getType() == m) {
				tot += it.getAmount();
			}
		}
		return tot;
	}

}
