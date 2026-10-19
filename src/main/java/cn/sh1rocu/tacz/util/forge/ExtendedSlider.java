package cn.sh1rocu.tacz.util.forge;

import java.text.DecimalFormat;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Slider with a real value range and step size, drawn by the vanilla slider. */
public class ExtendedSlider extends AbstractSliderButton {
   protected Component prefix;
   protected Component suffix;
   protected double minValue;
   protected double maxValue;
   protected double stepSize;
   protected boolean drawString;
   private final DecimalFormat format;

   public ExtendedSlider(
      int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString
   ) {
      super(x, y, width, height, Component.empty(), 0.0);
      this.prefix = prefix;
      this.suffix = suffix;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.stepSize = Math.abs(stepSize);
      this.value = this.snapToNearest((currentValue - minValue) / (maxValue - minValue));
      this.drawString = drawString;
      if (stepSize == 0.0) {
         precision = Math.min(precision, 4);
         StringBuilder builder = new StringBuilder("0");
         if (precision > 0) {
            builder.append('.');
         }

         while (precision-- > 0) {
            builder.append('0');
         }

         this.format = new DecimalFormat(builder.toString());
      } else if (Mth.equal(this.stepSize, Math.floor(this.stepSize))) {
         this.format = new DecimalFormat("0");
      } else {
         this.format = new DecimalFormat(Double.toString(this.stepSize).replaceAll("[0-9]", "0"));
      }

      this.updateMessage();
   }

   public ExtendedSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString) {
      this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1.0, 0, drawString);
   }

   public double getValue() {
      return this.value * (this.maxValue - this.minValue) + this.minValue;
   }

   public long getValueLong() {
      return Math.round(this.getValue());
   }

   public int getValueInt() {
      return (int)this.getValueLong();
   }

   public void setValue(double value) {
      this.value = this.snapToNearest((value - this.minValue) / (this.maxValue - this.minValue));
      this.updateMessage();
   }

   public String getValueString() {
      return this.format.format(this.getValue());
   }

   private double snapToNearest(double value) {
      if (this.stepSize <= 0.0) {
         return Mth.clamp(value, 0.0, 1.0);
      }

      value = Mth.lerp(Mth.clamp(value, 0.0, 1.0), this.minValue, this.maxValue);
      value = this.stepSize * Math.round(value / this.stepSize);
      if (this.minValue > this.maxValue) {
         value = Mth.clamp(value, this.maxValue, this.minValue);
      } else {
         value = Mth.clamp(value, this.minValue, this.maxValue);
      }

      return Mth.map(value, this.minValue, this.maxValue, 0.0, 1.0);
   }

   @Override
   protected void updateMessage() {
      if (this.drawString) {
         this.setMessage(Component.literal("").append(this.prefix).append(this.getValueString()).append(this.suffix));
      } else {
         this.setMessage(Component.empty());
      }
   }

   @Override
   protected void applyValue() {
   }
}
