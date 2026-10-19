package cn.sh1rocu.tacz.api.event;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;

public class AddPackFindersEvent extends BaseEvent {
   private final PackType packType;
   private final Consumer<RepositorySource> sources;
   private final boolean trusted;
   public static final Event<AddPackFindersEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      AddPackFindersEvent.Callback.class, callbacks -> event -> {
         for (AddPackFindersEvent.Callback c : callbacks) {
            c.onAddPackFinders(event);
         }
      }
   );

   public AddPackFindersEvent(PackType packType, Consumer<RepositorySource> sources, boolean trusted) {
      this.packType = packType;
      this.sources = sources;
      this.trusted = trusted;
   }

   public void addRepositorySource(RepositorySource source) {
      this.sources.accept(source);
   }

   public PackType getPackType() {
      return this.packType;
   }

   public boolean isTrusted() {
      return this.trusted;
   }

   public interface Callback {
      void onAddPackFinders(AddPackFindersEvent var1);
   }
}
