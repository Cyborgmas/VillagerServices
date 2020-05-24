package com.cyborgmas.villagerservices.trading;

import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface IService {
   void executeService(PlayerEntity player);
}
