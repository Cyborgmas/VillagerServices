package com.cyborgmas.villagerservices.events;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.capability.ServiceSerializerCap;
import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.item.MerchantOffers;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Mod.EventBusSubscriber(modid = VillagerServices.MOD_ID)
public class EventHandler {
   private static final Marker MARKER = MarkerManager.getMarker("Service serialization");
   /**
    * These fields are used to track data loss if {@link #deserializeServices} or {@link #serializeServices}
    * do not throw an exception.
    * These fields are only queried and update server side
    */
   private static int whileRunningCount;
   private static int onStopCount;
   private static boolean countedDeserialization;

   @SubscribeEvent
   public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
      Entity entity = event.getObject();
      if(ServiceSerializerCap.canAttachTo(entity)){
         event.addCapability(ServiceSerializerCap.NAME, new ServiceSerializerCap());
         if(countedDeserialization && !entity.world.isRemote)
            whileRunningCount++;
      }
   }

   /**
    * Invalidates the merchants capabilities on death.
    */
   @SubscribeEvent
   public static void merchantDeath(LivingDeathEvent event){
      Entity entity = event.getEntity();
      if(entity instanceof IMerchant) {
         entity.getCapability(ServiceSerializerCap.INSTANCE).invalidate();
         if(!entity.world.isRemote)
            whileRunningCount--;
      }
   }

   /**
    * Resetting these fields is needed when on the client each time a new world is loaded.
    */
   @SubscribeEvent
   public static void resetCount(FMLServerAboutToStartEvent event){
      whileRunningCount = 0;
      onStopCount = 0;
      countedDeserialization = false;
   }

   /**
    * The MerchantOffers are put up to date using the data stored in the merchant's {@link ServiceSerializerCap}
    * This is done after the worlds are loaded in, so entities are loaded in as well
    */
   @SubscribeEvent
   public static void deserializeServices(FMLServerStartedEvent event){
      try {
         event.getServer().getWorlds()
                 .forEach(world -> world.getEntities()
                         .filter(entity -> entity instanceof IMerchant)
                         .forEach(merchant -> merchant.getCapability(ServiceSerializerCap.INSTANCE)
                                 .ifPresent(serializer -> {
                                    serializer.deserializeServices(((IMerchant)merchant).getOffers());
                                    whileRunningCount++;
                                 })));
         countedDeserialization = true;
         VillagerServices.LOGGER.info(MARKER,"Successfully deserialized service offers");
      } catch (Throwable throwable) {
         CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Could not deserialize service offers");
         event.getServer().addServerInfoToCrashReport(crashReport);
         throw new ReportedException(crashReport);
      }
   }

   /**
    * The {@link MerchantOffers} do not serialize any {@link ServiceMerchantOffer} data.
    * It is instead stored in the merchant's {@link ServiceSerializerCap}
    * This is done before the server stops, so entities are still loaded.
    */
   @SubscribeEvent
   public static void serializeServices(FMLServerStoppingEvent event){
      try {
         event.getServer().getWorlds()
                 .forEach(world -> world.getEntities()
                         .filter(entity -> entity instanceof IMerchant)
                         .forEach(merchant -> merchant.getCapability(ServiceSerializerCap.INSTANCE)
                                 .ifPresent(serializer -> {
                                    serializer.serializeServices(((IMerchant)merchant).getOffers());
                                    onStopCount++;
                                 })));
         VillagerServices.LOGGER.info(MARKER,"Successfully serialized service offers");
         if(onStopCount != whileRunningCount) {
            String cause = onStopCount > whileRunningCount ? " more " : " less ";
            int difference = onStopCount - whileRunningCount;
            difference = difference < 0 ? difference*-1 : difference;
            VillagerServices.LOGGER.error(MARKER,"Problem encountered while serializing offers: there was "+ difference + cause +
                    "serialization/killing done then there was deserialization/new merchants." +
                    "This strongly suggests that data loss has occurred.");
         }
      } catch (Throwable throwable) {
         CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Could not serialize service offers");
         event.getServer().addServerInfoToCrashReport(crashReport);
         throw new ReportedException(crashReport);
      }
   }
}
