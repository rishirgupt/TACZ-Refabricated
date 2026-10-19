package com.tacz.guns.api.client.animation.gltf;

import com.tacz.guns.api.client.animation.gltf.accessor.AccessorData;
import com.tacz.guns.api.client.animation.gltf.accessor.AccessorDatas;
import com.tacz.guns.api.client.animation.gltf.accessor.AccessorSparseUtils;
import com.tacz.guns.client.resource.pojo.animation.gltf.Accessor;
import com.tacz.guns.client.resource.pojo.animation.gltf.AccessorSparse;
import com.tacz.guns.client.resource.pojo.animation.gltf.AccessorSparseIndices;
import com.tacz.guns.client.resource.pojo.animation.gltf.AccessorSparseValues;
import com.tacz.guns.client.resource.pojo.animation.gltf.Animation;
import com.tacz.guns.client.resource.pojo.animation.gltf.AnimationChannel;
import com.tacz.guns.client.resource.pojo.animation.gltf.AnimationChannelTarget;
import com.tacz.guns.client.resource.pojo.animation.gltf.AnimationSampler;
import com.tacz.guns.client.resource.pojo.animation.gltf.Buffer;
import com.tacz.guns.client.resource.pojo.animation.gltf.BufferView;
import com.tacz.guns.client.resource.pojo.animation.gltf.Node;
import com.tacz.guns.client.resource.pojo.animation.gltf.RawAnimationStructure;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AnimationStructure {
   private final List<AccessorModel> accessorModels = new ArrayList<>();
   private final List<AnimationModel> animationModels = new ArrayList<>();
   private final List<BufferModel> bufferModels = new ArrayList<>();
   private final List<BufferViewModel> bufferViewModels = new ArrayList<>();
   private final List<NodeModel> nodeModels = new ArrayList<>();
   private final RawAnimationStructure gltf;

   public AnimationStructure(RawAnimationStructure asset) {
      this.gltf = asset;
      this.createAccessorModels();
      this.createAnimationModels();
      this.createBufferModels();
      this.createBufferViewModels();
      this.createNodeModels();
      this.initBufferModels();
      this.initBufferViewModels();
      this.initAccessorModels();
      this.initAnimationModels();
      this.initNodeModels();
   }

   private static float[] clone(float[] array) {
      return array == null ? null : (float[])array.clone();
   }

   private static BufferViewModel createBufferViewModel(String uriString, ByteBuffer bufferData) {
      BufferModel bufferModel = new BufferModel();
      bufferModel.setUri(uriString);
      bufferModel.setBufferData(bufferData);
      BufferViewModel bufferViewModel = new BufferViewModel(null);
      bufferViewModel.setByteOffset(0);
      bufferViewModel.setByteLength(bufferData.capacity());
      bufferViewModel.setBufferModel(bufferModel);
      return bufferViewModel;
   }

   private static BufferViewModel createBufferViewModel(BufferView bufferView) {
      int byteOffset = bufferView.getByteOffset() == null ? 0 : bufferView.getByteOffset();
      int byteLength = bufferView.getByteLength();
      Integer byteStride = bufferView.getByteStride();
      Integer target = bufferView.getTarget();
      BufferViewModel bufferViewModel = new BufferViewModel(target);
      bufferViewModel.setByteOffset(byteOffset);
      bufferViewModel.setByteLength(byteLength);
      bufferViewModel.setByteStride(byteStride);
      return bufferViewModel;
   }

   private static boolean isDataUriString(String uriString) {
      if (uriString == null) {
         return false;
      }

      try {
         URI uri = new URI(uriString);
         return isDataUri(uri);
      } catch (URISyntaxException e) {
         return false;
      }
   }

   private static boolean isDataUri(URI uri) {
      return "data".equalsIgnoreCase(uri.getScheme());
   }

   public static byte[] readDataUri(String uriString) {
      String encoding = "base64,";
      int encodingIndex = uriString.indexOf(encoding);
      if (encodingIndex < 0) {
         throw new IllegalArgumentException("The given URI string is not a base64 encoded data URI string: " + uriString);
      }

      int contentStartIndex = encodingIndex + encoding.length();
      return Base64.getDecoder().decode(uriString.substring(contentStartIndex));
   }

   public List<BufferModel> getBufferModels() {
      return Collections.unmodifiableList(this.bufferModels);
   }

   public List<AccessorModel> getAccessorModels() {
      return this.accessorModels;
   }

   public List<AnimationModel> getAnimationModels() {
      return this.animationModels;
   }

   public List<BufferViewModel> getBufferViewModels() {
      return this.bufferViewModels;
   }

   public List<NodeModel> getNodeModels() {
      return this.nodeModels;
   }

   private void createBufferModels() {
      List<Buffer> buffers = this.gltf.getBuffers() == null ? Collections.emptyList() : this.gltf.getBuffers();

      for (int i = 0; i < buffers.size(); i++) {
         Buffer buffer = buffers.get(i);
         BufferModel bufferModel = new BufferModel();
         bufferModel.setUri(buffer.getUri());
         this.bufferModels.add(bufferModel);
      }
   }

   private void initBufferModels() {
      List<Buffer> buffers = this.gltf.getBuffers() == null ? Collections.emptyList() : this.gltf.getBuffers();

      for (int i = 0; i < buffers.size(); i++) {
         Buffer buffer = buffers.get(i);
         BufferModel bufferModel = this.bufferModels.get(i);
         String uri = buffer.getUri();
         if (isDataUriString(uri)) {
            byte[] data = readDataUri(uri);
            ByteBuffer bufferData = Buffers.create(data);
            bufferModel.setBufferData(bufferData);
         }
      }
   }

   private void createAccessorModels() {
      for (Accessor accessor : this.gltf.getAccessors() == null ? Collections.<Accessor>emptyList() : this.gltf.getAccessors()) {
         Integer componentType = accessor.getComponentType();
         Integer count = accessor.getCount();
         ElementType elementType = ElementType.forString(accessor.getType());
         AccessorModel accessorModel = new AccessorModel(componentType, count, elementType);
         this.accessorModels.add(accessorModel);
      }
   }

   private void initAccessorModels() {
      List<Accessor> accessors = this.gltf.getAccessors() == null ? Collections.emptyList() : this.gltf.getAccessors();

      for (int i = 0; i < accessors.size(); i++) {
         Accessor accessor = accessors.get(i);
         AccessorModel accessorModel = this.accessorModels.get(i);
         int byteOffset = accessor.getByteOffset() == null ? 0 : accessor.getByteOffset();
         accessorModel.setByteOffset(byteOffset);
         AccessorSparse accessorSparse = accessor.getSparse();
         if (accessorSparse == null) {
            this.initDenseAccessorModel(i, accessor, accessorModel);
         } else {
            this.initSparseAccessorModel(i, accessor, accessorModel);
         }
      }
   }

   private void createAnimationModels() {
      List<Animation> animations = this.gltf.getAnimations() == null ? Collections.emptyList() : this.gltf.getAnimations();

      for (int i = 0; i < animations.size(); i++) {
         this.animationModels.add(new AnimationModel());
      }
   }

   private void initAnimationModels() {
      List<Animation> animations = this.gltf.getAnimations() == null ? Collections.emptyList() : this.gltf.getAnimations();

      for (int i = 0; i < animations.size(); i++) {
         Animation animation = animations.get(i);
         AnimationModel animationModel = this.animationModels.get(i);
         animationModel.setName(animation.getName());

         for (AnimationChannel animationChannel : animation.getChannels()) {
            AnimationModel.Channel channel = this.createChannel(animation, animationChannel);
            animationModel.addChannel(channel);
         }
      }
   }

   private void createBufferViewModels() {
      for (BufferView bufferView : this.gltf.getBufferViews() == null ? Collections.<BufferView>emptyList() : this.gltf.getBufferViews()) {
         BufferViewModel bufferViewModel = createBufferViewModel(bufferView);
         this.bufferViewModels.add(bufferViewModel);
      }
   }

   private void initBufferViewModels() {
      List<BufferView> bufferViews = this.gltf.getBufferViews() == null ? Collections.emptyList() : this.gltf.getBufferViews();

      for (int i = 0; i < bufferViews.size(); i++) {
         BufferView bufferView = bufferViews.get(i);
         BufferViewModel bufferViewModel = this.bufferViewModels.get(i);
         int bufferIndex = bufferView.getBuffer();
         BufferModel bufferModel = this.bufferModels.get(bufferIndex);
         bufferViewModel.setBufferModel(bufferModel);
      }
   }

   private void createNodeModels() {
      List<Node> nodes = this.gltf.getNodes() == null ? Collections.emptyList() : this.gltf.getNodes();

      for (int i = 0; i < nodes.size(); i++) {
         this.nodeModels.add(new NodeModel());
      }
   }

   private void initNodeModels() {
      List<Node> nodes = this.gltf.getNodes() == null ? Collections.emptyList() : this.gltf.getNodes();

      for (int i = 0; i < nodes.size(); i++) {
         Node node = nodes.get(i);
         NodeModel nodeModel = this.nodeModels.get(i);
         nodeModel.setName(node.getName());

         for (Integer childIndex : node.getChildren() == null ? Collections.<Integer>emptyList() : node.getChildren()) {
            NodeModel child = this.nodeModels.get(childIndex);
            nodeModel.addChild(child);
         }

         float[] matrix = node.getMatrix();
         float[] translation = node.getTranslation();
         float[] rotation = node.getRotation();
         float[] scale = node.getScale();
         nodeModel.setMatrix(clone(matrix));
         nodeModel.setTranslation(clone(translation));
         nodeModel.setRotation(clone(rotation));
         nodeModel.setScale(clone(scale));
      }
   }

   private AnimationModel.Channel createChannel(Animation animation, AnimationChannel animationChannel) {
      List<AnimationSampler> samplers = animation.getSamplers();
      int samplerIndex = animationChannel.getSampler();
      AnimationSampler animationSampler = samplers.get(samplerIndex);
      int inputAccessorIndex = animationSampler.getInput();
      AccessorModel inputAccessorModel = this.accessorModels.get(inputAccessorIndex);
      int outputAccessorIndex = animationSampler.getOutput();
      AccessorModel outputAccessorModel = this.accessorModels.get(outputAccessorIndex);
      String interpolationString = animationSampler.getInterpolation();
      AnimationModel.Interpolation interpolation = interpolationString == null
         ? AnimationModel.Interpolation.LINEAR
         : AnimationModel.Interpolation.valueOf(interpolationString);
      AnimationModel.Sampler sampler = new AnimationModel.Sampler(inputAccessorModel, interpolation, outputAccessorModel);
      AnimationChannelTarget animationChannelTarget = animationChannel.getTarget();
      Integer nodeIndex = animationChannelTarget.getNode();
      NodeModel nodeModel = null;
      if (nodeIndex != null) {
         nodeModel = this.nodeModels.get(nodeIndex);
      }

      String path = animationChannelTarget.getPath();
      return new AnimationModel.Channel(sampler, nodeModel, path);
   }

   private void initDenseAccessorModel(int accessorIndex, Accessor accessor, AccessorModel accessorModel) {
      Integer bufferViewIndex = accessor.getBufferView();
      if (bufferViewIndex != null) {
         BufferViewModel bufferViewModel = this.bufferViewModels.get(bufferViewIndex);
         accessorModel.setBufferViewModel(bufferViewModel);
      } else {
         int count = accessorModel.getCount();
         int elementSizeInBytes = accessorModel.getElementSizeInBytes();
         int byteLength = elementSizeInBytes * count;
         ByteBuffer bufferData = Buffers.create(byteLength);
         String uriString = "buffer_for_accessor" + accessorIndex + ".bin";
         BufferViewModel bufferViewModel = createBufferViewModel(uriString, bufferData);
         accessorModel.setBufferViewModel(bufferViewModel);
      }

      BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
      Integer byteStride = bufferViewModel.getByteStride();
      if (byteStride == null) {
         accessorModel.setByteStride(accessorModel.getElementSizeInBytes());
      } else {
         accessorModel.setByteStride(byteStride);
      }
   }

   private void initSparseAccessorModel(int accessorIndex, Accessor accessor, AccessorModel accessorModel) {
      int count = accessorModel.getCount();
      int elementSizeInBytes = accessorModel.getElementSizeInBytes();
      int byteLength = elementSizeInBytes * count;
      ByteBuffer bufferData = Buffers.create(byteLength);
      String uriString = "buffer_for_accessor" + accessorIndex + ".bin";
      BufferViewModel denseBufferViewModel = createBufferViewModel(uriString, bufferData);
      accessorModel.setBufferViewModel(denseBufferViewModel);
      accessorModel.setByteOffset(0);
      Integer bufferViewIndex = accessor.getBufferView();
      if (bufferViewIndex != null) {
         Consumer<ByteBuffer> sparseSubstitutionCallback = denseByteBuffer -> {
            BufferViewModel baseBufferViewModel = this.bufferViewModels.get(bufferViewIndex);
            ByteBuffer baseBufferViewData = baseBufferViewModel.getBufferViewData();
            AccessorData baseAccessorData = AccessorDatas.create(accessorModel, baseBufferViewData);
            AccessorData denseAccessorData = AccessorDatas.create(accessorModel, bufferData);
            this.substituteSparseAccessorData(accessor, accessorModel, denseAccessorData, baseAccessorData);
         };
         denseBufferViewModel.setSparseSubstitutionCallback(sparseSubstitutionCallback);
      } else {
         Consumer<ByteBuffer> sparseSubstitutionCallback = denseByteBuffer -> {
            AccessorData denseAccessorData = AccessorDatas.create(accessorModel, bufferData);
            this.substituteSparseAccessorData(accessor, accessorModel, denseAccessorData, null);
         };
         denseBufferViewModel.setSparseSubstitutionCallback(sparseSubstitutionCallback);
      }
   }

   private void substituteSparseAccessorData(Accessor accessor, AccessorModel accessorModel, AccessorData denseAccessorData, AccessorData baseAccessorData) {
      AccessorSparse accessorSparse = accessor.getSparse();
      int count = accessorSparse.getCount();
      AccessorSparseIndices accessorSparseIndices = accessorSparse.getIndices();
      AccessorData sparseIndicesAccessorData = this.createSparseIndicesAccessorData(accessorSparseIndices, count);
      AccessorSparseValues accessorSparseValues = accessorSparse.getValues();
      ElementType elementType = accessorModel.getElementType();
      AccessorData sparseValuesAccessorData = this.createSparseValuesAccessorData(
         accessorSparseValues, accessorModel.getComponentType(), elementType.getNumComponents(), count
      );
      AccessorSparseUtils.substituteAccessorData(denseAccessorData, baseAccessorData, sparseIndicesAccessorData, sparseValuesAccessorData);
   }

   private AccessorData createSparseIndicesAccessorData(AccessorSparseIndices accessorSparseIndices, int count) {
      Integer componentType = accessorSparseIndices.getComponentType();
      Integer bufferViewIndex = accessorSparseIndices.getBufferView();
      BufferViewModel bufferViewModel = this.bufferViewModels.get(bufferViewIndex);
      ByteBuffer bufferViewData = bufferViewModel.getBufferViewData();
      int byteOffset = accessorSparseIndices.getByteOffset() == null ? 0 : accessorSparseIndices.getByteOffset();
      return AccessorDatas.create(componentType, bufferViewData, byteOffset, count, 1, null);
   }

   private AccessorData createSparseValuesAccessorData(AccessorSparseValues accessorSparseValues, int componentType, int numComponentsPerElement, int count) {
      Integer bufferViewIndex = accessorSparseValues.getBufferView();
      BufferViewModel bufferViewModel = this.bufferViewModels.get(bufferViewIndex);
      ByteBuffer bufferViewData = bufferViewModel.getBufferViewData();
      int byteOffset = accessorSparseValues.getByteOffset() == null ? 0 : accessorSparseValues.getByteOffset();
      return AccessorDatas.create(componentType, bufferViewData, byteOffset, count, numComponentsPerElement, null);
   }
}
