#!/usr/bin/perl

use strict;
use warnings;

my $FOLD = 10;
my %data = ();

while (<>) {
    chomp($_);
    my @f = split(/\t/, $_);

    print "$_\n";

    if (scalar(@f) != 5) {
        next;
    }

    $data{$f[0]}{acc} += $f[1];
    $data{$f[0]}{pre} += $f[2];
    $data{$f[0]}{rec} += $f[3];
    $data{$f[0]}{fme} += $f[4];
}

print "\n";
foreach my $tag (sort keys %data) {
    print "$tag\t";
    print sprintf("%.2f", $data{$tag}{acc} / $FOLD * 100) . "\t";
    print sprintf("%.2f", $data{$tag}{pre} / $FOLD * 100) . "\t";
    print sprintf("%.2f", $data{$tag}{rec} / $FOLD * 100) . "\t";
    print sprintf("%.2f", $data{$tag}{fme} / $FOLD * 100) . "\t";
    print "\n";
}
