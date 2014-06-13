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

if (scalar(@ARGV) < 4) {
    print STDERR "./vxml2hmm.pl <OUTPUT_DIR> <MODE> <TAGSET> <VXML FILES>\n";
    print STDERR "<MODE> could be {test, train}\n";
    print STDERR "As <TAGSET> use list of tags separated by `|'.\n";
    exit 1;
}

my $OUTPUT_DIR = shift(@ARGV);
my $MODE = shift(@ARGV);
my $TAGSET = shift(@ARGV);

#print STDERR "vxml2hmm: # suborov: " . scalar(@ARGV) . "\n";

foreach my $file (@ARGV) {
    my $file_name = $file;
    $file_name =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $file_name =~ s/^(.*)\.(.*)$/$1/;

    #print STDERR "vxml2hmm: $file -> $OUTPUT_DIR/$file_name.hmm\n";

    my $VXML = new INTLIB::JTagger;
    open(FILE, ">$OUTPUT_DIR/$file_name.hmm");
    binmode(FILE, ":encoding(utf-8)");
    print FILE $VXML->vxml2hmm($MODE, [split(/\|/, $TAGSET)], $file);
    close(FILE);
}
