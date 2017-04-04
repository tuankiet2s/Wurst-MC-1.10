/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@Mod.Info(
	description = "Automatically throws splash healing potions if your health is below the set value.",
	name = "AutoSplashPot",
	tags = "AutoPotion,auto potion,auto splash potion",
	help = "Mods/AutoSplashPot")
@Mod.Bypasses
public final class AutoSplashPotMod extends Mod implements UpdateListener
{
	public float health = 18F;
	
	@Override
	public void initSettings()
	{
		settings.add(
			new SliderSetting("Health", health, 2, 20, 1, ValueDisplay.INTEGER)
			{
				@Override
				public void update()
				{
					health = (float)getValue();
				}
			});
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		// check if no container is open
		if(mc.currentScreen instanceof GuiContainer
			&& !(mc.currentScreen instanceof GuiInventory))
			return;
		
		// check if health is low
		if(WMinecraft.getPlayer().getHealth() >= health)
			return;
		
		// find health potions
		int potionInInventory = findPotion(9, 36);
		int potionInHotbar = findPotion(36, 45);
		
		// check if any potion was found
		if(potionInInventory == -1 && potionInHotbar == -1)
			return;
		
		if(hasTimePassedM(500))
			if(potionInHotbar != -1)
			{
				// throw potion in hotbar
				int oldSlot = WMinecraft.getPlayer().inventory.currentItem;
				WConnection.sendPacket(new CPacketPlayer.Rotation(
					WMinecraft.getPlayer().rotationYaw, 90.0F,
					WMinecraft.getPlayer().onGround));
				WConnection
					.sendPacket(new CPacketHeldItemChange(potionInHotbar - 36));
				mc.playerController.updateController();
				WConnection.sendPacket(
					new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
				WConnection.sendPacket(new CPacketHeldItemChange(oldSlot));
				WConnection.sendPacket(new CPacketPlayer.Rotation(
					WMinecraft.getPlayer().rotationYaw,
					WMinecraft.getPlayer().rotationPitch,
					WMinecraft.getPlayer().onGround));
				
				// reset timer
				updateLastMS();
			}else
				// move potion in inventory to hotbar
				mc.playerController.windowClick(0, potionInInventory, 0,
					ClickType.QUICK_MOVE, WMinecraft.getPlayer());
			
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	private int findPotion(int startSlot, int endSlot)
	{
		for(int i = startSlot; i < endSlot; i++)
		{
			ItemStack stack =
				WMinecraft.getPlayer().inventoryContainer.getSlot(i).getStack();
			if(stack != null && stack.getItem() == Items.SPLASH_POTION)
				for(PotionEffect effect : PotionUtils
					.getEffectsFromStack(stack))
					if(effect.getPotion() == MobEffects.INSTANT_HEALTH)
						return i;
		}
		return -1;
	}
}
