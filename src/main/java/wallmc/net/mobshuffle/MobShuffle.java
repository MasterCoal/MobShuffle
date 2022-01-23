package wallmc.net.mobshuffle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MobShuffle extends JavaPlugin implements CommandExecutor {

    public List<Entity> entityList = new ArrayList<>();
    public List<EntityType> AllMobs;
    public int runnableTaskID = 0;
    public List<EntityType> voidEntities = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage( ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "MobShuffle" + ChatColor.DARK_GRAY + "]" + ChatColor.GREEN + " Enabled!");
        this.getCommand("mobshuffle").setExecutor(this);
        this.getCommand("mobshuffleforce").setExecutor(this);
        AllMobs = Arrays.asList(Arrays.stream(EntityType.values()).toArray(EntityType[]::new));
        voidEntities.add(EntityType.FISHING_HOOK);
        voidEntities.add(EntityType.AREA_EFFECT_CLOUD);
        voidEntities.add(EntityType.PLAYER);
        voidEntities.add(EntityType.SPLASH_POTION);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void startShuffleTimer() {

        int time = 1200 + (int)(Math.random() * ((1200 - 6000) + 1));

        BukkitTask runnable = new BukkitRunnable() {

            @Override
            public void run() {
                shuffle();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.GREEN + "Shuffled!");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 100, 100);
                }

            }
        }.runTaskLater(this, time);

        runnableTaskID = runnable.getTaskId();

    }

    public void shuffle() {

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location l = p.getLocation();
            for (int chX = -1; chX <= 1; chX++) {
                for (int chZ = -1; chZ <= 1; chZ++) {
                    int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                    for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk().getEntities()) {
                        if (e.getLocation().distance(l) <= 20 && e.getLocation().getBlock() != l.getBlock() && (getDistance(l.getY(), e.getLocation().getY())) < 10) {
                            if (entityList!= null && !entityList.contains(e) && !(e instanceof Player)) {
                                if (e.getCustomName() == null) {
                                    entityList.add(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Entity e : entityList) {
            Location l = e.getLocation();
            e.remove();

            EntityType random = AllMobs.get((int)(Math.random() * AllMobs.size()));

            if (voidEntities.contains(random)) {
                continue;
            }

            if (l != null && random.getEntityClass() != null) {
                l.getWorld().spawnEntity(l, random);

            }
        }

        entityList.clear();

        startShuffleTimer();
    }


    public static double getDistance(double int1, double in2) {
        double integer = int1 - in2;

        if (integer < 0) {
            integer = integer*-1;
        }
        return integer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("mobshuffle")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("mobshuffle.perms")) {
                    if (runnableTaskID == 0) {
                        player.sendMessage(ChatColor.GREEN + "Mob Shuffle has started!");
                        startShuffleTimer();
                    } else {
                        Bukkit.getScheduler().cancelTask(runnableTaskID);
                        runnableTaskID = 0;
                        player.sendMessage(ChatColor.RED + "Mob Shuffle has stopped!");
                    }
                }
            }
        } else if (command.getName().equals("mobshuffleforce")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("mobshuffle.perms")) {
                    player.sendMessage(ChatColor.GREEN + "Mob Shuffle has started!");
                    shuffle();
                }
            }
        }
        return true;
    }
}
