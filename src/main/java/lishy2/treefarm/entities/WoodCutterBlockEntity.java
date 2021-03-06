package lishy2.treefarm.entities;

import lishy2.treefarm.Treefarm;
import lishy2.treefarm.blocks.PlanterBlock;
import lishy2.treefarm.containers.WoodCutterContainer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static lishy2.treefarm.util.RegistryHandler.WOOD_CUTTER_BLOCK;


public class WoodCutterBlockEntity extends ItemHolderEntity implements ITickableTileEntity {

    private Tree cuttingTreeNow;
    private int cooldown;

    public WoodCutterBlockEntity() {
        super(TileEntityType.Builder.create(WoodCutterBlockEntity::new, WOOD_CUTTER_BLOCK.get()).build(null), (ItemStack it) -> it.getItem() instanceof AxeItem || it.getItem() instanceof ShearsItem, 2);
    }


    @Override
    public void tick() {
        ItemStack axe = entityContent.get(0), scissors = entityContent.get(1);
        if (world instanceof ServerWorld && !axe.isEmpty() && !scissors.isEmpty()) {
            ++cooldown;
            if (cooldown == 120 / axe.getDestroySpeed(Blocks.OAK_LOG.getDefaultState())) {
                BlockPos forwardPosition = getPos().add(this.getBlockState().get(PlanterBlock.HORIZONTAL_FACING).getDirectionVec());
                BlockPos backPosition = getPos().subtract(this.getBlockState().get(PlanterBlock.HORIZONTAL_FACING).getDirectionVec());
                if (cuttingTreeNow == null || cuttingTreeNow.isEmpty()) {
                    Treefarm.LOGGER.info("Tree is empty");
                    Treefarm.LOGGER.info("Finding new tree");
                    cuttingTreeNow = new Tree(forwardPosition, world);
                }
                if (!cuttingTreeNow.isEmpty()) {
                    ItemStack tool = world.getBlockState(cuttingTreeNow.peek()).getBlock() instanceof LogBlock ? axe : scissors;
                    Treefarm.LOGGER.info("Cutting block " + cuttingTreeNow.peek() + " by " + tool);
                    LootContext.Builder context = new LootContext.Builder((ServerWorld) world).withParameter(LootParameters.TOOL, tool).withParameter(LootParameters.POSITION, cuttingTreeNow.peek());
                    List<ItemStack> drop = world.getBlockState(cuttingTreeNow.peek()).getDrops(context);
                    Treefarm.LOGGER.info("Dropped " + drop);
                    world.destroyBlock(cuttingTreeNow.pop(), false);
                    for (ItemStack itemStack : drop) {
                        InventoryHelper.spawnItemStack(world, backPosition.getX() + 0.5, backPosition.getY() + 0.5, forwardPosition.getZ() + 0.5, itemStack);
                    }
                    tool.attemptDamageItem(1, world.rand, null);
                }
                cooldown = 0;
            }
        }
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.treefarm.wood_cutter_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new WoodCutterContainer(id, player, this);
    }
}

class Tree {
    private ArrayList<BlockPos> logs;
    private ArrayList<BlockPos> leaves;

    public Tree(BlockPos one, World w) {
        logs = new ArrayList<>();
        leaves = new ArrayList<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(one);
        boolean[][][] was = new boolean[20][60][20];
        while (!q.isEmpty() && logs.size() + leaves.size() < 100) {
            BlockPos v = q.pollFirst();
            Block vBlock = w.getBlockState(v).getBlock();
            if (vBlock instanceof LogBlock) {
                logs.add(v);
            } else if (vBlock instanceof LeavesBlock) {
                leaves.add(v);
            } else {
                continue;
            }
            was[v.getX() - one.getX() + 2][v.getY() - one.getY() + 2][v.getZ() - one.getZ() + 2] = true;
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        BlockPos newBlock = v.add(dx, dy, dz);
                        if (!was[newBlock.getX() - one.getX() + 10][newBlock.getY() - one.getY() + 3][newBlock.getZ() - one.getZ() + 10]) {
                            was[newBlock.getX() - one.getX() + 10][newBlock.getY() - one.getY() + 3][newBlock.getZ() - one.getZ() + 10] = true;
                            q.add(newBlock);
                        }
                    }
                }
            }
        }
    }


    BlockPos peek() {
        if (!leaves.isEmpty()) return leaves.get(leaves.size() - 1);
        if (!logs.isEmpty()) return logs.get(logs.size() - 1);
        return null;
    }

    BlockPos pop() {
        if (!leaves.isEmpty()) return leaves.remove(leaves.size() - 1);
        if (!logs.isEmpty()) return logs.remove(logs.size() - 1);
        return null;
    }

    boolean isEmpty() {
        return logs.isEmpty() && leaves.isEmpty();
    }
}