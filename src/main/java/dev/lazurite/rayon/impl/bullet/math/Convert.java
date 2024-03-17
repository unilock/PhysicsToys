package dev.lazurite.rayon.impl.bullet.math;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class Convert {
    public static Box toMinecraft(BoundingBox box) {
        var min = box.getMin(new Vector3f());
        var max = box.getMax(new Vector3f());
        return new Box(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public static BoundingBox toBullet(Box box) {
        return new BoundingBox(toBullet(box.getCenter()),
                (float) box.getLengthX(),
                (float) box.getLengthY(),
                (float) box.getLengthZ());
    }

    public static com.jme3.math.Quaternion toBullet(Quaternionf quat) {
        return new com.jme3.math.Quaternion(quat.x(), quat.y(), quat.z(), quat.w());
    }

    public static Vector3f toBullet(BlockPos blockPos) {
        return new Vector3f(blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f);
    }

    public static Quaternionf toMinecraft(Quaternion quat) {
        return new Quaternionf(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public static org.joml.Vector3f toMinecraft(Vector3f vector3f) {
        return new org.joml.Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    public static Vector3f toBullet(org.joml.Vector3f vector3f) {
        return new Vector3f(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public static Vector3f toBullet(Vec3d vec3) {
        return new Vector3f((float) vec3.getX(), (float) vec3.getY(), (float) vec3.getZ());
    }
}
