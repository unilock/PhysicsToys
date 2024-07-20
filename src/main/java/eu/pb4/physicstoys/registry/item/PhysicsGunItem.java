package eu.pb4.physicstoys.registry.item;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PhysicsGunItem extends Item implements PolymerItem, PhysicsEntityInteractor {
    public PhysicsGunItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.LEATHER_HORSE_ARMOR;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (stack.contains(USRegistry.TARGET_COMPONENT) && entity instanceof ServerPlayerEntity player) {
            var target = ((ServerWorld) world).getEntity(stack.get(USRegistry.TARGET_COMPONENT));
            if (target instanceof BasePhysicsEntity basePhysics) {
                if (selected || player.getOffHandStack() == stack) {
                    basePhysics.setHolder((PlayerEntity) entity);
                    var cast = entity.raycast(3, 0, false);
                    basePhysics.getRigidBody().setPhysicsLocation(Convert.toBullet(cast.getPos()));
                    basePhysics.getRigidBody().setLinearVelocity(basePhysics.getRigidBody().getFrame().getLocationDelta(new Vector3f()).mult(5));
                } else {
                    stack.remove(USRegistry.TARGET_COMPONENT);
                    basePhysics.getRigidBody().activate();
                    basePhysics.setHolder(null);
                }
            } else {
                stack.remove(USRegistry.TARGET_COMPONENT);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        /*var altStack = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if ((stack.hasNbt() && stack.getNbt().contains(TARGET_NBT)) || altStack.isEmpty()) {
            return TypedActionResult.fail(stack);
        }
        var cast = user.raycast(1, 0, false);

        if (stack.isOf(USRegistry.PHYSICAL_TNT_ITEM)) {
            var entity = PhysicalTntEntity.of(world, cast.getPos().x, cast.getPos().y, cast.getPos().z, user);
            entity.setHolder(user);
            stack.getOrCreateNbt().put(TARGET_NBT, NbtHelper.fromUuid(entity.getUuid()));
            world.spawnEntity(entity);
            if (!user.isCreative()) {
                altStack.decrement(1);
            }

            return TypedActionResult.success(stack, true);
        } else if (altStack.getItem() instanceof BlockItem blockItem) {
            var entity = BlockPhysicsEntity.create(world, blockItem.getBlock().getDefaultState(), BlockPos.ofFloored(cast.getPos()));
            entity.setDespawnTimer(10);
            entity.setHolder(user);
            stack.getOrCreateNbt().put(TARGET_NBT, NbtHelper.fromUuid(entity.getUuid()));
            world.spawnEntity(entity);
            if (!user.isCreative()) {
                altStack.decrement(1);
            }
            return TypedActionResult.success(stack, true);
        }*/

        return TypedActionResult.fail(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getStack().contains(USRegistry.TARGET_COMPONENT)) {
            return ActionResult.FAIL;
        }

        var blockState = context.getWorld().getBlockState(context.getBlockPos());
        context.getWorld().setBlockState(context.getBlockPos(), Blocks.AIR.getDefaultState());
        if (blockState.isOf(USRegistry.PHYSICAL_TNT_BLOCK)) {
            var vec = Vec3d.ofCenter(context.getBlockPos());
            var entity = PhysicalTntEntity.of(context.getWorld(), vec.x, vec.y, vec.z, context.getPlayer());
            entity.setHolder(context.getPlayer());
            context.getStack().set(USRegistry.TARGET_COMPONENT, entity.getUuid());
            context.getWorld().spawnEntity(entity);
        } else {
            var entity = BlockPhysicsEntity.create(context.getWorld(), blockState, context.getBlockPos());
            entity.setDespawnTimer(5 * 20);
            entity.setHolder(context.getPlayer());
            context.getStack().set(USRegistry.TARGET_COMPONENT, entity.getUuid());
            context.getWorld().spawnEntity(entity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return itemStack.contains(USRegistry.TARGET_COMPONENT) ? 0xffe357 : 0xbd7100;
    }

    @Override
    public void onInteractWith(PlayerEntity player, ItemStack stack, Vec3d hitPos, BasePhysicsEntity basePhysics) {
        basePhysics.setOwner(player.getGameProfile());
        if (basePhysics.getHolder() == player) {
            basePhysics.setHolder(null);
            stack.remove(USRegistry.TARGET_COMPONENT);
        } else {
            if (stack.contains(USRegistry.TARGET_COMPONENT)) {
                return;
            }

            stack.set(USRegistry.TARGET_COMPONENT, basePhysics.getUuid());
            basePhysics.setHolder(player);
        }
    }

    @Override
    public void onAttackWith(ServerPlayerEntity player, ItemStack stack, BasePhysicsEntity basePhysics) {
        if (stack.contains(USRegistry.TARGET_COMPONENT) && basePhysics.getHolder() == player) {
            basePhysics.getRigidBody().applyCentralImpulse(Convert.toBullet(player.getRotationVec(0).multiply(200)));
            basePhysics.setHolder(null);
            basePhysics.setOwner(player.getGameProfile());
            if (basePhysics instanceof BlockPhysicsEntity blockPhysicsEntity && !(basePhysics instanceof PhysicalTntEntity)) {
                blockPhysicsEntity.setDespawnTimer(10 * 20);
            }
            stack.remove(USRegistry.TARGET_COMPONENT);
        }
    }
}
