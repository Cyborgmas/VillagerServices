package com.cyborgmas.villagerservices.network;

import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import com.cyborgmas.villagerservices.trading.ServiceOffer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;

public class OffersToPacketBuffer {
   public static MerchantOffers read(PacketBuffer buffer) {
      MerchantOffers merchantoffers = new MerchantOffers();
      int i = buffer.readByte() & 255;

      for(int j = 0; j < i; ++j) {
         ItemStack price1 = buffer.readItemStack();
         ItemStack result = buffer.readItemStack();
         ItemStack price2 = ItemStack.EMPTY;
         if (buffer.readBoolean()) {
            price2 = buffer.readItemStack();
         }

         boolean setMaxUses = buffer.readBoolean();
         int uses = buffer.readInt();
         int maxUses = buffer.readInt();
         int givenXp = buffer.readInt();
         int specialPrice = buffer.readInt();
         float priceMulti = buffer.readFloat();
         int demand = buffer.readInt();
         MerchantOffer offer;
         if(buffer.readBoolean()) { //Is this a Service MerchantOffer
            ServiceOffer service = ServiceOffer.getFromRegistry(buffer.readString());
            offer = new ServiceMerchantOffer(price1, price2, service, uses, maxUses, givenXp, priceMulti, demand);
         } else {
            offer = new MerchantOffer(price1, price2, result, uses, maxUses, givenXp, priceMulti, demand);
         }
         if (setMaxUses) {
            offer.getMaxUses(); //this method is incorrectly named, it sets uses = maxUses;
         }
         offer.setSpecialPrice(specialPrice);
         merchantoffers.add(offer);
      }
      return merchantoffers;
   }

   public static void write(PacketBuffer buffer, MerchantOffers offers) {
      buffer.writeByte((byte)(offers.size() & 255));

      for (MerchantOffer offer : offers) {
         buffer.writeItemStack(offer.getBuyingStackFirst());
         buffer.writeItemStack(offer.getSellingStack());
         ItemStack itemstack = offer.getBuyingStackSecond();
         buffer.writeBoolean(!itemstack.isEmpty());
         if (!itemstack.isEmpty()) {
            buffer.writeItemStack(itemstack);
         }

         buffer.writeBoolean(offer.hasNoUsesLeft());
         buffer.writeInt(offer.getUses());
         buffer.writeInt(offer.func_222214_i());
         buffer.writeInt(offer.getGivenExp());
         buffer.writeInt(offer.getSpecialPrice());
         buffer.writeFloat(offer.getPriceMultiplier());
         buffer.writeInt(offer.getDemand());
         boolean service = offer instanceof ServiceMerchantOffer;
         buffer.writeBoolean(service);
         if(service) {
            buffer.writeString(((ServiceMerchantOffer)offer).getName());
         }
      }
   }
}
