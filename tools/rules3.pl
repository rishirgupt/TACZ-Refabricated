undef $/;
for my $file (@ARGV) {
  open(my $in, "<:encoding(UTF-8)", $file) or die; my $s = <$in>; close $in; my $o = $s;
  $s =~ s/\bScreen\.hasShiftDown\(\)/Minecraft.getInstance().hasShiftDown()/g;
  $s =~ s/\bScreen\.hasControlDown\(\)/Minecraft.getInstance().hasControlDown()/g;
  $s =~ s/\bScreen\.hasAltDown\(\)/Minecraft.getInstance().hasAltDown()/g;
  $s =~ s/\.getTimer\(\)/.getDeltaTracker()/g;
  if ($s ne $o) {
    $s =~ s/(\npackage [^\n]*\n)/$1/;
    if ($s =~ /Minecraft\.getInstance\(\)/ && $s !~ /import net\.minecraft\.client\.Minecraft;/) { $s =~ s/(\npackage [^\n]*;\n)/$1\nimport net.minecraft.client.Minecraft;/; }
    open(my $out, ">:encoding(UTF-8)", $file) or die; print $out $s; close $out;
  }
}
