package com.tacz.guns.api.client.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public class DiscreteTrackArray implements Iterable<Integer> {
   private int top = 0;
   private final ArrayList<LinkedList<Integer>> tracks;
   private int modCount = 0;

   public DiscreteTrackArray() {
      this.tracks = new ArrayList<>();
   }

   public void ensureCapacity(int size) {
      int cap = this.tracks.size();

      for (int i = cap; i < size; i++) {
         this.modCount++;
         this.tracks.add(null);
      }
   }

   public void ensureTrackAmount(int index, int amount) {
      if (index >= this.tracks.size()) {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.tracks.size());
      }

      LinkedList<Integer> list = this.tracks.get(index);
      int oldSize = list == null ? 0 : list.size();

      for (int i = oldSize; i < amount; i++) {
         this.assignNewTrack(index);
      }
   }

   public int addTrackLine() {
      this.modCount++;
      this.tracks.add(null);
      return this.tracks.size() - 1;
   }

   public int assignNewTrack(int index) {
      if (this.top == Integer.MAX_VALUE) {
         throw new RuntimeException("Can't assign new track due to overflow");
      }

      this.modCount++;
      Optional.ofNullable(this.tracks.get(index)).ifPresentOrElse(list -> list.add(this.top++), () -> {
         LinkedList<Integer> list = new LinkedList<>();
         list.add(this.top++);
         this.tracks.set(index, list);
      });
      return this.top - 1;
   }

   public int getTrackLineSize() {
      return this.tracks.size();
   }

   @Nonnull
   public @UnmodifiableView List<Integer> getByIndex(int index) {
      LinkedList<Integer> list = this.tracks.get(index);
      return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
   }

   @NotNull
   @Override
   public Iterator<Integer> iterator() {
      return new DiscreteTrackArray.MyIterator(this.modCount);
   }

   private final class MyIterator implements Iterator<Integer> {
      private final int modCount;
      @Nullable
      private Iterator<Integer> iterator;
      private int nextIndex;

      public MyIterator(int modCount) {
         this.modCount = modCount;
         int index = this.findNextNotEmptyList(-1);
         if (index != -1) {
            this.iterator = DiscreteTrackArray.this.tracks.get(index).iterator();
            this.nextIndex = this.findNextNotEmptyList(index);
         } else {
            this.iterator = null;
            this.nextIndex = -1;
         }
      }

      @Override
      public boolean hasNext() {
         this.checkForModifications();
         return this.iterator != null && this.iterator.hasNext() ? true : this.nextIndex != -1;
      }

      public Integer next() throws IllegalStateException {
         this.checkForModifications();
         if (this.iterator != null && this.iterator.hasNext()) {
            return this.iterator.next();
         } else if (this.nextIndex != -1) {
            this.iterator = DiscreteTrackArray.this.tracks.get(this.nextIndex).iterator();
            this.nextIndex = this.findNextNotEmptyList(this.nextIndex);
            return this.iterator.next();
         } else {
            throw new IllegalStateException("No more elements");
         }
      }

      private void checkForModifications() {
         if (DiscreteTrackArray.this.modCount != this.modCount) {
            throw new ConcurrentModificationException("Container modified during iteration");
         }
      }

      private int findNextNotEmptyList(int index) {
         for (int i = index + 1; i < DiscreteTrackArray.this.tracks.size(); i++) {
            LinkedList<Integer> list = DiscreteTrackArray.this.tracks.get(i);
            if (list != null && !list.isEmpty()) {
               return i;
            }
         }

         return -1;
      }
   }
}
