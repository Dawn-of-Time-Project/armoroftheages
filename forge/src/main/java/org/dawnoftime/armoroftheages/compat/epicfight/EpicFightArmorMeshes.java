package org.dawnoftime.armoroftheages.compat.epicfight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import org.dawnoftime.armoroftheages.client.models.anubis_armor.ChestAnubisArmorModel;
import org.dawnoftime.armoroftheages.client.models.centurion_armor.ChestCenturionArmorModel;
import org.dawnoftime.armoroftheages.client.models.centurion_armor.FeetCenturionArmorModel;
import org.dawnoftime.armoroftheages.client.models.centurion_armor.LegsCenturionArmorModel;
import org.dawnoftime.armoroftheages.client.models.exalted_aurum.ChestExaltedAurumArmorModel;
import org.dawnoftime.armoroftheages.client.models.exalted_aurum.LegsExaltedAurumArmorModel;
import org.dawnoftime.armoroftheages.client.models.pharaoh_armor.LegsPharaohArmorModel;
import org.dawnoftime.armoroftheages.client.models.quetzalcoatl_armor.ChestQuetzalcoatlArmorModel;
import org.dawnoftime.armoroftheages.client.models.quetzalcoatl_armor.FeetQuetzalcoatlArmorModel;
import org.dawnoftime.armoroftheages.client.models.quetzalcoatl_armor.LegsQuetzalcoatlArmorModel;
import org.dawnoftime.armoroftheages.client.models.iron_plate_armor.ChestIronPlateArmorModel;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.client.model.transformer.HumanoidModelTransformer;
import yesman.epicfight.api.client.model.transformer.VanillaModelTransformer;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bakes the Epic Fight meshes of our armors.
 * <p>
 * Epic Fight splits the cubes of the torso and of the limbs so they can bend. This algorithm
 * only works with axis aligned cubes: on a rotated cube (a diagonal strap, tilted plates...) it creates vertices
 * with wrong positions and wrong texture coordinates, which shows up as long spikes and stretched pixels.
 * <p>
 * This transformer is a copy of the vanilla Epic Fight one, with two differences. A rotated cube of the torso is
 * attached rigidly to the joint of its height instead of being split. An accessory of a limb that crosses the elbow
 * or the knee line (a shoulder pad, a bracer, a leg guard) is attached rigidly to the half of the limb where it is.
 */
