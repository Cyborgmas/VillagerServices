package com.cyborgmas.villagerservices;

import com.cyborgmas.villagerservices.capability.ServiceSerializerCap;
import com.cyborgmas.villagerservices.gui.ServiceMerchantScreen;
import com.cyborgmas.villagerservices.network.Network;
import com.cyborgmas.villagerservices.registration.DeferredRegistration;
import com.cyborgmas.villagerservices.trading.ServiceTrade;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(VillagerServices.MOD_ID)
public class VillagerServices
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "villagerservices";
    public static final String MOD_NAME = "Villager Services";

    public VillagerServices() {
        Network.init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::clientSetup);
        bus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::setVillagerTrades);
    }

    public static ResourceLocation getId(String name){
        return new ResourceLocation(MOD_ID, name);
    }

    private void commonSetup(FMLCommonSetupEvent event){
        ServiceSerializerCap.register();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(DeferredRegistration.SERVICE_MERCHANT_CONTAINER.get(), ServiceMerchantScreen::new);
    }

    private void setVillagerTrades(VillagerTradesEvent event){
        if(event.getType() == VillagerProfession.FARMER) {
            VillagerTrades.ITrade temp = event.getTrades().get(1).get(0);
            VillagerTrades.ITrade temp1 = event.getTrades().get(1).get(1);
            event.getTrades().get(1).clear();
            event.getTrades().get(1)
                    .add(new ServiceTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.MELON_SLICE), DeferredRegistration.TEST_OFFER, 10, 10, 0));
            event.getTrades().get(1).add(temp);
            event.getTrades().get(1).add(temp1);
            event.getTrades().get(2).
                    add(new ServiceTrade(new ItemStack(Items.DIAMOND, 10), DeferredRegistration.SUMMON_LIGHTNING_BOLT, 1, 10, 0.1F));
        }
    }
}
