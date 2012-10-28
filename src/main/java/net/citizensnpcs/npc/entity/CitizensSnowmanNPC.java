package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntitySnowman;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftSnowman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowman;
import org.bukkit.util.Vector;

public class CitizensSnowmanNPC extends CitizensMobNPC {

    public CitizensSnowmanNPC(int id, String name) {
        super(id, name, EntitySnowmanNPC.class);
    }

    @Override
    public Snowman getBukkitEntity() {
        return (Snowman) super.getBukkitEntity();
    }

    public static class EntitySnowmanNPC extends EntitySnowman implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySnowmanNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void bh() {
            if (npc == null)
                super.bh();
            // check despawn method, we only want to despawn on chunk unload.
        }

        @Override
        public void bi() {
            super.bi();
            if (npc != null)
                npc.update();
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity);
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public Entity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new SnowmanNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class SnowmanNPC extends CraftSnowman implements NPCHolder {
        private final CitizensNPC npc;

        public SnowmanNPC(EntitySnowmanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}