package com.tacz.guns.api.client.animation.gltf;

public class GltfConstants {
   public static final int GL_BGR = 32992;
   public static final int GL_RGB = 6407;
   public static final int GL_RGBA = 6408;
   public static final int GL_BGRA = 32993;
   public static final int GL_BYTE = 5120;
   public static final int GL_UNSIGNED_BYTE = 5121;
   public static final int GL_SHORT = 5122;
   public static final int GL_UNSIGNED_SHORT = 5123;
   public static final int GL_INT = 5124;
   public static final int GL_UNSIGNED_INT = 5125;
   public static final int GL_FLOAT = 5126;
   public static final int GL_FLOAT_VEC2 = 35664;
   public static final int GL_FLOAT_VEC3 = 35665;
   public static final int GL_FLOAT_VEC4 = 35666;
   public static final int GL_INT_VEC2 = 35667;
   public static final int GL_INT_VEC3 = 35668;
   public static final int GL_INT_VEC4 = 35669;
   public static final int GL_BOOL = 35670;
   public static final int GL_BOOL_VEC2 = 35671;
   public static final int GL_BOOL_VEC3 = 35672;
   public static final int GL_BOOL_VEC4 = 35673;
   public static final int GL_FLOAT_MAT2 = 35674;
   public static final int GL_FLOAT_MAT3 = 35675;
   public static final int GL_FLOAT_MAT4 = 35676;
   public static final int GL_SAMPLER_2D = 35678;
   public static final int GL_SAMPLER_CUBE = 35680;
   public static final int GL_POINTS = 0;
   public static final int GL_LINES = 1;
   public static final int GL_LINE_LOOP = 2;
   public static final int GL_LINE_STRIP = 3;
   public static final int GL_TRIANGLES = 4;
   public static final int GL_TRIANGLE_STRIP = 5;
   public static final int GL_TRIANGLE_FAN = 6;
   public static final int GL_VERTEX_SHADER = 35633;
   public static final int GL_FRAGMENT_SHADER = 35632;
   public static final int GL_TEXTURE_2D = 3553;
   public static final int GL_ARRAY_BUFFER = 34962;
   public static final int GL_ELEMENT_ARRAY_BUFFER = 34963;
   public static final int GL_BLEND = 3042;
   public static final int GL_CULL_FACE = 2884;
   public static final int GL_DEPTH_TEST = 2929;
   public static final int GL_POLYGON_OFFSET_FILL = 32823;
   public static final int GL_SAMPLE_ALPHA_TO_COVERAGE = 32926;
   public static final int GL_SCISSOR_TEST = 3089;
   public static final int GL_FUNC_ADD = 32774;
   public static final int GL_FUNC_SUBTRACT = 32778;
   public static final int GL_FUNC_REVERSE_SUBTRACT = 32779;
   public static final int GL_ZERO = 0;
   public static final int GL_ONE = 1;
   public static final int GL_SRC_COLOR = 768;
   public static final int GL_ONE_MINUS_SRC_COLOR = 769;
   public static final int GL_DST_COLOR = 774;
   public static final int GL_ONE_MINUS_DST_COLOR = 775;
   public static final int GL_SRC_ALPHA = 770;
   public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
   public static final int GL_DST_ALPHA = 772;
   public static final int GL_ONE_MINUS_DST_ALPHA = 773;
   public static final int GL_CONSTANT_COLOR = 32769;
   public static final int GL_ONE_MINUS_CONSTANT_COLOR = 32770;
   public static final int GL_CONSTANT_ALPHA = 32771;
   public static final int GL_ONE_MINUS_CONSTANT_ALPHA = 32772;
   public static final int GL_SRC_ALPHA_SATURATE = 776;
   public static final int GL_FRONT = 1028;
   public static final int GL_BACK = 1029;
   public static final int GL_FRONT_AND_BACK = 1032;
   public static final int GL_NEVER = 512;
   public static final int GL_LESS = 513;
   public static final int GL_LEQUAL = 515;
   public static final int GL_EQUAL = 514;
   public static final int GL_GREATER = 516;
   public static final int GL_NOTEQUAL = 517;
   public static final int GL_GEQUAL = 518;
   public static final int GL_ALWAYS = 519;
   public static final int GL_CW = 2304;
   public static final int GL_CCW = 2305;
   public static final int GL_NEAREST = 9728;
   public static final int GL_LINEAR = 9729;
   public static final int GL_NEAREST_MIPMAP_NEAREST = 9984;
   public static final int GL_LINEAR_MIPMAP_NEAREST = 9985;
   public static final int GL_NEAREST_MIPMAP_LINEAR = 9986;
   public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
   public static final int GL_REPEAT = 10497;
   public static final int GL_MIRRORED_REPEAT = 33648;
   public static final int GL_CLAMP_TO_EDGE = 33071;
   public static final int GL_CLAMP_TO_BORDER = 33069;

   private GltfConstants() {
   }

