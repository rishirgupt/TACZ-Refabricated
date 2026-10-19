undef $/;
for my $file (@ARGV) {
  open(my $in, "<:encoding(UTF-8)", $file) or die; my $s = <$in>; close $in; my $o = $s;
  $s =~ s/\bDirectionProperty\b/EnumProperty<Direction>/g;
  $s =~ s/import net\.minecraft\.world\.level\.block\.state\.properties\.EnumProperty<Direction>;/import net.minecraft.world.level.block.state.properties.EnumProperty;\nimport net.minecraft.core.Direction;/g;
  $s =~ s/\.blockUpdated\(([^,()]+),\s*([^()]+)\)/.updateNeighborsAt($1, $2, null)/g;
  $s =~ s/\.displayClientMessage\((.*), true\);/.sendOverlayMessage($1);/g;
  $s =~ s/\.displayClientMessage\((.*), false\);/.sendSystemMessage($1);/g;
  $s =~ s/RenderShape\.ENTITYBLOCK_ANIMATED/RenderShape.INVISIBLE/g;
  $s =~ s/import net\.minecraft\.world\.ItemInteractionResult;\n//g;
  $s =~ s/\bItemInteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION\b/InteractionResult.TRY_WITH_EMPTY_HAND/g;
  $s =~ s/\bItemInteractionResult\.SKIP_DEFAULT_BLOCK_INTERACTION\b/InteractionResult.PASS/g;
  $s =~ s/\bItemInteractionResult\b/InteractionResult/g;
  $s =~ s/InteractionResult\.sidedSuccess\([^()]*(?:\([^()]*\))?[^()]*\)/InteractionResult.SUCCESS/g;
  $s =~ s/\b(level|world|pLevel|pWorld|serverLevel|worldIn)\.random\b(?!\()/$1.getRandom()/g;
  $s =~ s/\b(this\.level|this\.world)\.random\b(?!\()/$1.getRandom()/g;
  if ($s ne $o) { open(my $out, ">:encoding(UTF-8)", $file) or die; print $out $s; close $out; }
}
