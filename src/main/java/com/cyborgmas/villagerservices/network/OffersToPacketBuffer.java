package com.cyborgmas.villagerservices.network;

import com.cyborgmas.villagerservices.registration.RegistryRegistration;
import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import com.cyborgmas.villagerservices.trading.ServiceOffer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

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

         boolean flag = buffer.readBoolean();
         int k = buffer.readInt();
         int l = buffer.readInt();
         int i1 = buffer.readInt();
         int j1 = buffer.readInt();
         float f = buffer.readFloat();
         int k1 = buffer.readInt();
         MerchantOffer offer;
         if(buffer.readBoolean()) { //Service MerchantOffer
            ServiceOffer service = RegistryRegistration.services.getValue(new ResourceLocation(buffer.readString()));
            offer = new ServiceMerchantOffer(price1, price2, service, k, l, i1, f, k1);
         } else {
            offer = new MerchantOffer(price1, price2, result, k, l, i1, f, k1);
         }
         if (flag) {
            offer.getMaxUses();
         }
         offer.setSpecialPrice(j1);
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
