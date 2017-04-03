/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.utils.ChatUtils;
import net.wurstclient.utils.EntityUtils;
import net.wurstclient.utils.EntityUtils.TargetSettings;

@Mod.Info(
	description = "Allows you to see the world as someone else.\n"
		+ "Use the .rv command to make it target a specific entity.",
	name = "RemoteView",
	tags = "remote view",
	help = "Mods/RemoteView")
@Mod.Bypasses
public class RemoteViewMod extends Mod implements UpdateListener
{
	private Entity entity = null;
	
	private double oldX;
	private double oldY;
	private double oldZ;
	private float oldYaw;
	private float oldPitch;
	private boolean wasInvisible;
	
	private TargetSettings targetSettingsFind = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return true;
		};
	};
	
	private TargetSettings targetSettingsKeep = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return true;
		};
		
		@Override
		public boolean targetInvisiblePlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetInvisibleMobs()
		{
			return true;
		}
	};
	
	@Override
	public void onEnable()
	{
		// find entity if not already set
		if(entity == null)
		{
			entity = EntityUtils.getClosestEntity(targetSettingsFind);
			
			// check if entity was found
			if(entity == null)
			{
				ChatUtils.message("There is no nearby entity.");
				setEnabled(false);
				return;
			}
		}
		
		// save old data
		oldX = WMinecraft.getPlayer().posX;
		oldY = WMinecraft.getPlayer().posY;
		oldZ = WMinecraft.getPlayer().posZ;
		oldYaw = WMinecraft.getPlayer().rotationYaw;
		oldPitch = WMinecraft.getPlayer().rotationPitch;
		wasInvisible = entity.isInvisibleToPlayer(WMinecraft.getPlayer());
		
		// activate NoClip
		WMinecraft.getPlayer().noClip = true;
		
		// spawn fake player
		EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(
			WMinecraft.getWorld(), WMinecraft.getPlayer().getGameProfile());
		fakePlayer.clonePlayer(WMinecraft.getPlayer(), true);
		fakePlayer.copyLocationAndAnglesFrom(WMinecraft.getPlayer());
		fakePlayer.rotationYawHead = WMinecraft.getPlayer().rotationYawHead;
		WMinecraft.getWorld().addEntityToWorld(-69, fakePlayer);
		
		// success message
		ChatUtils.message("Now viewing " + entity.getName() + ".");
		
		// add listener
		wurst.events.add(UpdateListener.class, this);
	}
	
	public void onToggledByCommand(String viewName)
	{
		// set entity
		if(!isEnabled() && viewName != null && !viewName.isEmpty())
			entity =
				EntityUtils.getEntityWithName(viewName, targetSettingsFind);
		
		// toggle RemoteView
		toggle();
	}
	
	@Override
	public void onUpdate()
	{
		// validate entity
		if(!EntityUtils.isCorrectEntity(entity, targetSettingsKeep))
		{
			setEnabled(false);
			return;
		}
		
		// update position, rotation, etc.
		WMinecraft.getPlayer().copyLocationAndAnglesFrom(entity);
		WMinecraft.getPlayer().motionX = 0;
		WMinecraft.getPlayer().motionY = 0;
		WMinecraft.getPlayer().motionZ = 0;
		
		// set entity invisible
		entity.setInvisible(true);
	}
	
	@Override
	public void onDisable()
	{
		// remove listener
		wurst.events.remove(UpdateListener.class, this);
		
		// reset entity
		if(entity != null)
		{
			ChatUtils.message("No longer viewing " + entity.getName() + ".");
			entity.setInvisible(wasInvisible);
			entity = null;
		}
		
		// reset player
		WMinecraft.getPlayer().noClip = false;
		WMinecraft.getPlayer().setPositionAndRotation(oldX, oldY, oldZ, oldYaw,
			oldPitch);
		
		// remove fake player
		WMinecraft.getWorld().removeEntityFromWorld(-69);
	}
}
