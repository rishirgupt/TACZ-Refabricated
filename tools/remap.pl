#!/usr/bin/perl
# Rewrites decompiled sources that use Fabric intermediary names (class_123, method_456, field_789)
# to Mojang names, using a tiny-v2 mapping file (official / intermediary / named).
#
# usage: remap.pl <mappings.tiny> <file.java>...      (files are rewritten in place)
use strict;
use warnings;

my $mapfile = shift @ARGV;

my %cls;     # last intermediary segment (class_123) => named internal name (a/b/C$D)
my %meth;    # method_123 => name
my %fld;     # field_123 => name
my %conflict;

open(my $fh, "<", $mapfile) or die "cannot open $mapfile";
<$fh>;       # header
while (my $line = <$fh>) {
    chomp $line;
    my @p = split /\t/, $line, -1;
    if ($p[0] eq 'c') {
        my ($inter, $named) = ($p[2], $p[3]);
        my $last = $inter;
        $last =~ s/.*[\$\/]//;
        $cls{$last} = $named;
    } elsif (@p >= 6 && $p[1] eq 'm') {
        my ($inter, $named) = ($p[4], $p[5]);
        next if $inter eq $named || $inter =~ /^</;
        if (exists $meth{$inter} && $meth{$inter} ne $named) { $conflict{$inter} = 1; }
        $meth{$inter} = $named;
    } elsif (@p >= 6 && $p[1] eq 'f') {
        my ($inter, $named) = ($p[4], $p[5]);
        next if $inter eq $named;
        if (exists $fld{$inter} && $fld{$inter} ne $named) { $conflict{$inter} = 1; }
        $fld{$inter} = $named;
    }
}
close $fh;

my %unmapped;

sub fqcn {      # class_123 => net.minecraft.world.item.ItemStack (dots, inner with dots)
    my ($tok) = @_;
    my $n = $cls{$tok};
    return undef unless defined $n;
    $n =~ s/[\/\$]/./g;
    return $n;
}

sub simple {
    my ($tok) = @_;
    my $f = fqcn($tok);
    return undef unless defined $f;
    $f =~ s/.*\.//;
    return $f;
}

sub member_name {
    my ($tok) = @_;
    return $meth{$tok} if exists $meth{$tok};
    return $fld{$tok} if exists $fld{$tok};
    $unmapped{$tok}++;
    return $tok;
}

for my $file (@ARGV) {
    open(my $in, "<:encoding(UTF-8)", $file) or die "cannot read $file";
    local $/;
    my $src = <$in>;
    close $in;

    my $isMixin = $file =~ /mixin/i;

    # --- collect class tokens used in this file and detect simple-name collisions
    my %used;
    $used{$1} = 1 while $src =~ /\b(class_\d+)\b/g;
    my %bySimple;
    for my $t (keys %used) {
        my $f = fqcn($t);
        unless (defined $f) { $unmapped{$t}++; next; }
        my $s = simple($t);
        $bySimple{$s}{$f} = 1;
    }
    # non-minecraft imports occupy simple names too
    my %foreign;
    while ($src =~ /^import\s+(?!static\b)(?!net\.minecraft\.class_)([\w.]+)\.(\w+);/mg) {
        $foreign{$2}{"$1.$2"} = 1;
    }
    my %qualify;      # simple names that must be written fully qualified
    for my $s (keys %bySimple) {
        my $count = scalar(keys %{$bySimple{$s}}) + (exists $foreign{$s} ? scalar(keys %{$foreign{$s}}) : 0);
        $qualify{$s} = 1 if $count > 1;
    }

    my $name = sub {   # token => text to use in code
        my ($t) = @_;
        my $f = fqcn($t);
        return $t unless defined $f;
        my $s = simple($t);
        return $qualify{$s} ? $f : $s;
    };

    # --- imports
    $src =~ s{^import\s+(static\s+)?net\.minecraft((?:\.class_\d+)+)((?:\.\w+)?);}{
        my ($static, $chain, $tail) = ($1 // '', $2, $3);
        my @toks = $chain =~ /(class_\d+)/g;
        my $f = fqcn($toks[-1]);
        if (!defined $f) { "import $static" . "net.minecraft$chain$tail;" }
        else {
            my $s = simple($toks[-1]);
            if (!$static && $qualify{$s}) { "" }
            else {
                my $t = $tail;
                $t =~ s/\.(field_\d+|method_\d+)/"." . member_name($1)/e;
                "import $static$f$t;"
            }
        }
    }mge;

    # --- split into string literals and code
    my @out;
    my @parts = split /("(?:[^"\\\n]|\\.)*")/, $src, -1;
    for my $i (0 .. $#parts) {
        my $seg = $parts[$i];
        if ($i % 2 == 1) {
            # string literal: only mixin-style internal names and member names
            $seg =~ s{net/minecraft/((?:class_\d+[\$/]?)+)}{
                my $chain = $1;
                my @toks = $chain =~ /(class_\d+)/g;
                my $n = $cls{$toks[-1]};
                defined $n ? $n : "net/minecraft/$chain"
            }ge;
            $seg =~ s/\b(method_\d+|field_\d+)\b/member_name($1)/ge;
            push @out, $seg;
        } else {
            $seg =~ s{\bnet\.minecraft((?:\.class_\d+)+)}{
                my @toks = $1 =~ /(class_\d+)/g;
                fqcn($toks[-1]) // "net.minecraft$1"
            }ge;
            # Outer.Inner chains written out in full
            $seg =~ s{\b(class_\d+)\.(class_\d+)\b}{ $name->($1) . "." . (simple($2) // $2) }ge;
            $seg =~ s{\b(class_\d+)\b}{ $name->($1) }ge;
            $seg =~ s/\b(method_\d+|field_\d+|comp_\d+)\b/member_name($1)/ge;
            push @out, $seg;
        }
    }
    my $result = join('', @out);

    open(my $o, ">:encoding(UTF-8)", $file) or die "cannot write $file";
    print $o $result;
    close $o;
}

if (%unmapped) {
    print STDERR "unmapped tokens: " . scalar(keys %unmapped) . "\n";
    for my $t (sort keys %unmapped) { print STDERR "  $t ($unmapped{$t})\n"; }
}
if (%conflict) {
    print STDERR "conflicting names: " . join(", ", sort keys %conflict) . "\n";
}
