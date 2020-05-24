package com.cyborgmas.villagerservices.capability;

import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.item.MerchantOffers;

public interface IServiceSerializer {
   /**
    * This serializes the necessary information to get the {@link ServiceMerchantOffer} back after exiting the game
    * @param offers the offers to serialize
    */
   void serializeServices(MerchantOffers offers);

   /**
    * This takes the deserialized {@link ServiceMerchantOffer}s and puts them in the passed in merchantoffers.
    * @param offers the offers to write the services offers to.
    */
   void deserializeServices(MerchantOffers offers);
}
