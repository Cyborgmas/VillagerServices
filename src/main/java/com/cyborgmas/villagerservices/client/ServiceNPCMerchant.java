package com.cyborgmas.villagerservices.client;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

//Client side implementation of IMerchant.
public class ServiceNPCMerchant implements IMerchant {
   private final PlayerEntity customer;
   private MerchantOffers offers = new MerchantOffers();
   private int xp;

   public ServiceNPCMerchant(PlayerEntity customer) {
      this.customer = customer;
   }

   @Nullable
   public PlayerEntity getCustomer() {
      return this.customer;
   }

   public void setCustomer(@Nullable PlayerEntity player) {
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   @OnlyIn(Dist.CLIENT)
   public void setClientSideOffers(@Nullable MerchantOffers offers) {
      this.offers = offers;
   }

   public void onTrade(MerchantOffer offer) {
      offer.increaseUses();
   }

   /**
    * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
    * being played depending if the suggested itemstack is not null.
    */
   public void verifySellingItem(ItemStack stack) {
   }

   public World getWorld() {
      return this.customer.world;
   }

   public int getXp() {
      return this.xp;
   }

   public void setXP(int xpIn) {
      this.xp = xpIn;
   }

   //returns true if has xp bar
   public boolean func_213705_dZ() {
      return true;
   }

   public SoundEvent getYesSound() {
      return SoundEvents.ENTITY_VILLAGER_YES;
   }
}