public class EpicFightArmorMeshes extends HumanoidModelTransformer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHEST_JOINT = 8;
    private static final int TORSO_JOINT = 7;
    private static final int RIGHT_UPPER_ARM_JOINT = 11;
    private static final int RIGHT_LOWER_ARM_JOINT = 12;
    private static final int LEFT_UPPER_ARM_JOINT = 16;
    private static final int LEFT_LOWER_ARM_JOINT = 17;
    /** Height (in model pixels) where Epic Fight switches from the chest joint to the torso joint. */
    private static final float WAIST_HEIGHT = 18.0F;
    private static final int RIGHT_UPPER_LEG_JOINT = 1;
    private static final int RIGHT_LOWER_LEG_JOINT = 2;
    private static final int LEFT_UPPER_LEG_JOINT = 4;
    private static final int LEFT_LOWER_LEG_JOINT = 5;
    /** Height (in model pixels) where Epic Fight cuts the arms to make the elbow. */
    private static final float ELBOW_HEIGHT = 19.0F;
    /** Height (in model pixels) where Epic Fight cuts the legs to make the knee. */
    private static final float KNEE_HEIGHT = 6.0F;
    private static final float AXIS_ALIGNED_EPSILON = 1.0E-4F;
    /** Half height (in model pixels) of the zone where a limb goes from its upper joint to its lower joint. */
    private static final float BLEND_HALF_HEIGHT = 2.0F;
    /** Number of slices in this zone. */
    private static final int BLEND_STEPS = 4;

    /** Epic Fight keeps its triangle counter private, we have to move it after adding our own vertices. */
    private static final Field INDEX_COUNTER_FIELD = findIndexCounterField();
    private static final boolean CLEAN_CUT_AVAILABLE = INDEX_COUNTER_FIELD != null;

    /** Rigid transformer of Epic Fight, used for the cubes we do not want to be split. Its joint is replaced afterwards. */
    private static final PartTransformer<ModelPart.Cube> RIGID = VanillaModelTransformer.HEAD;
    private static final PartTransformer<ModelPart.Cube> CHEST = VanillaModelTransformer.CHEST;
    private static final PartTransformer<ModelPart.Cube> RIGHT_ARM = VanillaModelTransformer.RIGHT_ARM;
    private static final PartTransformer<ModelPart.Cube> LEFT_ARM = VanillaModelTransformer.LEFT_ARM;
    private static final PartTransformer<ModelPart.Cube> RIGHT_LEG = VanillaModelTransformer.RIGHT_LEG;
    private static final PartTransformer<ModelPart.Cube> LEFT_LEG = VanillaModelTransformer.LEFT_LEG;

    private static Field findIndexCounterField() {
        try {
            Field field = PartTransformer.IndexCounter.class.getDeclaredField("indexCounter");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("Armor of the Ages could not access the Epic Fight index counter, the clean limb cut is disabled", e);
            return null;
        }
    }

    private record Partition(PartTransformer<ModelPart.Cube> transformer, ModelPart part, String name) {
    }

    public static void register() {
        HumanoidModelBaker.registerNewTransformer(new EpicFightArmorMeshes());
    }

    /**
     * Armors listed here use the rigid attachment for their rotated chest cubes. Add the other models once they are validated.
     */
    private static boolean isSupported(HumanoidModel<?> model) {
        return model instanceof ChestCenturionArmorModel<?>
                || model instanceof LegsCenturionArmorModel<?>
                || model instanceof FeetCenturionArmorModel<?>
                || model instanceof ChestIronPlateArmorModel<?>
                || model instanceof ChestAnubisArmorModel<?>
                || model instanceof ChestExaltedAurumArmorModel<?>
                || model instanceof LegsExaltedAurumArmorModel<?>
                || model instanceof LegsPharaohArmorModel<?>
                || model instanceof ChestQuetzalcoatlArmorModel<?>
                || model instanceof LegsQuetzalcoatlArmorModel<?>
                || model instanceof FeetQuetzalcoatlArmorModel<?>;
    }

    /**
     * Armors listed here also get a progressive joint at the elbow and the knee, instead of the single stretched strip
     * Epic Fight adds between the two halves of the limb.
     */
    private static boolean isCleanCut(HumanoidModel<?> model) {
        return CLEAN_CUT_AVAILABLE && (model instanceof ChestCenturionArmorModel<?> || model instanceof LegsCenturionArmorModel<?> || model instanceof FeetCenturionArmorModel<?>);
    }

    @Override
    public SkinnedMesh transformArmorModel(HumanoidModel<?> model) {
        if (!isSupported(model)) {
            return null;
        }
        try {
            return bake(model, isCleanCut(model));
        } catch (RuntimeException e) {
            // Returning null lets Epic Fight use its default conversion
            LOGGER.warn("Armor of the Ages could not convert {} for Epic Fight, using the default conversion", model.getClass().getSimpleName(), e);
            return null;
        }
    }

    private static SkinnedMesh bake(HumanoidModel<?> model, boolean cleanCut) {
        // Remove the entity animation
        List<ModelPart> parts = List.of(model.head, model.hat, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg);
        parts.forEach(part -> part.loadPose(part.getInitialPose()));

        List<Partition> partitions = new ArrayList<>();
        addPartition(partitions, VanillaModelTransformer.HEAD, model.head, "head");
        addPartition(partitions, VanillaModelTransformer.HEAD, model.hat, "hat");
        addPartition(partitions, VanillaModelTransformer.CHEST, model.body, "body");
        addPartition(partitions, VanillaModelTransformer.RIGHT_ARM, model.rightArm, "rightArm");
        addPartition(partitions, VanillaModelTransformer.LEFT_ARM, model.leftArm, "leftArm");
        addPartition(partitions, VanillaModelTransformer.LEFT_LEG, model.leftLeg, "leftLeg");
        addPartition(partitions, VanillaModelTransformer.RIGHT_LEG, model.rightLeg, "rightLeg");

        List<SingleGroupVertexBuilder> vertices = new ArrayList<>();
        Map<MeshPartDefinition, IntList> indices = new HashMap<>();
        PoseStack poseStack = new PoseStack();
        PartTransformer.IndexCounter indexCounter = new PartTransformer.IndexCounter();

        poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(180.0F));
        poseStack.mulPose(QuaternionUtils.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -24.0F, 0.0F);

        for (Partition partition : partitions) {
            bakePart(poseStack, partition.name(), partition, partition.part(), vertices, indices, new ArrayList<>(), indexCounter, false, cleanCut);
        }

        return SingleGroupVertexBuilder.loadVertexInformation(vertices, indices);
    }

    private static void addPartition(List<Partition> partitions, PartTransformer<ModelPart.Cube> transformer, ModelPart part, String name) {
        if (part.skipDraw || part.visible) {
            partitions.add(new Partition(transformer, part, name));
        }
    }

    private static void bakePart(PoseStack poseStack, String partName, Partition partition, ModelPart part, List<SingleGroupVertexBuilder> vertices, Map<MeshPartDefinition, IntList> indices, List<String> path, PartTransformer.IndexCounter indexCounter, boolean bindPart, boolean cleanCut) {
        PartPose initialPose = part.getInitialPose();

        poseStack.pushPose();
        poseStack.translate(initialPose.x, initialPose.y, initialPose.z);
        poseStack.mulPose(new Quaternionf().rotationZYX(initialPose.zRot, initialPose.yRot, initialPose.xRot));

        if (!bindPart) {
            poseStack.scale(part.xScale, part.yScale, part.zScale);
        }

        List<String> newPath = new ArrayList<>(path);

        if (bindPart) {
            newPath.add(partName);
        }

        if (part.visible && !part.skipDraw) {
            MeshPartDefinition partDefinition = VanillaModelTransformer.VanillaMeshPartDefinition.of(partName);

            if (bindPart) {
                OpenMatrix4f invertedParentTransform = OpenMatrix4f.importFromMojangMatrix(poseStack.last().pose());
                invertedParentTransform.m30 *= 0.0625F;
                invertedParentTransform.m31 *= 0.0625F;
                invertedParentTransform.m32 *= 0.0625F;
                invertedParentTransform.invert();
                partDefinition = VanillaModelTransformer.VanillaMeshPartDefinition.of(partName, newPath, invertedParentTransform, partition.part());
            }

            for (ModelPart.Cube cube : part.cubes) {
                float[] bounds = getBounds(poseStack, cube);

                if (partition.transformer() == CHEST && isRotated(poseStack)) {
                    // A rotated cube on the torso is attached to the joint of its height
                    int joint = centerY(bounds) < WAIST_HEIGHT ? TORSO_JOINT : CHEST_JOINT;
                    bakeRigidCube(poseStack, partDefinition, cube, vertices, indices, indexCounter, joint);
                } else if (bindPart && isArm(partition) && bounds[2] < ELBOW_HEIGHT && bounds[3] > ELBOW_HEIGHT) {
                    // An accessory crossing the elbow must not bend, it follows the half of the arm where it is
                    boolean upper = centerY(bounds) >= ELBOW_HEIGHT;
                    boolean right = partition.transformer() == RIGHT_ARM;
                    int joint = right ? (upper ? RIGHT_UPPER_ARM_JOINT : RIGHT_LOWER_ARM_JOINT) : (upper ? LEFT_UPPER_ARM_JOINT : LEFT_LOWER_ARM_JOINT);
                    bakeRigidCube(poseStack, partDefinition, cube, vertices, indices, indexCounter, joint);
                } else if (bindPart && isLeg(partition) && bounds[2] < KNEE_HEIGHT && bounds[3] > KNEE_HEIGHT) {
                    // Same for an accessory crossing the knee
                    boolean upper = centerY(bounds) >= KNEE_HEIGHT;
                    boolean right = partition.transformer() == RIGHT_LEG;
                    int joint = right ? (upper ? RIGHT_UPPER_LEG_JOINT : RIGHT_LOWER_LEG_JOINT) : (upper ? LEFT_UPPER_LEG_JOINT : LEFT_LOWER_LEG_JOINT);
                    bakeRigidCube(poseStack, partDefinition, cube, vertices, indices, indexCounter, joint);
                } else if (cleanCut && !bindPart && (isArm(partition) || isLeg(partition))) {
                    // The limb itself gets a progressive joint
                    boolean arm = isArm(partition);
                    boolean right = partition.transformer() == RIGHT_ARM || partition.transformer() == RIGHT_LEG;
                    int upper = arm ? (right ? RIGHT_UPPER_ARM_JOINT : LEFT_UPPER_ARM_JOINT) : (right ? RIGHT_UPPER_LEG_JOINT : LEFT_UPPER_LEG_JOINT);
                    int lower = arm ? (right ? RIGHT_LOWER_ARM_JOINT : LEFT_LOWER_ARM_JOINT) : (right ? RIGHT_LOWER_LEG_JOINT : LEFT_LOWER_LEG_JOINT);
                    bakeBlendedCube(poseStack, partDefinition, cube, vertices, indices, indexCounter, arm ? ELBOW_HEIGHT : KNEE_HEIGHT, upper, lower);
                } else {
                    partition.transformer().bakeCube(poseStack, partDefinition, cube, vertices, indices, indexCounter);
                }
            }
        }

        for (Map.Entry<String, ModelPart> child : part.children.entrySet()) {
            bakePart(poseStack, child.getKey(), partition, child.getValue(), vertices, indices, newPath, indexCounter, true, cleanCut);
        }

        poseStack.popPose();
    }

    /**
     * Bakes the cube with a progressive joint: the faces are cut in thin slices around the given height, and the weight of
     * each vertex goes smoothly from the upper joint to the lower joint. Unlike Epic Fight, no single strip is stretched
     * between the two halves, and unlike a clean cut, the two halves stay connected when the limb bends.
     */
    private static void bakeBlendedCube(PoseStack poseStack, MeshPartDefinition partDefinition, ModelPart.Cube cube, List<SingleGroupVertexBuilder> vertices, Map<MeshPartDefinition, IntList> indices, PartTransformer.IndexCounter indexCounter, float cutHeight, int upperJoint, int lowerJoint) {
        Matrix4f matrix = poseStack.last().pose();
        IntList triangles = indices.computeIfAbsent(partDefinition, key -> new IntArrayList());
        float bottom = cutHeight - BLEND_HALF_HEIGHT;

        for (ModelPart.Polygon polygon : cube.polygons) {
            Vector3f normal = new Vector3f(polygon.normal);
            normal.mul(poseStack.last().normal());

            List<float[]> points = new ArrayList<>();

            for (ModelPart.Vertex vertex : polygon.vertices) {
                Vector4f position = new Vector4f(vertex.pos, 1.0F).mul(matrix);
                points.add(new float[]{position.x(), position.y(), position.z(), vertex.u, vertex.v});
            }

            // Cut the polygon at every slice limit
            List<List<float[]>> pieces = new ArrayList<>();
            pieces.add(points);

            for (int step = 0; step <= BLEND_STEPS; step++) {
                float height = bottom + 2.0F * BLEND_HALF_HEIGHT * step / BLEND_STEPS;
                List<List<float[]>> cutPieces = new ArrayList<>();

                for (List<float[]> piece : pieces) {
                    List<float[]> above = new ArrayList<>();
                    List<float[]> below = new ArrayList<>();
                    splitAt(piece, height, above, below);
                    addIfPolygon(cutPieces, above);
                    addIfPolygon(cutPieces, below);
                }

                pieces = cutPieces;
            }

            for (List<float[]> piece : pieces) {
                addBlendedPolygon(piece, normal, bottom, upperJoint, lowerJoint, vertices, triangles);
            }
        }

        // Epic Fight expects its counter to be the number of vertices already added
        try {
            INDEX_COUNTER_FIELD.setInt(indexCounter, vertices.size());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void addIfPolygon(List<List<float[]>> pieces, List<float[]> piece) {
        if (piece.size() >= 3) {
            pieces.add(piece);
        }
    }

    /**
     * Splits a convex polygon with the plane y = height. The points are {x, y, z, u, v}.
     */
    private static void splitAt(List<float[]> points, float height, List<float[]> above, List<float[]> below) {
        for (int i = 0; i < points.size(); i++) {
            float[] a = points.get(i);
            float[] b = points.get((i + 1) % points.size());

            if (a[1] >= height) {
                above.add(a);
            }

            if (a[1] <= height) {
                below.add(a);
            }

            if ((a[1] - height) * (b[1] - height) < 0.0F) {
                float t = (height - a[1]) / (b[1] - a[1]);
                float[] cut = new float[5];

                for (int k = 0; k < 5; k++) {
                    cut[k] = a[k] + (b[k] - a[k]) * t;
                }

                above.add(cut);
                below.add(cut);
            }
        }
    }

    private static void addBlendedPolygon(List<float[]> points, Vector3f normal, float bottom, int upperJoint, int lowerJoint, List<SingleGroupVertexBuilder> vertices, IntList triangles) {
        int first = vertices.size();

        for (float[] point : points) {
            float upperWeight = Math.max(0.0F, Math.min(1.0F, (point[1] - bottom) / (2.0F * BLEND_HALF_HEIGHT)));
            SingleGroupVertexBuilder vertex = new SingleGroupVertexBuilder()
                    .setPosition(new Vec3f(point[0], point[1], point[2]).scale(0.0625F))
                    .setNormal(new Vec3f(normal.x(), normal.y(), normal.z()))
                    .setTextureCoordinate(new Vec2f(point[3], point[4]));

            if (upperWeight >= 1.0F) {
                vertex.setEffectiveJointIDs(new Vec3f(upperJoint, 0.0F, 0.0F)).setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F)).setEffectiveJointNumber(1);
            } else if (upperWeight <= 0.0F) {
                vertex.setEffectiveJointIDs(new Vec3f(lowerJoint, 0.0F, 0.0F)).setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F)).setEffectiveJointNumber(1);
            } else {
                vertex.setEffectiveJointIDs(new Vec3f(upperJoint, lowerJoint, 0.0F)).setEffectiveJointWeights(new Vec3f(upperWeight, 1.0F - upperWeight, 0.0F)).setEffectiveJointNumber(2);
            }

            vertices.add(vertex);
        }

        // Each index is written three times: position, texture coordinate and normal
        for (int i = 1; i < points.size() - 1; i++) {
            for (int index : new int[]{first, first + i, first + i + 1}) {
                triangles.add(index);
                triangles.add(index);
                triangles.add(index);
            }
        }
    }

    /**
     * Bakes the cube without splitting it, and attaches all its vertices to the given joint.
     */
    private static void bakeRigidCube(PoseStack poseStack, MeshPartDefinition partDefinition, ModelPart.Cube cube, List<SingleGroupVertexBuilder> vertices, Map<MeshPartDefinition, IntList> indices, PartTransformer.IndexCounter indexCounter, int joint) {
        int firstVertex = vertices.size();
        RIGID.bakeCube(poseStack, partDefinition, cube, vertices, indices, indexCounter);

        for (int i = firstVertex; i < vertices.size(); i++) {
            vertices.get(i).setEffectiveJointIDs(new Vec3f(joint, 0.0F, 0.0F));
        }
    }

    /**
     * @return the bounds of the cube once placed on the body, as {minX, maxX, minY, maxY} in model pixels.
     */
    private static float[] getBounds(PoseStack poseStack, ModelPart.Cube cube) {
        Matrix4f matrix = poseStack.last().pose();
        float[] bounds = {Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};

        for (ModelPart.Polygon polygon : cube.polygons) {
            for (ModelPart.Vertex vertex : polygon.vertices) {
                Vector4f position = new Vector4f(vertex.pos, 1.0F).mul(matrix);
                bounds[0] = Math.min(bounds[0], position.x());
                bounds[1] = Math.max(bounds[1], position.x());
                bounds[2] = Math.min(bounds[2], position.y());
                bounds[3] = Math.max(bounds[3], position.y());
            }
        }

        return bounds;
    }

    private static float centerY(float[] bounds) {
        return (bounds[2] + bounds[3]) * 0.5F;
    }

    /**
     * True when the cube is not aligned with the axes once placed on the body. Epic Fight is not able to split these cubes correctly.
     */
    private static boolean isRotated(PoseStack poseStack) {
        return !isAxisAligned(poseStack.last().pose());
    }

    private static boolean isLeg(Partition partition) {
        return partition.transformer() == RIGHT_LEG || partition.transformer() == LEFT_LEG;
    }

    private static boolean isArm(Partition partition) {
        return partition.transformer() == RIGHT_ARM || partition.transformer() == LEFT_ARM;
    }

    private static boolean isAxisAligned(Matrix4f matrix) {
        return Math.abs(matrix.m01()) < AXIS_ALIGNED_EPSILON && Math.abs(matrix.m02()) < AXIS_ALIGNED_EPSILON
                && Math.abs(matrix.m10()) < AXIS_ALIGNED_EPSILON && Math.abs(matrix.m12()) < AXIS_ALIGNED_EPSILON
                && Math.abs(matrix.m20()) < AXIS_ALIGNED_EPSILON && Math.abs(matrix.m21()) < AXIS_ALIGNED_EPSILON;
    }
}
