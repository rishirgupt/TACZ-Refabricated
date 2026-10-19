#!/usr/bin/perl
# usage: fixat.pl '<symbol regex>' '<perl substitution applied to the reported line>'
# Applies the substitution only on lines where the compiler reported a missing symbol matching the regex.
use strict; my ($sym, $sub) = @ARGV; my %hits;
open(my $log, "<", "$ENV{TEMP}/tz.log") or die; my @l = <$log>; close $log;
for my $i (0..$#l) {
  next unless $l[$i] =~ /^(.*\.java):(\d+): error: (.*)/;
  my ($f, $n, $msg) = ($1, $2, $3); my $ctx = join("", @l[$i..($i+5 > $#l ? $#l : $i+5)]);
  $hits{$f}{$n} = 1 if $ctx =~ /$sym/;
}
my $count = 0;
for my $f (keys %hits) {
  open(my $in, "<", $f) or next; my @src = <$in>; close $in;
  for my $n (keys %{$hits{$f}}) { local $_ = $src[$n-1]; my $before = $_; eval $sub; die $@ if $@; if ($_ ne $before) { $src[$n-1] = $_; $count++; } }
  open(my $out, ">", $f); print $out @src; close $out;
}
print "changed $count lines\n";
