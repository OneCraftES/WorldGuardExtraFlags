package net.goldtreeservers.worldguardextraflags.wg.handlers;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import lombok.Getter;
import net.goldtreeservers.worldguardextraflags.flags.Flags;

public class PlayerDamageMobsFlagHandler extends FlagValueChangeHandler<State>
{
	public static final Factory FACTORY()
	{
		return new Factory();
	}

    public static class Factory extends Handler.Factory<PlayerDamageMobsFlagHandler>
    {
		@Override
        public PlayerDamageMobsFlagHandler create(Session session)
        {
            return new PlayerDamageMobsFlagHandler(session);
        }
    }

    @Getter private Boolean isDamageMobsEnabled;
    private Boolean originalEssentialsGodmode;

	protected PlayerDamageMobsFlagHandler(Session session)
	{
		super(session, Flags.PLAYER_DAMAGE_MOBS);
	}

	@Override
	protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, State value)
	{
		this.handleValue(player, value);
	}

	@Override
	protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType)
	{
		this.handleValue(player, currentValue);
		return true;
	}

	@Override
	protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType)
	{
		this.handleValue(player, null);
		return true;
	}
	
	private void handleValue(LocalPlayer player, State state)
	{
		if (state != null)
		{
			this.isDamageMobsEnabled = state == State.ALLOW;
		}
		else
		{
			this.isDamageMobsEnabled = null;
		}
	}
	
	@Override
    public State getInvincibility(LocalPlayer player)
	{
		if (this.isDamageMobsEnabled != null)
		{
			return this.isDamageMobsEnabled ? State.ALLOW : State.DENY;
		}
		
		return null;
	}
}
