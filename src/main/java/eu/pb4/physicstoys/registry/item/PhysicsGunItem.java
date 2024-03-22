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
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PhysicsGunItem extends Item implements PolymerItem, PhysicsEntityInteractor {
    private static final String TARGET_NBT = "held_entity";
    private static final String PICK_TIME_NBT = "pick_time";

    public PhysicsGunItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.LEATHER_HORSE_ARMOR;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (stack.hasNbt() && stack.getNbt().contains(TARGET_NBT) && entity instanceof ServerPlayerEntity player) {
            var target = ((ServerWorld) world).getEntity(NbtHelper.toUuid(stack.getNbt().get(TARGET_NBT)));
            if (target instanceof BasePhysicsEntity basePhysics) {
                if (selected || player.getOffHandStack() == stack) {
                    basePhysics.setHolder((PlayerEntity) entity);
                    HitResult cast;// = entity.raycast(3, 0, false);
                    {
                        var maxDistance = 3;
                        Vec3d vec3d = entity.getCameraPosVec(0);
                        Vec3d vec3d2 = entity.getRotationVec(0);
                        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                        cast = world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, basePhysics));
                    }

                    var previous = basePhysics.getRigidBody().getPhysicsLocation(new Vector3f());

                    basePhysics.getRigidBody().setPhysicsLocation(previous.mult(0.6f).add(Convert.toBullet(cast.getPos()).mult(0.4f)));
                    basePhysics.getRigidBody().setLinearVelocity(basePhysics.getRigidBody().getFrame().getLocationDelta(new Vector3f()).mult(10));
                } else {
                    stack.getNbt().remove(TARGET_NBT);
                    basePhysics.getRigidBody().activate();
                    basePhysics.setHolder(null);
                }
            } else {
                stack.getNbt().remove(TARGET_NBT);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (stack.hasNbt() && stack.getNbt().contains(TARGET_NBT)) {
            var pickTime = stack.getNbt().getLong(PICK_TIME_NBT);
            if (world.getTime() - pickTime < 5) {
                return TypedActionResult.fail(stack);
            }

            var target = ((ServerWorld) world).getEntity(NbtHelper.toUuid(stack.getNbt().get(TARGET_NBT)));
            if (target instanceof BasePhysicsEntity basePhysics) {
                basePhysics.getRigidBody().activate();
                basePhysics.setHolder(null);
                stack.getNbt().remove(TARGET_NBT);
                return TypedActionResult.success(stack, true);
            }
            stack.getNbt().remove(TARGET_NBT);
        }

        return TypedActionResult.fail(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getStack().hasNbt() && context.getStack().getNbt().contains(TARGET_NBT)) {
            return ActionResult.FAIL;
        }

        var blockState = context.getWorld().getBlockState(context.getBlockPos());
        context.getWorld().setBlockState(context.getBlockPos(), Blocks.AIR.getDefaultState());
        context.getStack().getNbt().putLong(PICK_TIME_NBT, context.getWorld().getTime());

        if (blockState.isOf(USRegistry.PHYSICAL_TNT_BLOCK)) {
            var vec = Vec3d.ofCenter(context.getBlockPos());
            var entity = PhysicalTntEntity.of(context.getWorld(), vec.x, vec.y, vec.z, context.getPlayer());
            entity.setHolder(context.getPlayer());
            context.getStack().getOrCreateNbt().put(TARGET_NBT, NbtHelper.fromUuid(entity.getUuid()));
            context.getWorld().spawnEntity(entity);
        } else {
            var entity = BlockPhysicsEntity.create(context.getWorld(), blockState, context.getBlockPos());
            entity.setDespawnTimer(5 * 20);
            entity.setHolder(context.getPlayer());
            context.getStack().getOrCreateNbt().put(TARGET_NBT, NbtHelper.fromUuid(entity.getUuid()));
            context.getWorld().spawnEntity(entity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return itemStack.hasNbt() && itemStack.getNbt().contains(TARGET_NBT) ? 0xffe357 : 0xbd7100;
    }

    @Override
    public void onInteractWith(PlayerEntity player, ItemStack stack, Vec3d hitPos, BasePhysicsEntity basePhysics) {
        basePhysics.setOwner(player.getGameProfile());
        if (stack.hasNbt() && basePhysics.getHolder() == player) {
            basePhysics.setHolder(null);
            stack.getNbt().remove(TARGET_NBT);
        } else {
            if (stack.hasNbt() && stack.getNbt().contains(TARGET_NBT)) {
                return;
            }

            stack.getOrCreateNbt().put(TARGET_NBT, NbtHelper.fromUuid(basePhysics.getUuid()));
            basePhysics.setHolder(player);
            stack.getNbt().putLong(PICK_TIME_NBT, player.world.getTime());

        }
    }

    @Override
    public void onAttackWith(ServerPlayerEntity player, ItemStack stack, BasePhysicsEntity basePhysics) {
        if (stack.hasNbt() && basePhysics.getHolder() == player) {
            basePhysics.getRigidBody().applyCentralImpulse(Convert.toBullet(player.getRotationVec(0).multiply(200)));
            basePhysics.setHolder(null);
            basePhysics.setOwner(player.getGameProfile());
            if (basePhysics instanceof BlockPhysicsEntity blockPhysicsEntity && !(basePhysics instanceof PhysicalTntEntity)) {
                blockPhysicsEntity.setDespawnTimer(10 * 20);
            }
            stack.getNbt().remove(TARGET_NBT);
        }
    }
}
