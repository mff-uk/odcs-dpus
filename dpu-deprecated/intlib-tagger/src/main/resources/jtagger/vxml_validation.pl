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

if (scalar(@ARGV) < 2) {
    print STDERR "./vxml_validation.pl <OUTPUT DIR> <VXML FILES>\n";
    print STDERR "This tool validates VXML documents (for example after the usage of AMAT).\n";
    exit 1;
}

my $DIR_OUTPUT = shift(@ARGV);

binmode(STDOUT, ":encoding(utf-8)");

foreach my $file (@ARGV) {
    my $output_file = $file;
    $output_file =~ s/^(.*?\/?)([^\/]+)$/$2/;

    my $XML = new INTLIB::VXML();
    $XML->vxml_validation($file, "$DIR_OUTPUT/$output_file");
}
