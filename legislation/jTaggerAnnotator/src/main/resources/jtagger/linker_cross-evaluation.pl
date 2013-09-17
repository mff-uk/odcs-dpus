#!/usr/bin/perl

use strict;
use warnings;

my $FOLD = 10;
my %data = ();

while (<>) {
    chomp($_);
    my @f = split(/\t/, $_);

    print "$_\n";

    if (scalar(@f) < 1) {
        next;
    }

    $data{$f[0]}{acc} += $f[1];
}

print "\n";
foreach my $tag (sort keys %data) {
    print "$tag\t";
    print sprintf("%.2f", $data{$tag}{acc} / $FOLD);
    print "\n";
}
