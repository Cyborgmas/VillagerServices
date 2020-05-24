package com.cyborgmas.villagerservices.gui;

import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ServiceMerchantInventory implements IInventory {
   private final IMerchant merchant;
   private final NonNullList<ItemStack> slots = NonNullList.withSize(3, ItemStack.EMPTY);
   @Nullable
   private MerchantOffer currentOffer;
   private int currentRecipeIndex;
   private int exp;
   private boolean canExecuteService;

   private final Consumer<Boolean> onTradeTypeChange;

   public ServiceMerchantInventory(IMerchant merchantIn, Consumer<Boolean> onTradeTypeChange) {
      this.merchant = merchantIn;
      this.onTradeTypeChange = onTradeTypeChange;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getSizeInventory() {
      return this.slots.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.slots) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }
      return true;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getStackInSlot(int index) {
      return this.slots.get(index);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack decrStackSize(int index, int count) {
      ItemStack stackTaken = this.slots.get(index);
      if (index == 2 && !stackTaken.isEmpty()) { //Result Slot
         return ItemStackHelper.getAndSplit(this.slots, index, stackTaken.getCount());
      } else { //non result slot
         ItemStack returningStack = ItemStackHelper.getAndSplit(this.slots, index, count);
         if (!returningStack.isEmpty() && this.needToRecalculateResult(index)) {
            this.recalculateMerchantSlots();
         }
         return returningStack;
      }
   }

   /**
    * if par1 slot has changed, does resetRecipeAndSlots need to be called?
    */
   private boolean needToRecalculateResult(int slotIn) {
      return slotIn == 0 || slotIn == 1;
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeStackFromSlot(int index) {
      return ItemStackHelper.getAndRemove(this.slots, index);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setInventorySlotContents(int index, ItemStack stack) {
      this.slots.set(index, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
         stack.setCount(this.getInventoryStackLimit());
      }
      if (this.needToRecalculateResult(index)) {
         this.recalculateMerchantSlots();
      }
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean isUsableByPlayer(PlayerEntity player) {
      return this.merchant.getCustomer() == player;
   }

   /**
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void markDirty() {
      this.recalculateMerchantSlots();
   }

   public void recalculateMerchantSlots() {
      this.canExecuteService = false;
      this.currentOffer = null;
      ItemStack firstPrice;
      ItemStack secondPrice;
      if (this.slots.get(0).isEmpty()) { //this is to make the firstprice be the stack in the second slot if the first one is empty.
         firstPrice = this.slots.get(1);
         secondPrice = ItemStack.EMPTY;
      } else {
         firstPrice = this.slots.get(0);
         secondPrice = this.slots.get(1);
      }

      if (firstPrice.isEmpty()) {
         this.setInventorySlotContents(2, ItemStack.EMPTY);
         this.exp = 0;
      } else {
         MerchantOffers offers = this.merchant.getOffers();
         if (!offers.isEmpty()) {
            MerchantOffer offer = offers.func_222197_a(firstPrice, secondPrice, this.currentRecipeIndex);
            if (offer == null || offer.hasNoUsesLeft()) {
               this.currentOffer = offer;
               offer = offers.func_222197_a(secondPrice, firstPrice, this.currentRecipeIndex);
            }

            if (offer != null && !offer.hasNoUsesLeft()) {
               this.currentOffer = offer;
               if(offer instanceof ServiceMerchantOffer) {
                  this.canExecuteService = true;
               } else {
                  this.setInventorySlotContents(2, offer.getCopyOfSellingStack());
               }
               this.exp = offer.getGivenExp();
            } else {
               this.setInventorySlotContents(2, ItemStack.EMPTY);
               this.exp = 0;
            }
         }
         this.merchant.verifySellingItem(this.getStackInSlot(2));
      }
      onTradeTypeChange.accept(this.canExecuteService);
   }

   @Nullable
   public MerchantOffer getCurrentOffer() {
      return this.currentOffer;
   }

   public boolean canExecuteService() {
      return this.canExecuteService;
   }

   public void setCurrentRecipeIndex(int currentRecipeIndexIn) {
      this.currentRecipeIndex = currentRecipeIndexIn;
      this.recalculateMerchantSlots();
   }

   public void clear() {
      this.slots.clear();
   }

   /*public boolean shouldRenderServices(){
      return this.renderServices;
   }*/

   public int getClientSideExp() {
      return this.exp;
   }
}
