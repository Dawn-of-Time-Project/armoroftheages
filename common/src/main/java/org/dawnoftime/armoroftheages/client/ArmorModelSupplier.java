package org.dawnoftime.armoroftheages.client;

import net.minecraft.client.model.geom.ModelPart;
import org.dawnoftime.armoroftheages.client.models.ArmorModel;

public interface ArmorModelSupplier {
    ArmorModel create(ModelPart root, boolean isSlim);
}
