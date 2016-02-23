 package eu.xoola.HeartDrops;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 
 public class DropInfo
 {
   private EntityType entityType;
   private float dropChance;
   private int minDropAmount;
   private int maxDropAmount;
   private int maxStackSize;
   
   public DropInfo(String configList)
   {
     String[] data = configList.split(" : ");
     if (data.length == 5)
     {
       this.entityType = EntityType.valueOf(data[0]);
       this.dropChance = Float.parseFloat(data[1]);
       this.minDropAmount = Integer.parseInt(data[2]);
       this.maxDropAmount = Integer.parseInt(data[3]);
       this.maxStackSize = Integer.parseInt(data[4]);
     }
     else
     {
       HeartDrops.instance.getLogger().severe(
         "failed to load the configuration. Please check your config.yml");
     }
   }
   
   public void onMobDeath(LivingEntity e)
   {
     int dropAmount = this.minDropAmount;
     for (int i = dropAmount; i < this.maxDropAmount; i++)
     {
       dropAmount += (HeartDrops.instance.rand.nextFloat() <= this.dropChance ? 1 : 0);
     }
     
     if (dropAmount <= 0)
     {
       return;
     }
     
     while (dropAmount > this.maxStackSize)
     {
       Item item = e.getWorld().dropItemNaturally(e.getLocation(), new ItemStack(HeartDrops.instance.heartMaterial, this.maxStackSize));
       HeartDrops.instance.dropTask.addDroppedItem(item);
       HeartDrops.instance.removeItem(item);
       dropAmount -= this.maxStackSize;
     }
     
     Item item = e.getWorld().dropItemNaturally(e.getLocation(), new ItemStack(HeartDrops.instance.heartMaterial, dropAmount));
     HeartDrops.instance.dropTask.addDroppedItem(item);
     HeartDrops.instance.removeItem(item);
   }
   
   public EntityType getEntityType()
   {
     return this.entityType;
   }
   
   public float getDropChance()
   {
     return this.dropChance;
   }
   
   public int getMinDropAmount()
   {
     return this.minDropAmount;
   }
   
   public int getMaxDropAmount()
   {
     return this.maxDropAmount;
   }
 }