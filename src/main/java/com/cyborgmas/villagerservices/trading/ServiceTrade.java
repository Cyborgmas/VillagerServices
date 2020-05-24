package com.cyborgmas.villagerservices.trading;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.common.BasicTrade;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

public class ServiceTrade extends BasicTrade {
   public final Supplier<ServiceOffer> service;

   public ServiceTrade(ItemStack price, ItemStack price2, Supplier<ServiceOffer> offer, int maxTrades, int xp, float priceMult) {
      super(price, price2, ItemStack.EMPTY, maxTrades, xp, priceMult);
      this.service = offer;
   }

   public ServiceTrade(ItemStack price, Supplier<ServiceOffer> offer, int maxTrades, int xp, float priceMult) {
      super(price, ItemStack.EMPTY, maxTrades, xp, priceMult);
      this.service = offer;
   }

   @Nullable
   @Override
   public MerchantOffer getOffer(Entity merchant, Random rand) {
      return new ServiceMerchantOffer(price, price2, service.get(), maxTrades, xp, priceMult);
   }
}
