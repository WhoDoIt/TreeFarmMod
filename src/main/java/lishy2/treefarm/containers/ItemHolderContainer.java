package lishy2.treefarm.containers;


import lishy2.treefarm.entities.ItemHolderEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.fml.RegistryObject;

import java.util.Objects;

public abstract class ItemHolderContainer<CT extends Container> extends Container {

    public final ItemHolderEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    final RegistryObject<Block> block;


    protected ItemHolderContainer(final int windowId, final PlayerInventory playerInventory, final ItemHolderEntity tileEntity, final RegistryObject<ContainerType<CT>> containerType, final RegistryObject<Block> block) {
        super(containerType.get(), windowId);
        this.tileEntity = tileEntity;
        this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());
        this.block = block;
        // Main Inventory
        int startX = 80;
        int startY = 20;
        int slotSizePlus2 = 18;
        this.addSlot(new Slot(tileEntity, 0, startX, startY));

        // Main Player Inventory
        int startPlayerInvY = 51;
        int startPlayerInvX = 8;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startPlayerInvX + (column * slotSizePlus2),
                        startPlayerInvY + (row * slotSizePlus2)));
            }
        }
        // Hotbar
        int hotbarY = 109;
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, startPlayerInvX + (column * slotSizePlus2), hotbarY));
        }
    }

    private static ItemHolderEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if (tileAtPos instanceof ItemHolderEntity) {
            return (ItemHolderEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    public ItemHolderContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data, final RegistryObject<ContainerType<CT>> containerType, final RegistryObject<Block> block) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data), containerType, block);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, block.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 1) {
                if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

}
