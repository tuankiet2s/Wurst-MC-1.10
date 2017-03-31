/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.network.play.client.CPacketPlayer;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.mods.Mod.Bypasses;
import net.wurstclient.features.mods.Mod.Info;

@Info(
	description = "Regenerates your health 1000 times faster.\n"
		+ "Can cause unwanted \"Flying is not enabled!\" kicks.",
	name = "Regen",
	tags = "GodMode, god mode",
	help = "Mods/Regen")
@Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public class RegenMod extends Mod implements UpdateListener
{
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!mc.thePlayer.capabilities.isCreativeMode
			&& mc.thePlayer.getFoodStats().getFoodLevel() > 17
			&& mc.thePlayer.getHealth() < 20 && mc.thePlayer.getHealth() != 0
			&& mc.thePlayer.onGround)
			for(int i = 0; i < 1000; i++)
				mc.thePlayer.connection.sendPacket(new CPacketPlayer());
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
}