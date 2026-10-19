undef $/;
for my $file (@ARGV) {
  open(my $in, "<:encoding(UTF-8)", $file) or die; my $s = <$in>; close $in; my $o = $s;
  $s =~ s/\(\((Byte|Short|Int|Long|Float|Double)Tag\)(\w+)\)\.getAs(Byte|Short|Int|Long|Float|Double)\(\)/"((${1}Tag)$2)." . lc($3) . "Value()"/ge;
  $s =~ s/import net\.minecraft\.Util;/import net.minecraft.util.Util;/;
  $s =~ s/net\.minecraft\.world\.entity\.animal\.horse\./net.minecraft.world.entity.animal.equine./g;
  if ($file =~ /mixin/i) { $s =~ s/\((LivingEntity|Player|Entity|ServerPlayer|Mob|ServerLevel|Level|ItemStack|BlockState)\)this\b/($1)(Object)this/g; }
  $s =~ s/\b(minecraft|mc|client|Minecraft\.getInstance\(\)|this\.minecraft)\.screen\b(?![\w(])/$1.gui.screen()/g;
  $s =~ s/\b(minecraft|mc|client|Minecraft\.getInstance\(\))\.setScreen\(/$1.gui.setScreen(/g;
  $s =~ s/BuiltInRegistries\.(\w+)\.getHolder\(([^()]*)\)\.orElse\(null\)/BuiltInRegistries.$1.get($2).orElse(null)/g;
  if ($s ne $o) { open(my $out, ">:encoding(UTF-8)", $file) or die; print $out $s; close $out; }
}
