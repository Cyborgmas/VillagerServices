package com.cyborgmas.villagerservices.trading;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ServiceMerchantOffer extends MerchantOffer {
   private final ServiceOffer service;

   /**
    * Constructs a ServiceMerchantOffer from a {@link MerchantOffer} and a {@link ServiceOffer}. This is necessary to deserialize ServiceMerchantOffers
    * since the {@link MerchantOffers} class does not invoke a read method to deserialize its offers but construct one directly using {@link MerchantOffer#MerchantOffer(CompoundNBT)}
    * making all data invoked in an overridden write method useless.
    * @param offer the offer that was previously incorrectly deserialized
    * @param service the service to add back to make a proper ServiceMerchantOffer. It is found based on its registry name.
    */
   public ServiceMerchantOffer(MerchantOffer offer, ServiceOffer service){
      super(offer.getBuyingStackFirst(), offer.getBuyingStackSecond(), offer.getSellingStack(), offer.getUses(), offer.func_222214_i(), offer.getGivenExp(), offer.getPriceMultiplier(), offer.getDemand());
      this.service = service;
   }

   public ServiceMerchantOffer(ItemStack firstPrice, ServiceOffer service, int maxUses, int givenExp, float priceMulti) {
      super(firstPrice, ItemStack.EMPTY, maxUses, givenExp, priceMulti);
      this.service = service;
   }

   public ServiceMerchantOffer(ItemStack firstPrice, ItemStack secondPrice,  ServiceOffer service, int uses, int maxUses, int givenExp, float priceMulti) {
      super(firstPrice, secondPrice, ItemStack.EMPTY, uses, maxUses, givenExp, priceMulti);
      this.service = service;
   }

   public ServiceMerchantOffer(ItemStack firstPrice, ItemStack secondPrice, ServiceOffer service, int maxUses, int givenExp, float priceMulti) {
      super(firstPrice, secondPrice, ItemStack.EMPTY, maxUses, givenExp, priceMulti);
      this.service = service;
   }

   public ServiceMerchantOffer(ItemStack firstPrice, ItemStack secondPrice, ServiceOffer service, int uses, int maxUses, int givenExp, float priceMulti, int demand) {
      super(firstPrice, secondPrice, ItemStack.EMPTY, uses, maxUses, givenExp, priceMulti, demand);
      this.service = service;
   }

   public void executeService(PlayerEntity player){
      service.executeService(player);
   }

   /**
    * @return a 26 x 26 texture that will replace the result slot of the villager. if null, no texture will be added.
    */
   @Nullable
   public ResourceLocation getBackground(){
      return service.background;
   }

   /**
    * @return a 16 x 16 texture that will replace what would be the item in a normal trade.
    */
   public ResourceLocation getTexture(){
      return service.texture;
   }

   /**
    * @return the list of strings to display on the tooltip while hovering over the texture. Can be a translation string.
    */
   public List<String> getTooltip(){
      return service.tooltip;
   }

   public String getName(){
      return service.getRegistryName().toString();
   }
}
