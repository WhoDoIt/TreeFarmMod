package net.minecraft.item;

import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CarrotOnAStickItem extends Item {
   public CarrotOnAStickItem(Item.Properties builder) {
      super(builder);
   }

   public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
      ItemStack itemstack = playerIn.getHeldItem(handIn);
      if (worldIn.isRemote) {
         return ActionResult.func_226250_c_(itemstack);
      } else {
         if (playerIn.isPassenger() && playerIn.getRidingEntity() instanceof PigEntity) {
            PigEntity pigentity = (PigEntity)playerIn.getRidingEntity();
            if (itemstack.getMaxDamage() - itemstack.getDamage() >= 7 && pigentity.boost()) {
               itemstack.damageItem(7, playerIn, (p_219991_1_) -> {
                  p_219991_1_.sendBreakAnimation(handIn);
               });
               if (itemstack.isEmpty()) {
                  ItemStack itemstack1 = new ItemStack(Items.FISHING_ROD);
                  itemstack1.setTag(itemstack.getTag());
                  return ActionResult.func_226248_a_(itemstack1);
               }

               return ActionResult.func_226248_a_(itemstack);
            }
         }

         playerIn.addStat(Stats.ITEM_USED.get(this));
         return ActionResult.func_226250_c_(itemstack);
      }
   }
}