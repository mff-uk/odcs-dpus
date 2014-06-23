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

use INTLIB::JTagger;

if (scalar(@ARGV) < 2) {
    print STDERR "./institution_tagger.pl <OUTPUT DIR> <VXML FILES>\n";
    print STDERR "Anotujeme obecne nazvy institucii vo VXML datach.\n";
    exit 1;
}

my $DIR_OUTPUT = shift(@ARGV);

binmode(STDOUT, ":encoding(utf-8)");

foreach my $file (@ARGV) {
    my $output_file = $file;
    $output_file =~ s/^(.*?\/?)([^\/]+)$/$2/;

    my $XML = new INTLIB::JTagger();

    open(OUTPUT_FILE, ">$DIR_OUTPUT/$output_file");
    binmode(OUTPUT_FILE, ":encoding(utf-8)");
    print OUTPUT_FILE $XML->institutionTagger($file, "$DIR_OUTPUT/$output_file");
    close(OUTPUT_FILE);
}
