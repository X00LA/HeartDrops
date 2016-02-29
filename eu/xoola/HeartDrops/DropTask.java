 package eu.xoola.HeartDrops;
 
 import java.util.ArrayList;
 import org.bukkit.entity.Item;
 
 public class DropTask implements Runnable
 {
   private ArrayList<Item> droppedItems;
   
   public DropTask()
   {
     this.droppedItems = new ArrayList<Item>();
   }
   
   public void addDroppedItem(Item item)
   {
     this.droppedItems.add(item);
   }
   
   public void removeDroppedItem(Item item)
   {
     this.droppedItems.remove(item);
   }
   
   public ArrayList<Item> getDroppedItems()
   {
	   return this.droppedItems;
   }
   
   public void clearItems()
   {
	   this.droppedItems.clear();
   }
   
   public void run()
   {
     Item item = null;
     for (int i = 0; i < this.droppedItems.size(); i++)
     {
       item = (Item)this.droppedItems.get(i);
       if (item.isDead())
       {
         this.droppedItems.remove(i);
         i--;
 
       }
       else
       {
         item.getWorld().spigot().playEffect(item.getLocation(), org.bukkit.Effect.HEART);
       }
     }
   }
 }