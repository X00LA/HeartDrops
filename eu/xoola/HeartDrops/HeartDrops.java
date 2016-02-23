 package eu.xoola.HeartDrops;
 import com.comphenix.protocol.PacketType;
 import com.comphenix.protocol.ProtocolLibrary;
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.PacketContainer;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Random;
 import net.md_5.bungee.api.ChatColor;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HeartDrops extends JavaPlugin implements Listener
 {
   public Material heartMaterial;
   private int particleTime;
   private Sound pickUpSound;
   private float pickUpVolume;
   private float pickUpPitch;
   private int healAmount;
   private float doubleHealChance;
   private HashMap<org.bukkit.entity.EntityType, DropInfo> entityInfo;
   public DropTask dropTask;
   public Random rand;
   public ProtocolManager protocolManager;
   public static HeartDrops instance;
   
   public void onEnable()
   {
     instance = this;
     saveDefaultConfig();
     getServer().getPluginManager().registerEvents(this, this);
     
     loadConfig();
     
     this.rand = new Random();
     this.dropTask = new DropTask();
     Bukkit.getScheduler().scheduleSyncRepeatingTask(
       this, this.dropTask, this.particleTime, this.particleTime);
   }
   
 
   public void onDisable()
   {
     Bukkit.getScheduler().cancelTasks(this);
     if (this.protocolManager != null)
     {
       this.protocolManager.removePacketListeners(this);
     }
   }
   
   private void loadConfig()
   {
     this.heartMaterial = Material.valueOf(getConfig().getString("material"));
     this.particleTime = getConfig().getInt("particleTime");
     this.pickUpSound = Sound.valueOf(getConfig().getString("pickupsound"));
     this.pickUpVolume = ((float)getConfig().getDouble("pickupvolume"));
     this.pickUpPitch = ((float)getConfig().getDouble("pickuppitch"));
     this.healAmount = getConfig().getInt("healAmount");
     this.doubleHealChance = ((float)getConfig().getDouble("doubleHealChance"));
     
     if (this.entityInfo == null)
     {
       this.entityInfo = new HashMap<EntityType, DropInfo>();
     }
     else
     {
       this.entityInfo.clear();
     }
     
     java.util.List<String> entityList = getConfig().getStringList("entities");
     for (String value : entityList)
     {
       DropInfo dropInfo = new DropInfo(value);
       if (dropInfo.getEntityType() != null)
       {
         this.entityInfo.put(dropInfo.getEntityType(), dropInfo);
       }
     }
     
     if (this.dropTask != null)
     {
       for (Item item : this.dropTask.droppedItems)
       {
         item.remove();
       }
       
       this.dropTask.droppedItems.clear();
       
       Bukkit.getScheduler().cancelAllTasks();
     }
     else
     {
       this.dropTask = new DropTask();
     }
     
     Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.dropTask, this.particleTime, this.particleTime);
   }
   
   private void dropHearts(LivingEntity e)
   {
     ((DropInfo)this.entityInfo.get(e.getType())).onMobDeath(e);
   }
   
   public void removeItem(Item item)
   {
     PacketContainer removeItem = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
     removeItem.getIntegerArrays().write(0, new int[] { item.getEntityId() });
     
     try
     {
       for (Player player : Bukkit.getServer().getOnlinePlayers())
       {
         ProtocolLibrary.getProtocolManager().sendServerPacket(player, removeItem);
       }
     }
     catch (InvocationTargetException e)
     {
       e.printStackTrace();
     }
   }
   
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
   {
     if ((cmd.getName().equalsIgnoreCase("heartdrops")) && (sender.hasPermission("heartdrops.admin")))
     {
       if (args.length == 0)
       {
         sender.sendMessage(ChatColor.BLUE + "/heartdrops reload" + ChatColor.GREEN + " - Reloads the configuration");
       }
       else if ((args.length == 1) && (args[0].equals("reload")))
       {
         loadConfig();
         sender.sendMessage(ChatColor.GREEN + "Reloaded configuration");
       }
       
 
       return true;
     }
     
    return false;
   }
   
   @EventHandler(priority=EventPriority.LOWEST)
   public void onEntityDeath(EntityDeathEvent e)
   {
     if ((e.getDroppedExp() > 0) && (this.entityInfo.containsKey(e.getEntityType())))
     {
       dropHearts(e.getEntity());
     }
   }
   
   @EventHandler(priority=EventPriority.LOWEST)
   public void onItemPickUp(PlayerPickupItemEvent e)
   {
     if ((e.getItem().getItemStack().getType() == this.heartMaterial) && (e.getPlayer().hasPermission("heartdrops.pickup")))
     {
       Player player = e.getPlayer();
       if (this.dropTask.droppedItems.contains(e.getItem()))
       {
         e.setCancelled(true);
         
         if (player.getHealth() < player.getMaxHealth())
         {
           int amountToHeal = e.getItem().getItemStack().getAmount() * this.healAmount;
           if (this.rand.nextFloat() <= this.doubleHealChance)
           {
             this.healAmount *= 2;
           }
           
           if (player.getHealth() + amountToHeal > player.getMaxHealth())
           {
             player.setHealth(player.getMaxHealth());
           }
           else
           {
             player.setHealth(e.getPlayer().getHealth() + amountToHeal);
           }
           
           player.playSound(player.getLocation(), this.pickUpSound, this.pickUpVolume, this.pickUpPitch);
           
           this.dropTask.removeDroppedItem(e.getItem());
           e.getItem().remove();
         }
       }
     }
   }
 }
