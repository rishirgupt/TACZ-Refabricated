#!/usr/bin/perl
# Bulk rewrites for API changes between Minecraft 1.21.1 and 26.2. Run on already Mojang-named sources.
# usage: api126.pl <file.java>...
use strict;
use warnings;

for my $file (@ARGV) {
    open(my $in, "<:encoding(UTF-8)", $file) or die "cannot read $file";
    local $/;
    my $src = <$in>;
    close $in;
    my $orig = $src;

    my $usesNbt = $src =~ /net\.minecraft\.nbt\.(CompoundTag|ListTag|Tag)\b/;
    my $arg = qr/((?:[^()]|\((?:[^()]|\([^()]*\))*\))*?)/;   # call arguments with up to two nesting levels

    if ($usesNbt) {
        $src =~ s/\.getUnsafe\(\)/.tag/g;
        $src =~ s/\.contains\($arg,\s*\d+\)/.contains($1)/g;
        $src =~ s/\.getAllKeys\(\)/.keySet()/g;
        $src =~ s/\.getInt\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getIntOr($1, 0)/g;
        $src =~ s/\.getLong\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getLongOr($1, 0L)/g;
        $src =~ s/\.getFloat\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getFloatOr($1, 0.0F)/g;
        $src =~ s/\.getDouble\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getDoubleOr($1, 0.0)/g;
        $src =~ s/\.getShort\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getShortOr($1, (short)0)/g;
        $src =~ s/\.getByte\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getByteOr($1, (byte)0)/g;
        $src =~ s/\.getBoolean\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getBooleanOr($1, false)/g;
        $src =~ s/\.getString\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getStringOr($1, "")/g;
        $src =~ s/\.getCompound\(("[^"]*"|[\w.]+(?:\(\))?)\)/.getCompoundOrEmpty($1)/g;
        $src =~ s/\.getList\(("[^"]*"|[\w.]+(?:\(\))?),\s*\d+\)/.getListOrEmpty($1)/g;
    }

    # UUID helpers on tags were replaced by codecs
    $src =~ s/\.putUUID\(("[^"]*"|\w+),\s*/.store($1, net.minecraft.core.UUIDUtil.CODEC, /g;
    $src =~ s/\.hasUUID\(("[^"]*"|\w+)\)/.read($1, net.minecraft.core.UUIDUtil.CODEC).isPresent()/g;
    $src =~ s/\.getUUID\(("[^"]*"|\w+)\)(?=\s*[;,)])/.read($1, net.minecraft.core.UUIDUtil.CODEC).orElse(null)/g if $usesNbt;

    # entity / level accessors that became methods
    $src =~ s/\.selected\b(?![\w(])/.getSelectedSlot()/g;

    if ($src ne $orig) {
        open(my $o, ">:encoding(UTF-8)", $file) or die "cannot write $file";
        print $o $src;
        close $o;
    }
}
