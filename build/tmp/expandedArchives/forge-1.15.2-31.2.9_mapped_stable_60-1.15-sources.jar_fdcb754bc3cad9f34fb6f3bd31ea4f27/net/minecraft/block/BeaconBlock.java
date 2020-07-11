package net.minecraft.block;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BeaconBlock extends ContainerBlock implements IBeaconBeamColorProvider {
   public BeaconBlock(Block.Properties properties) {
      super(properties);
   }

   public DyeColor getColor() {
      return DyeColor.WHITE;
   }

   public TileEntity createNewTileEntity(IBlockReader worldIn) {
      return new BeaconTileEntity();
   }

   public ActionResultType func_225533_a_(BlockState p_225533_1_, World p_225533_2_, BlockPos p_225533_3_, PlayerEntity p_225533_4_, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
      if (p_225533_2_.isRemote) {
         return ActionResultType.SUCCESS;
      } else {
         TileEntity tileentity = p_225533_2_.getTileEntity(p_225533_3_);
         if (tileentity instanceof BeaconTileEntity) {
            p_225533_4_.openContainer((BeaconTileEntity)tileentity);
            p_225533_4_.addStat(Stats.INTERACT_WITH_BEACON);
         }

         return ActionResultType.SUCCESS;
      }
   }

   public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return false;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (stack.hasDisplayName()) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof BeaconTileEntity) {
            ((BeaconTileEntity)tileentity).setCustomName(stack.getDisplayName());
         }
      }

   }
}