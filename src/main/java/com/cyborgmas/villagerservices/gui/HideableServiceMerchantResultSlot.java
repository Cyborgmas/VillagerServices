package com.cyborgmas.villagerservices.gui;

import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.stats.Stats;

public class HideableServiceMerchantResultSlot extends Slot {
   private final ServiceMerchantInventory merchantInventory;
   private final PlayerEntity player;
   private int removeCount;
   private final IMerchant merchant;
   private boolean hidden;

   public HideableServiceMerchantResultSlot(PlayerEntity player, IMerchant merchant, ServiceMerchantInventory merchantInventory, int slotIndex, int xPosition, int yPosition) {
      super(merchantInventory, slotIndex, xPosition, yPosition);
      this.player = player;
      this.merchant = merchant;
      this.merchantInventory = merchantInventory;
      this.hidden = false;
   }

   public void setHidden(boolean hidden){
      this.hidden = hidden;
   }

   @Override
   public boolean isEnabled() {
      return !hidden;
   }

   /**
    * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
    */
   public boolean isItemValid(ItemStack stack) {
      return false;
   }

   /**
    * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new stack.
    */
   public ItemStack decrStackSize(int amount) {
      if (this.getHasStack()) {
         this.removeCount += Math.min(amount, this.getStack().getCount());
      }
      return super.decrStackSize(amount);
   }

   /**
    * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
    * internal count then calls onCrafting(item).
    */
   protected void onCrafting(ItemStack stack, int amount) {
      this.removeCount += amount;
      this.onCrafting(stack);
   }

   /**
    * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
    */
   protected void onCrafting(ItemStack stack) {
      stack.onCrafting(this.player.world, this.player, this.removeCount);
      this.removeCount = 0;
   }

   //The inventory handles the logic of whether the slot contains a stack or not.
   // this can never execute unless the stack is valid beforehand.
   public ItemStack onTake(PlayerEntity player, ItemStack stack) {
      this.onCrafting(stack);
      MerchantOffer offer = this.merchantInventory.getCurrentOffer();
      if (offer != null) {
         if(offer instanceof ServiceMerchantOffer) {
            throw new RuntimeException("Can't take the result out of a service offer!");
         }
         ItemStack firstPrice = this.merchantInventory.getStackInSlot(0);
         ItemStack secondPrice = this.merchantInventory.getStackInSlot(1);
         if (offer.doTransaction(firstPrice, secondPrice) || offer.doTransaction(secondPrice, firstPrice)) {
            this.merchant.onTrade(offer);
            player.addStat(Stats.TRADED_WITH_VILLAGER);
            this.merchantInventory.setInventorySlotContents(0, firstPrice); //this seems useless? doTransaction shrinks the stack already
            this.merchantInventory.setInventorySlotContents(1, secondPrice);
         }
         this.merchant.setXP(this.merchant.getXp() + offer.getGivenExp());
      }
      return stack;
   }
}
