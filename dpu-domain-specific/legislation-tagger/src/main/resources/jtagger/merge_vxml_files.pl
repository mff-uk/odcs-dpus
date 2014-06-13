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

if (scalar(@ARGV) < 3) {
    print STDERR "./merge_vxml_files.pl <TXT FILE> <OUTPUT FILE> <FILES>\n";
    exit 1;
}

my $TXT_FILE = shift(@ARGV);
my $OUTPUT_FILE = shift(@ARGV);

## Load annotation
my $VXML = new INTLIB::VXML();

## Save new file
open(OUTPUT, ">$OUTPUT_FILE");
binmode(OUTPUT, ":encoding(utf-8)");
print OUTPUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
print OUTPUT "<Document>";
print OUTPUT $VXML->merge($TXT_FILE, @ARGV);
print OUTPUT "</Document>";
close(OUTPUT);


