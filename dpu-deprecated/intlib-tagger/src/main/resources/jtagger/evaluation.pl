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
    print STDERR "./evaluation.pl <TAGSET> <GOLDSTANDARD VXML DIR> <TEST VXML FILES>\n";
    print STDERR "As a separator for list of tags in <TAGSET> use `|'\n";
    exit 1;
}

my $TAGSET = shift(@ARGV);
my $GOLDSTANDARD_DIR = shift(@ARGV);

my $JTagger = new INTLIB::JTagger();
$JTagger->initEvaluationStats();

foreach my $test_file (@ARGV) {
    my $gs_file = $test_file;
    $gs_file =~ s/^(.*?\/?)([^\/]+)$/$2/;

    print STDERR "\r$test_file                            ";

    if ($test_file !~ /vxml$/) {
        next;
    }
    
    if (! -r "$GOLDSTANDARD_DIR/$gs_file") {
        print STDERR "... no goldstandard for the file\n";
        next;
    }

    $JTagger->evaluation($TAGSET, "$GOLDSTANDARD_DIR/$gs_file", $test_file);
}
print STDERR "\r\n";

$JTagger->printEvaluationStats();