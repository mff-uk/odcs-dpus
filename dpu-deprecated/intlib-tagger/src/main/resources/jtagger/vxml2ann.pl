#!/usr/bin/perl

BEGIN {
    require lib;
    lib->import("../../");
    lib->import("../");
    lib->import("src/main/resources");
}

my $debug = 0;

use strict;
use warnings;

use INTLIB::VXML;

binmode(STDERR, ":encoding(utf-8)");

if (scalar(@ARGV) < 2) {
    print STDERR "./vxml2ann.pl <OUTPUT DIR> <VXML FILES>\n";
    exit 1;
}

my $DIR_OUTPUT = shift(@ARGV);

foreach my $vxml_file (@ARGV) {
    my $file_name = $vxml_file;
    $file_name =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $file_name =~ s/^(.*)\.(.*)$/$1/;

    print STDERR "$vxml_file\n";
    
    open(OUTPUT, ">$DIR_OUTPUT/$file_name.ann");
    binmode(OUTPUT, ":encoding(utf-8)");

    my $Brat = new INTLIB::VXML();
    print OUTPUT $Brat->vxml2ann($vxml_file);

    close(OUTPUT);
}

