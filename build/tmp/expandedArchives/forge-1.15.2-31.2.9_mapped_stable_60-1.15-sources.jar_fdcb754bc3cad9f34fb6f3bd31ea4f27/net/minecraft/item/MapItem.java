package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MapItem extends AbstractMapItem {
   public MapItem(Item.Properties builder) {
      super(builder);
   }

   public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
      ItemStack itemstack = FilledMapItem.setupNewMap(worldIn, MathHelper.floor(playerIn.func_226277_ct_()), MathHelper.floor(playerIn.func_226281_cx_()), (byte)0, true, false);
      ItemStack itemstack1 = playerIn.getHeldItem(handIn);
      if (!playerIn.abilities.isCreativeMode) {
         itemstack1.shrink(1);
      }

      if (itemstack1.isEmpty()) {
         return ActionResult.func_226248_a_(itemstack);
      } else {
         if (!playerIn.inventory.addItemStackToInventory(itemstack.copy())) {
            playerIn.dropItem(itemstack, false);
         }

         playerIn.addStat(Stats.ITEM_USED.get(this));
         return ActionResult.func_226248_a_(itemstack1);
      }
   }
}