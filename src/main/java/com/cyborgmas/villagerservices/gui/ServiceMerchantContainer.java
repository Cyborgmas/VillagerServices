package com.cyborgmas.villagerservices.gui;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.client.ServiceNPCMerchant;
import com.cyborgmas.villagerservices.registration.DeferredRegistration;
import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;

public class ServiceMerchantContainer extends Container {
   private final IMerchant merchant;
   private final ServiceMerchantInventory merchantInventory;
   private final PlayerInventory playerInventory;
   private int merchantLevel;
   private boolean hasExperienceBar;
   private boolean hasLimitedTrades;

   private final HideableServiceMerchantResultSlot hideableResultSlot;

   public ServiceMerchantContainer(int id, PlayerInventory playerInventory, PacketBuffer buffer) { //TODO find a use for buffer
      this(id, playerInventory, new ServiceNPCMerchant(playerInventory.player), buffer.readBoolean());
   }

   public ServiceMerchantContainer(int id, PlayerInventory inv, IMerchant merchant){
      this(id, inv, merchant, merchant.getOffers().get(0) instanceof ServiceMerchantOffer);
   }

   //On the server, the IMerchant is an entity (Villager) but on the client it is the NPCMerchant impl of IMerchant
   public ServiceMerchantContainer(int id, PlayerInventory playerInventory, IMerchant merchant, boolean openOnService) {
      super(DeferredRegistration.SERVICE_MERCHANT_CONTAINER.get(), id);
      this.merchant = merchant;
      this.merchantInventory = new ServiceMerchantInventory(merchant, this::changeDisplayingTrade);
      this.playerInventory = playerInventory;

      this.addSlot(new Slot(this.merchantInventory, 0, 136, 37)); //slots to buy from
      this.addSlot(new Slot(this.merchantInventory, 1, 162, 37));
      hideableResultSlot = new HideableServiceMerchantResultSlot(playerInventory.player, merchant, this.merchantInventory, 2, 220, 37);
      this.addSlot(hideableResultSlot);

      for(int i = 0; i < 3; ++i) { //player slots
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(playerInventory, k, 108 + k * 18, 142));
      }
   }

   public void changeDisplayingTrade(boolean isService) {
      //Not a bug, but leaving this here for context. This occurs when a services trade does not have anymore uses
      //if((isService && !(merchantInventory.getCurrentOffer() instanceof ServiceMerchantOffer)) || (!isService && merchantInventory.getCurrentOffer() instanceof ServiceMerchantOffer))  {
      //   VillagerServices.LOGGER.error("Is this a bug!");
      //}
      hideableResultSlot.setHidden(isService);
   }

   public void executeService(){
      MerchantOffer offer = merchantInventory.getCurrentOffer();
      PlayerEntity player = playerInventory.player;
      if(offer instanceof ServiceMerchantOffer) {
         if(offer.doTransaction(this.merchantInventory.getStackInSlot(0),this.merchantInventory.getStackInSlot(1))) {
            this.merchant.onTrade(offer);
            player.addStat(Stats.TRADED_WITH_VILLAGER);
            ((ServiceMerchantOffer) offer).executeService(player);
         } else {
            VillagerServices.LOGGER.error("Should be impossible to try an execute the service if its not valid!");
         }
         this.merchant.setXP(this.merchant.getXp()+offer.getGivenExp()); //getGivenExp will be 0 if do transaction fails, but for services it should never
      } else {
         throw new RuntimeException("Trying to execute a service of a non service offer!");
      }
   }

   /**
    * This function is called when clicking a trade. It will get all stacks from the player matching the trade and place them in the inventory.
    */
   public void putAllValidPaymentsInTradeSlots(int offerId) {
      if (this.getOffers().size() > offerId) {
         if(!mergeIfNonEmpty(0, this.merchantInventory)) //if this is false, we still check for the other slot
            return;

         if(!mergeIfNonEmpty(1, this.merchantInventory)) //if this is false, no need to check for later but...
            return;

         if (this.merchantInventory.getStackInSlot(0).isEmpty() && this.merchantInventory.getStackInSlot(1).isEmpty()) {
            ItemStack firstBuyingStack = this.getOffers().get(offerId).func_222205_b();
            ItemStack secondBuyingStack = this.getOffers().get(offerId).getBuyingStackSecond();

            this.transferAllStacksFromPlayerToMerchant(0, firstBuyingStack);
            this.transferAllStacksFromPlayerToMerchant(1, secondBuyingStack);
         }
      }
   }

   /**
    * @return false if merging is currently impossible. True if it can continue
    */
   private boolean mergeIfNonEmpty(int slotId, IInventory inventory){
      ItemStack toMerge = inventory.getStackInSlot(slotId);
      if(!toMerge.isEmpty()) {
         if(!this.mergeItemStack(toMerge, 3, 39, true)){ //false if no slot changed.
            return false;
         }
         inventory.setInventorySlotContents(slotId, toMerge);
      }
      return true;
   }

   private void transferAllStacksFromPlayerToMerchant(int slotId, ItemStack tradedItem) {
      if (!tradedItem.isEmpty()) {
         for(int i = 3; i < 39; ++i) { //getting the player inventory
            ItemStack playerStack = this.inventorySlots.get(i).getStack(); //stack of a slot
            if (!playerStack.isEmpty() && this.areItemStacksEqual(tradedItem, playerStack)) {
               ItemStack merchantStack = this.merchantInventory.getStackInSlot(slotId);
               int mercStackCount = merchantStack.getCount();
               //min between count left in the merc inv and the count of the stack the player is trying to pass
               int amountTransferred = Math.min(tradedItem.getMaxStackSize() - mercStackCount, playerStack.getCount());
               ItemStack playerStackCopy = playerStack.copy();
               int newStackCout = mercStackCount + amountTransferred;
               playerStack.shrink(amountTransferred);
               playerStackCopy.setCount(newStackCout);
               this.merchantInventory.setInventorySlotContents(slotId, playerStackCopy);
               if (newStackCout >= tradedItem.getMaxStackSize()) {
                  break;
               }
            }
         }
      }
   }

   private boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2) {
      return stack1.getItem() == stack2.getItem() && ItemStack.areItemStackTagsEqual(stack1, stack2);
   }

   @Override
   public boolean canInteractWith(PlayerEntity playerIn) {
      return this.merchant.getCustomer() == playerIn;
   }

   @Override
   public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
      return false;
   }

   @Override
   public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
      ItemStack returnStack = ItemStack.EMPTY;
      Slot slot = this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack clickedStack = slot.getStack();
         returnStack = clickedStack.copy();
         if (index == 2) { //result slot
            if (!this.mergeItemStack(clickedStack, 3, 39, true)) {
               return ItemStack.EMPTY;
            }
            slot.onSlotChange(clickedStack, returnStack);
            //this.playMerchantYesSound();
         } else if (index != 0 && index != 1) {
            if (index >= 3 && index < 30) {
               if (!this.mergeItemStack(clickedStack, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 30 && index < 39 && !this.mergeItemStack(clickedStack, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(clickedStack, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (clickedStack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }
         if (clickedStack.getCount() == returnStack.getCount()) {
            return ItemStack.EMPTY;
         }
         slot.onTake(playerIn, clickedStack);
      }
      return returnStack;
   }

   @Override
   public void onContainerClosed(PlayerEntity playerIn) {
      super.onContainerClosed(playerIn);
      this.merchant.setCustomer(null);
      if (!this.merchant.getWorld().isRemote) {
         if (!playerIn.isAlive() || playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity)playerIn).hasDisconnected()) {
            ItemStack itemstack = this.merchantInventory.removeStackFromSlot(0);
            if (!itemstack.isEmpty()) {
               playerIn.dropItem(itemstack, false);
            }
            itemstack = this.merchantInventory.removeStackFromSlot(1);
            if (!itemstack.isEmpty()) {
               playerIn.dropItem(itemstack, false);
            }
         } else {
            playerIn.inventory.placeItemBackInInventory(playerIn.world, this.merchantInventory.removeStackFromSlot(0));
            playerIn.inventory.placeItemBackInInventory(playerIn.world, this.merchantInventory.removeStackFromSlot(1));
         }
      }
   }

   @Override
   public void onCraftMatrixChanged(IInventory inventoryIn) {
      this.merchantInventory.recalculateMerchantSlots();
      super.onCraftMatrixChanged(inventoryIn);
   }

   public void setCurrentRecipeIndex(int currentRecipeIndex) {
      this.merchantInventory.setCurrentRecipeIndex(currentRecipeIndex);
   }

   public MerchantOffers getOffers() {
      return this.merchant.getOffers();
   }

   public boolean canExecuteService() {
      return this.merchantInventory.canExecuteService();
   }

   //Theses methods are all client only.
   public void setClientSideOffers(MerchantOffers offers) {
      this.merchant.setClientSideOffers(offers);
   }

   public void setHasLimitedTrades(boolean hasLimitedTraddes) {
      this.hasLimitedTrades = hasLimitedTraddes;
   }

   public boolean hasLimitedTrades() {
      return this.hasLimitedTrades;
   }

   public void setHasExperienceBar(boolean hasExperienceBar) {
      this.hasExperienceBar = hasExperienceBar;
   }

   public boolean getHasExperienceBar() {
      return this.hasExperienceBar;
   }

   public int getXp() {
      return this.merchant.getXp();
   }

   public int getPendingExp() {
      return this.merchantInventory.getClientSideExp();
   }

   public void setXp(int xp) {
      this.merchant.setXP(xp);
   }

   public int getMerchantLevel() {
      return this.merchantLevel;
   }

   public void setMerchantLevel(int level) {
      this.merchantLevel = level;
   }
}
