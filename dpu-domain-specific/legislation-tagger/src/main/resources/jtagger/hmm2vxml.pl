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

if (scalar(@ARGV) < 3) {
    print STDERR "./hmm2vxml.pl <HMM DIR> <OUTPUT DIR> <VXML FILES>\n";
    exit 1;
}

my $HMM_DIR = shift(@ARGV);
my $OUTPUT_DIR = shift(@ARGV);

foreach my $vxml_file (@ARGV) {
    my $hmm_file = $vxml_file;
    $hmm_file =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $hmm_file =~ s/^(.*)\.(.*)$/$1/;

    #print "$hmm_file\n";
    if (not(-r "$HMM_DIR/$hmm_file.hmm")) {
        next;
    }
    
    my $VXML = new INTLIB::JTagger;
    
    open(OUTPUT, ">$OUTPUT_DIR/$hmm_file.vxml");
    binmode(OUTPUT, ":encoding(utf-8)");
    print OUTPUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print OUTPUT "<Document>";
    print OUTPUT $VXML->hmm2vxml("$HMM_DIR/$hmm_file.hmm", $vxml_file);
    print OUTPUT "</Document>";
    close(OUTPUT);

}
