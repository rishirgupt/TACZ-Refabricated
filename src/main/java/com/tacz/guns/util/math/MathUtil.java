package com.tacz.guns.util.math;

import javax.annotation.Nonnull;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {
   public static final float[] QUATERNION_ONE = new float[]{0.0F, 0.0F, 0.0F, 1.0F};

   public static double magnificationToFovMultiplier(double magnification, double currentFov) {
      return magnificationToFov(magnification, currentFov) / currentFov;
   }

   public static double magnificationToFov(double magnification, double currentFov) {
      double currentTan = Math.tan(Math.toRadians(currentFov / 2.0));
      double newTan = currentTan / magnification;
      return Math.toDegrees(Math.atan(newTan)) * 2.0;
   }

   public static double fovToMagnification(double currentFov, double originFov) {
      return Math.tan(Math.toRadians(originFov / 2.0)) / Math.tan(Math.toRadians(currentFov / 2.0));
   }

   public static double zoomSensitivityRatio(double currentFov, double originFov, double coefficient) {
      return Math.atan(Math.tan(Math.toRadians(currentFov / 2.0)) * coefficient) / Math.atan(Math.tan(Math.toRadians(originFov / 2.0)) * coefficient);
   }

   public static double copySign(double magnitude, double sign) {
      return Math.abs(magnitude) * (sign < 0.0 ? -1 : 1);
   }

   public static float[] toQuaternion(float pitch, float yaw, float roll) {
      double cy = Math.cos(roll * 0.5);
      double sy = Math.sin(roll * 0.5);
      double cp = Math.cos(yaw * 0.5);
      double sp = Math.sin(yaw * 0.5);
      double cr = Math.cos(pitch * 0.5);
      double sr = Math.sin(pitch * 0.5);
      return new float[]{
         (float)(cy * cp * sr - sy * sp * cr), (float)(sy * cp * sr + cy * sp * cr), (float)(sy * cp * cr - cy * sp * sr), (float)(cy * cp * cr + sy * sp * sr)
      };
   }

   public static void toQuaternion(float pitch, float yaw, float roll, @Nonnull Quaternionf quaternion) {
      double cy = Math.cos(roll * 0.5);
      double sy = Math.sin(roll * 0.5);
      double cp = Math.cos(yaw * 0.5);
      double sp = Math.sin(yaw * 0.5);
      double cr = Math.cos(pitch * 0.5);
      double sr = Math.sin(pitch * 0.5);
      quaternion.set(
         (float)(cy * cp * sr - sy * sp * cr), (float)(sy * cp * sr + cy * sp * cr), (float)(sy * cp * cr - cy * sp * sr), (float)(cy * cp * cr + sy * sp * sr)
      );
   }

   public static float[] toEulerAngles(Quaternionf q) {
      float[] angles = new float[3];
      double sinrCosp = 2.0F * (q.w() * q.x() + q.y() * q.z());
      double cosrCosp = 1.0F - 2.0F * (q.x() * q.x() + q.y() * q.y());
      angles[0] = (float)Math.atan2(sinrCosp, cosrCosp);
      double sinp = 2.0F * (q.w() * q.y() - q.x() * q.z());
      if (Math.abs(sinp) >= 1.0) {
         angles[1] = (float)copySign(Math.PI / 2, sinp);
      } else {
         angles[1] = (float)Math.asin(sinp);
      }

      double sinyCosp = 2.0F * (q.w() * q.z() + q.y() * q.x());
      double cosyCosp = 1.0F - 2.0F * (q.y() * q.y() + q.z() * q.z());
      angles[2] = (float)Math.atan2(sinyCosp, cosyCosp);
      return angles;
   }

   public static float[] toEulerAngles(float[] q) {
      float[] angles = new float[3];
      double sinrCosp = 2.0F * (q[3] * q[0] + q[1] * q[2]);
      double cosrCosp = 1.0F - 2.0F * (q[0] * q[0] + q[1] * q[1]);
      angles[0] = (float)Math.atan2(sinrCosp, cosrCosp);
      double sinp = 2.0F * (q[3] * q[1] - q[2] * q[0]);
      if (Math.abs(sinp) >= 1.0) {
         angles[1] = (float)copySign(Math.PI / 2, sinp);
      } else {
         angles[1] = (float)Math.asin(sinp);
      }

      double sinyCosp = 2.0F * (q[3] * q[2] + q[1] * q[0]);
      double cosyCosp = 1.0F - 2.0F * (q[1] * q[1] + q[2] * q[2]);
      angles[2] = (float)Math.atan2(sinyCosp, cosyCosp);
      return angles;
   }

   public static double toDegreePositive(double angle) {
      while (angle < 0.0) {
         angle += Math.PI * 2;
      }

      return Math.toDegrees(angle);
   }

   public static float[] inverseQuaternion(float[] quaternion) {
      float[] result = new float[]{-quaternion[0], -quaternion[1], -quaternion[2], quaternion[3]};
      float m2 = quaternion[0] * quaternion[0] + quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2] + quaternion[3] * quaternion[3];
      result[0] /= m2;
      result[1] /= m2;
      result[2] /= m2;
      result[3] /= m2;
      return result;
   }

   public static float[] mulQuaternion(float[] q1, float[] q2) {
      return new float[]{
         Math.fma(q1[3], q2[0], Math.fma(q1[0], q2[3], Math.fma(q1[1], q2[2], -q1[2] * q2[1]))),
         Math.fma(q1[3], q2[1], Math.fma(-q1[0], q2[2], Math.fma(q1[1], q2[3], q1[2] * q2[0]))),
         Math.fma(q1[3], q2[2], Math.fma(q1[0], q2[1], Math.fma(-q1[1], q2[0], q1[2] * q2[3]))),
         Math.fma(q1[3], q2[3], Math.fma(-q1[0], q2[0], Math.fma(-q1[1], q2[1], -q1[2] * q2[2])))
      };
   }

   public static void blendQuaternion(Quaternionf to, Quaternionf from) {
      Quaternionf q1 = new Quaternionf(to);
      Quaternionf q2 = new Quaternionf(from);
      normalizeQuaternion(q1);
      normalizeQuaternion(q2);
      logQuaternion(q1);
      logQuaternion(q2);
      q1.set(q1.x() + q2.x(), q1.y() + q2.y(), q1.z() + q2.z(), q1.w() + q2.w());
      expQuaternion(q1);
      normalizeQuaternion(q1);
      to.set(q1.x(), q1.y(), q1.z(), q1.w());
   }

   public static void normalizeQuaternion(Quaternionf q) {
      float f = q.x() * q.x() + q.y() * q.y() + q.z() * q.z() + q.w() * q.w();
      if (f > 0.0F) {
         float f1 = (float)Mth.fastInvSqrt(f);
         q.set(f1 * q.x(), f1 * q.y(), f1 * q.z(), f1 * q.w());
      } else {
         q.set(0.0F, 0.0F, 0.0F, 1.0F);
      }
   }

   public static void logQuaternion(Quaternionf q) {
      double norm = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z() + q.w() * q.w());
      double vec = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
      double i = q.w() / norm;
      if (i > 1.0) {
         i = 1.0;
      }

      if (i < -1.0) {
         i = -1.0;
      }

      double theta = Math.acos(i);
      double factor = vec == 0.0 ? 0.0 : theta / vec;
      q.set((float)(q.x() * factor), (float)(q.y() * factor), (float)(q.z() * factor), (float)Math.log(norm));
   }

   public static void expQuaternion(Quaternionf q) {
      double magnitude = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
      double expW = Math.exp(q.w());
      double sinMagnitude = Math.sin(magnitude);
      double coef = magnitude == 0.0 ? 0.0 : expW * sinMagnitude / magnitude;
      q.set((float)(coef * q.x()), (float)(coef * q.y()), (float)(coef * q.z()), (float)(expW * Math.cos(magnitude)));
   }

   public static float[] slerp(float[] from, float[] to, float alpha) {
      float ax = from[0];
      float ay = from[1];
      float az = from[2];
      float aw = from[3];
      float bx = to[0];
      float by = to[1];
      float bz = to[2];
      float bw = to[3];
      float dot = ax * bx + ay * by + az * bz + aw * bw;
      if (dot < 0.0F) {
         bx = -bx;
         by = -by;
         bz = -bz;
         bw = -bw;
         dot = -dot;
      }

      float epsilon = 1.0E-6F;
      float s0;
      float s1;
      if (1.0 - dot > epsilon) {
         float omega = (float)Math.acos(dot);
         float invSinOmega = 1.0F / (float)Math.sin(omega);
         s0 = (float)Math.sin((1.0 - alpha) * omega) * invSinOmega;
         s1 = (float)Math.sin(alpha * omega) * invSinOmega;
      } else {
         s0 = 1.0F - alpha;
         s1 = alpha;
      }

      return new float[]{s0 * ax + s1 * bx, s0 * ay + s1 * by, s0 * az + s1 * bz, s0 * aw + s1 * bw};
   }

   public static Quaternionf toQuaternion(float[] q) {
      return new Quaternionf(q[0], q[1], q[2], q[3]);
   }

   public static Quaternionf slerp(Quaternionf from, Quaternionf to, float alpha) {
      float ax = from.x();
      float ay = from.y();
      float az = from.z();
      float aw = from.w();
      float bx = to.x();
      float by = to.y();
      float bz = to.z();
      float bw = to.w();
      float dot = ax * bx + ay * by + az * bz + aw * bw;
      if (dot < 0.0F) {
         bx = -bx;
         by = -by;
         bz = -bz;
         bw = -bw;
         dot = -dot;
      }

      float epsilon = 1.0E-6F;
      float s0;
      float s1;
      if (1.0 - dot > epsilon) {
         float omega = (float)Math.acos(dot);
         float invSinOmega = 1.0F / (float)Math.sin(omega);
         s0 = (float)Math.sin((1.0 - alpha) * omega) * invSinOmega;
         s1 = (float)Math.sin(alpha * omega) * invSinOmega;
      } else {
         s0 = 1.0F - alpha;
         s1 = alpha;
      }

      float rx = s0 * ax + s1 * bx;
      float ry = s0 * ay + s1 * by;
      float rz = s0 * az + s1 * bz;
      float rw = s0 * aw + s1 * bw;
      return new Quaternionf(rx, ry, rz, rw);
   }

   public static Vector3f getEulerAngles(Matrix4f matrix) {
      Vector3f dest = new Vector3f();
      matrix.getEulerAnglesZYX(dest);
      return dest;
   }

   public static float[] solveEquations(float[][] coefficients, float[] constants) {
      int n = constants.length;

      for (int pivot = 0; pivot < n - 1; pivot++) {
         for (int row = pivot + 1; row < n; row++) {
            float factor = coefficients[row][pivot] / coefficients[pivot][pivot];

            for (int col = pivot; col < n; col++) {
               coefficients[row][col] = coefficients[row][col] - coefficients[pivot][col] * factor;
            }

            constants[row] -= constants[pivot] * factor;
         }
      }

      float[] solution = new float[n];

      for (int i = n - 1; i >= 0; i--) {
         float sum = 0.0F;

         for (int j = i + 1; j < n; j++) {
            sum += coefficients[i][j] * solution[j];
         }

         solution[i] = (constants[i] - sum) / coefficients[i][i];
      }

      return solution;
   }

   public static float[] getRelativeQuaternion(float[] qa, float[] qb) {
      float[][] coefficients = new float[][]{
         {qa[3], -qa[2], qa[1], qa[0]}, {qa[2], qa[3], -qa[0], qa[1]}, {-qa[1], qa[0], qa[3], qa[2]}, {-qa[0], -qa[1], -qa[2], qa[3]}
      };
      float[] constants = new float[]{qb[0], qb[1], qb[2], qb[3]};
      return solveEquations(coefficients, constants);
   }

   public static Quaternionf getRelativeQuaternion(Quaternionf qa, Quaternionf qb) {
      float[][] coefficients = new float[][]{
         {qa.w(), -qa.z(), qa.y(), qa.x()}, {qa.z(), qa.w(), -qa.x(), qa.y()}, {-qa.y(), qa.x(), qa.w(), qa.z()}, {-qa.x(), -qa.y(), -qa.z(), qa.w()}
      };
      float[] constants = new float[]{qb.x(), qb.y(), qb.z(), qb.w()};
      float[] result = solveEquations(coefficients, constants);
      return new Quaternionf(result[0], result[1], result[2], result[3]);
   }

   public static void applyMatrixLerp(Matrix4f fromMatrix, Matrix4f toMatrix, Matrix4f resultMatrix, float alpha) {
      Vector3f translation = new Vector3f(toMatrix.m30() - fromMatrix.m30(), toMatrix.m31() - fromMatrix.m31(), toMatrix.m32() - fromMatrix.m32());
      translation.mul(alpha);
      Vector3f fromRotation = getEulerAngles(fromMatrix);
      float[] qFrom = toQuaternion(fromRotation.x(), fromRotation.y(), fromRotation.z());
      Vector3f toRotation = getEulerAngles(toMatrix);
      float[] qTo = toQuaternion(toRotation.x(), toRotation.y(), toRotation.z());
      float[] qRelative = getRelativeQuaternion(qFrom, qTo);
      Quaternionf qLerped = toQuaternion(slerp(QUATERNION_ONE, qRelative, alpha));
      resultMatrix.m30(resultMatrix.m30() + translation.x);
      resultMatrix.m31(resultMatrix.m31() + translation.y);
      resultMatrix.m32(resultMatrix.m32() + translation.z);
      resultMatrix.rotate(qLerped);
   }

   public static Pair<Float, Vector3f> getAngleAndAxis(Quaternionf quaternion) {
      double angle = 2.0 * Math.acos(quaternion.w());
      double sin = Math.sin(angle / 2.0);
      if (sin == 0.0) {
         return Pair.of(0.0F, new Vector3f(0.0F, 0.0F, 0.0F));
      }

      Vector3f axis = new Vector3f(quaternion.x(), quaternion.y(), quaternion.z());
      axis.mul((float)(1.0 / sin));
      return Pair.of((float)angle, axis);
   }

   public static Pair<Float, Vector3f> getAngleAndAxis(float[] quaternion) {
      double angle = 2.0 * Math.acos(quaternion[3]);
      double sin = Math.sin(angle / 2.0);
      if (sin == 0.0) {
         return Pair.of(0.0F, new Vector3f(0.0F, 0.0F, 0.0F));
      }

      Vector3f axis = new Vector3f(quaternion[0], quaternion[1], quaternion[2]);
      axis.mul((float)(1.0 / sin));
      return Pair.of((float)angle, axis);
   }

   public static Quaternionf multiplyQuaternion(Quaternionf quaternion, float multiplier) {
      Pair<Float, Vector3f> angleAndAxis = getAngleAndAxis(quaternion);
      float newAngle = (Float)angleAndAxis.getLeft() * multiplier;
      Vector3f axis = (Vector3f)angleAndAxis.getRight();
      double sin = Math.sin(newAngle / 2.0F);
      double cos = Math.cos(newAngle / 2.0F);
      axis.mul((float)sin);
      return new Quaternionf(axis.x(), axis.y(), axis.z(), (float)cos);
   }

   public static float[] multiplyQuaternion(float[] quaternion, float multiplier) {
      Pair<Float, Vector3f> angleAndAxis = getAngleAndAxis(quaternion);
      float newAngle = (Float)angleAndAxis.getLeft() * multiplier;
      Vector3f axis = (Vector3f)angleAndAxis.getRight();
      double sin = Math.sin(newAngle / 2.0F);
      double cos = Math.cos(newAngle / 2.0F);
      axis.mul((float)sin);
      return new float[]{axis.x(), axis.y(), axis.z(), (float)cos};
   }

   public static double getTwoVecAngle(Vec3 v1, Vec3 v2) {
      double dotProduct = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
      double magnitude1 = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
      double magnitude2 = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
      if (magnitude1 * magnitude2 == 0.0) {
         return -1.0;
      }

      double cos = dotProduct / (magnitude1 * magnitude2);
      return Math.acos(cos);
   }

   public static float splineCurve(float[] y, float tension, float alpha) {
      if (y.length != 4) {
         throw new IllegalArgumentException("y value length must be 4 when doing catmull-rom spline");
      } else if (!(tension < 0.0F) && !(tension > 1.0F)) {
         float v0 = (y[2] - y[0]) * 0.5F;
         float v1 = (y[3] - y[1]) * 0.5F;
         float t2 = alpha * alpha;
         float t3 = alpha * t2;
         float h1 = 2.0F * t3 - 3.0F * t2 + 1.0F;
         float h2 = -2.0F * t3 + 3.0F * t2;
         float h3 = t3 - 2.0F * t2 + alpha;
         float h4 = t3 - t2;
         return h1 * y[1] + h2 * y[2] + h3 * v0 + h4 * v1;
      } else {
         throw new IllegalArgumentException("tension must be 0~1 when doing catmull-rom spline");
      }
   }

   public static float[] quaternionSplineCurve(float[][] quaternions, float tension, float alpha) {
      if (quaternions.length != 4) {
         throw new IllegalArgumentException("y value length must be 4 when doing catmull-rom spline");
      }

      if (!(tension < 0.0F) && !(tension > 1.0F)) {
         float[] angles0 = toEulerAngles(quaternions[0]);
         float[] angles1 = toEulerAngles(quaternions[1]);
         float[] angles2 = toEulerAngles(quaternions[2]);
         float[] angles3 = toEulerAngles(quaternions[3]);
         float[] result = new float[3];

         for (int i = 0; i < 3; i++) {
            float[] input = new float[]{angles0[i], angles1[i], angles2[i], angles3[i]};
            result[i] = splineCurve(input, tension, alpha);
         }

         return toQuaternion(result[0], result[1], result[2]);
      } else {
         throw new IllegalArgumentException("tension must be 0~1 when doing catmull-rom spline");
      }
   }
}
