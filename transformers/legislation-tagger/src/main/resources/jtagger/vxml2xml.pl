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

use INTLIB::XML;

if (scalar(@ARGV) < 2) {
    print STDERR "./vxml2xml.pl <OUTPUT DIR> <VXML FILES>\n";
    exit 1;
}

my $DIR_OUTPUT = shift(@ARGV);

foreach my $file (@ARGV) {
    my $output_file = $file;
    $output_file =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $output_file =~ s/^(.*)\.(.*)$/$1/;

    my $XML = new INTLIB::XML();
    $XML->vxml2xml($file, "$DIR_OUTPUT/$output_file.xml");
}
