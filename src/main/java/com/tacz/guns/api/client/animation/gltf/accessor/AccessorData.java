package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;

public interface AccessorData {
   Class<?> getComponentType();

   int getNumElements();

   int getNumComponentsPerElement();

   int getTotalNumComponents();

   ByteBuffer createByteBuffer();
}
