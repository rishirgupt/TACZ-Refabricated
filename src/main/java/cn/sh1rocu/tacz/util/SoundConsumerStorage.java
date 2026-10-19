package cn.sh1rocu.tacz.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import com.mojang.blaze3d.audio.Channel;

public class SoundConsumerStorage {
   public static final Set<Consumer<Channel>> soundConsumerChannels = Collections.synchronizedSet(new HashSet<>());
}