   public static String stringFor(int constant) {
      switch (constant) {
         case 0:
            return "GL_ZERO or GL_POINTS";
         case 1:
            return "GL_ONE or GL_LINES";
         case 2:
            return "GL_LINE_LOOP";
         case 3:
            return "GL_LINE_STRIP";
         case 4:
            return "GL_TRIANGLES";
         case 5:
            return "GL_TRIANGLE_STRIP";
         case 512:
            return "GL_NEVER";
         case 513:
            return "GL_LESS";
         case 514:
            return "GL_EQUAL";
         case 515:
            return "GL_LEQUAL";
         case 516:
            return "GL_GREATER";
         case 517:
            return "GL_NOTEQUAL";
         case 518:
            return "GL_GEQUAL";
         case 519:
            return "GL_ALWAYS";
         case 768:
            return "GL_SRC_COLOR";
         case 769:
            return "GL_ONE_MINUS_SRC_COLOR";
         case 770:
            return "GL_SRC_ALPHA";
         case 771:
            return "GL_ONE_MINUS_SRC_ALPHA";
         case 772:
            return "GL_DST_ALPHA";
         case 773:
            return "GL_ONE_MINUS_DST_ALPHA";
         case 774:
            return "GL_DST_COLOR";
         case 775:
            return "GL_ONE_MINUS_DST_COLOR";
         case 776:
            return "GL_SRC_ALPHA_SATURATE";
         case 1028:
            return "GL_FRONT";
         case 1029:
            return "GL_BACK";
         case 1032:
            return "GL_FRONT_AND_BACK";
         case 2304:
            return "GL_CW";
         case 2305:
            return "GL_CCW";
         case 2884:
            return "GL_CULL_FACE";
         case 2929:
            return "GL_DEPTH_TEST";
         case 3042:
            return "GL_BLEND";
         case 3089:
            return "GL_SCISSOR_TEST";
         case 3553:
            return "GL_TEXTURE_2D";
         case 5120:
            return "GL_BYTE";
         case 5121:
            return "GL_UNSIGNED_BYTE";
         case 5122:
            return "GL_SHORT";
         case 5123:
            return "GL_UNSIGNED_SHORT";
         case 5124:
            return "GL_INT";
         case 5125:
            return "GL_UNSIGNED_INT";
         case 5126:
            return "GL_FLOAT";
         case 6407:
            return "GL_RGB";
         case 6408:
            return "GL_RGBA";
         case 9728:
            return "GL_NEAREST";
         case 9729:
            return "GL_LINEAR";
         case 9984:
            return "GL_NEAREST_MIPMAP_NEAREST";
         case 9985:
            return "GL_LINEAR_MIPMAP_NEAREST";
         case 9986:
            return "GL_NEAREST_MIPMAP_LINEAR";
         case 9987:
            return "GL_LINEAR_MIPMAP_LINEAR";
         case 10497:
            return "GL_REPEAT";
         case 32769:
            return "GL_CONSTANT_COLOR";
         case 32770:
            return "GL_ONE_MINUS_CONSTANT_COLOR";
         case 32771:
            return "GL_CONSTANT_ALPHA";
         case 32772:
            return "GL_ONE_MINUS_CONSTANT_ALPHA";
         case 32774:
            return "GL_FUNC_ADD";
         case 32778:
            return "GL_FUNC_SUBTRACT";
         case 32779:
            return "GL_FUNC_REVERSE_SUBTRACT";
         case 32823:
            return "GL_POLYGON_OFFSET_FILL";
         case 32926:
            return "GL_SAMPLE_ALPHA_TO_COVERAGE";
         case 32992:
            return "GL_BGR";
         case 32993:
            return "GL_BGRA";
         case 33069:
            return "GL_CLAMP_TO_BORDER";
         case 33071:
            return "GL_CLAMP_TO_EDGE";
         case 33648:
            return "GL_MIRRORED_REPEAT";
         case 34962:
            return "GL_ARRAY_BUFFER";
         case 34963:
            return "GL_ELEMENT_ARRAY_BUFFER";
         case 35632:
            return "GL_FRAGMENT_SHADER";
         case 35633:
            return "GL_VERTEX_SHADER";
         case 35664:
            return "GL_FLOAT_VEC2";
         case 35665:
            return "GL_FLOAT_VEC3";
         case 35666:
            return "GL_FLOAT_VEC4";
         case 35667:
            return "GL_INT_VEC2";
         case 35668:
            return "GL_INT_VEC3";
         case 35669:
            return "GL_INT_VEC4";
         case 35670:
            return "GL_BOOL";
         case 35671:
            return "GL_BOOL_VEC2";
         case 35672:
            return "GL_BOOL_VEC3";
         case 35673:
            return "GL_BOOL_VEC4";
         case 35674:
            return "GL_FLOAT_MAT2";
         case 35675:
            return "GL_FLOAT_MAT3";
         case 35676:
            return "GL_FLOAT_MAT4";
         case 35678:
            return "GL_SAMPLER_2D";
         default:
            return "UNKNOWN_GL_CONSTANT[" + constant + "]";
      }
   }
}
