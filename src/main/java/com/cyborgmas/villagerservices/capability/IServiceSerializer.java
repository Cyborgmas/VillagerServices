package com.cyborgmas.villagerservices.capability;

import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.item.MerchantOffers;

public interface IServiceSerializer {
   /**
    * This will serialize the necessary information to get the services back after exiting the game
    * @param offers the offers that contain the information to serialize
    */
   void serializeServices(MerchantOffers offers);

   /**
    * This will take the deserialized merchant offers and put them in the passed in merchantoffers.
    * @param offers the offers to write the {@link ServiceMerchantOffer} to.
    */
   void deserializeServices(MerchantOffers offers);
}
