package net.goldtreeservers.worldguardextraflags.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import net.goldtreeservers.worldguardextraflags.flags.helpers.ForcedStateFlag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.world.PortalCreateEvent;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import net.goldtreeservers.worldguardextraflags.flags.Flags;

@RequiredArgsConstructor
public class EntityListener implements Listener {
    private final WorldGuardPlugin worldGuardPlugin;
    private final RegionContainer regionContainer;
    private final SessionManager sessionManager;

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreateEvent(PortalCreateEvent event) {
        LocalPlayer localPlayer;
        if (event.getEntity() instanceof Player player) {
            localPlayer = this.worldGuardPlugin.wrapPlayer(player);
            if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld())) {
                return;
            }
        } else {
            localPlayer = null;
        }

        for (BlockState block : event.getBlocks()) {
            if (this.regionContainer.createQuery().queryState(BukkitAdapter.adapt(block.getLocation()), localPlayer, Flags.NETHER_PORTALS) == State.DENY) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
            if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld())) {
                return;
            }

            ForcedStateFlag.ForcedState state = this.regionContainer.createQuery().queryValue(localPlayer.getLocation(), localPlayer, Flags.GLIDE);
            switch (state) {
                case ALLOW:
                    break;
                case DENY: {
                    if (!event.isGliding()) {
                        return;
                    }

                    event.setCancelled(true);

                    //Prevent the player from being allowed to glide by spamming space
                    player.teleport(player.getLocation());

                    break;
                }
                case FORCE: {
                    if (event.isGliding()) {
                        return;
                    }

                    event.setCancelled(true);

                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if(event.getEntity().getType() != EntityType.PLAYER) {

                LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
                ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(localPlayer.getLocation());

                State CanDamageMobs = regions.queryValue(localPlayer, Flags.PLAYER_DAMAGE_MOBS);
                if (CanDamageMobs != null) {
                    if (CanDamageMobs == State.DENY) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
