package me.greenem.DeathSwap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class MainFile extends JavaPlugin implements Listener {
	
	Logger log = getLogger();
	final Random random = new Random(System.currentTimeMillis());
	
	public File invsSaves = new File("C:\\Users\\andrey\\Desktop\\PluginTests\\files\\invsaves");
	public volatile int gameNumber = 0;
	public ArrayList<Player> players = new ArrayList<>(); 
	
	@Override
	public void onDisable() {
		log.info(ChatColor.RED + "GeneralTools plugin has been disabled.");
	}

	@Override
	public void onEnable() {
		log.info(ChatColor.GREEN + "DeathSwap plugin has been enabled!");
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player p = getServer().getPlayer(sender.getName());
		if(cmd.getName().equalsIgnoreCase("dsjoin")){
			if(!players.contains(p)) {
				players.add(p);
				p.sendMessage(ChatColor.GOLD + "Now you participate in DeathSwap.");
				for (Player player : players) {
					if(!player.equals(p)) player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + p.getName() + "" + ChatColor.GREEN + "" + ChatColor.BOLD + " joined DeathSwap team!");
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "You already participate in DeathSwap!");
			}
		}
		else if(cmd.getName().equalsIgnoreCase("dsleave")){
			if(players.contains(p)) {
				players.remove(p);
				p.sendMessage(ChatColor.GOLD + "Now you don't participate in DeathSwap anymore.");
				for (Player player : players) {
					if(!player.equals(p)) player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + p.getName() + "" + ChatColor.RED + "" + ChatColor.BOLD + " left DeathSwap team.");
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "You already DON'T participate in DeathSwap!");
			}
		}
		else if(cmd.getName().equalsIgnoreCase("dsstart")){
			DsStart(600, 10); //600 (5 minutes)
		}
		else if(cmd.getName().equalsIgnoreCase("dsstop")){
			DsStop();
		}
		return false;
	}

	public void AddItemStack(Player p, ItemStack ItemStack, int slot) {
		if(!p.getInventory().contains(ItemStack)) {
			if(p.getInventory().getItem(slot) == null) p.getInventory().setItem(slot, ItemStack);
			else p.getInventory().addItem(ItemStack);
		}
	}

	public void RemoveMaterial(Player p, Material material) {
		if(p.getInventory().contains(material)) {
			p.getInventory().remove(material);
		}
	}

	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}

	public void saveInventory (Player p) throws IOException {
		YamlConfiguration c = new YamlConfiguration();
		c.set("inventory.armor", p.getInventory().getArmorContents());
		c.set("inventory.content", p.getInventory().getContents());
		c.save(new File(invsSaves, p.getName()+".yml"));
	}

	private void loadInventory(Player p) throws IOException {
		//File file = new File(inventoriesDirectory, p.getName()+".yml");
		YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(invsSaves, p.getName()+".yml"));
		ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
		p.getInventory().setArmorContents(content);
		content = ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[0]);
		p.getInventory().setContents(content);
	}
	
	public void DsStart(int seconds, int delay) {
		for (Player p : players) {
			p.sendMessage(ChatColor.GREEN + "The DeathSwap mode has been started! All of you will switch places in " + seconds/3600 + ":" + seconds/60 + ":" + seconds %60 + "!");
			p.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Start!", ChatColor.GRAY + "Prepare for the swap in " + seconds/3600 + ":" + seconds/60 + ":" + seconds %60);
		}
		for (int i = delay; i>0; i--) {
			log.info(i+"");
			final int iCopy = i;
			new BukkitRunnable() {
				int savedGameNumber = gameNumber;
				@Override
	    		public void run() {
					if(gameNumber!=savedGameNumber) {
	    				// log.info("breacking delay runnable (game canceled)");
	    				cancel();
	    				return;
	    			}
	    			if(players.size()<2) return;
	    			for (Player p : players) {
	    				p.sendMessage(ChatColor.RED + "Teleporting in " + iCopy + " seconds!");
	    			}
				}
			}.runTaskTimer(this, (seconds-i)*20, seconds*20);
		}
		new BukkitRunnable() {
			int savedGameNumber = gameNumber;
    		@Override
    		public void run() {
    			if(gameNumber!=savedGameNumber) {
    				log.info("breacking runnable (game canceled)");
    				cancel();
    				return;
    			}
    			if(players.size()<2) return;
    			Player firstPlayer = players.get(0);
    			ArrayList<Player> playersCopy = (ArrayList<Player>) players.clone();
    			Map <Player, Location> References = new HashMap();
    			ArrayList<Player> playersToChoose = (ArrayList<Player>) playersCopy.clone();
    			for (Player p : playersCopy) {
    				if(playersToChoose.size()==players.size()) {
    					firstPlayer = p;
    				}
    				log.info("playersCopy: " + playersCopy.toString());
    				playersToChoose.remove(p);
    				log.info("playersToChoose: " + playersToChoose.toString());
    				if(playersToChoose.size()>0) {
    					Player Destination = playersToChoose.get(random.nextInt(playersToChoose.size()));
    					log.info("Destination: " + Destination.toString());
    					Location loc = Destination.getLocation();
    					References.put(p, new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()));
    					log.info("References: " + References.toString());
    				}
    				else {
    					log.info("Last player to first player!");
    					Player Destination = firstPlayer;
    					log.info("Destination: " + Destination.toString());
    					Location loc = Destination.getLocation();
    					References.put(p, new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()));
    					log.info("References: " + References.toString());
    				}
    			}
    			//log.info(References.toString());
    			//log.info(playersCopy.toString());
    			for (Player p : playersCopy) {
    				p.sendMessage(ChatColor.RED + "Teleporting...");
    				for (Player player : playersCopy) {
    					p.hidePlayer(player);
    				}
    			}
    			for (Player p : playersCopy) {
    				Location loc = References.get(p);
    				loc.setPitch(p.getLocation().getPitch());
    				loc.setYaw(p.getLocation().getYaw());
    				p.teleport(loc);
    			}
    			for (Player p : playersCopy) {
    				for (Player player : playersCopy) {
    					p.showPlayer(player);
    				}
    			}
    		}
    	}.runTaskTimer(this, seconds*20, seconds*20);
	}
	
	public void DsStop() {
		gameNumber++;
		for (Player p : players) {
			p.sendMessage(ChatColor.GREEN + "The DeathSwap mode has been stopped");
		}
	}
}