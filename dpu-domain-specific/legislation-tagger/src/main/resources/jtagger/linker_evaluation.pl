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
    print STDERR "./linker_evaluation.pl <GOLDSTANDARD VXML DIR> <TEST VXML FILES>\n";
    print STDERR "As a separator for list of tags in <TAGSET> use `|'\n";
    exit 1;
}

my $GOLDSTANDARD_DIR = shift(@ARGV);

binmode(STDERR, ":encoding(utf-8)");

my $JTagger = new INTLIB::JTagger();
$JTagger->initLinkerEvaluationStats();
foreach my $test_file (@ARGV) {
    my $gs_file = $test_file;
    $gs_file =~ s/^(.*?\/?)([^\/]+)$/$2/;

    if (! -r "$GOLDSTANDARD_DIR/$gs_file") {
        print STDERR "... no goldstandard for the file\n";
        next;
    }

    $JTagger->linkerEvaluation("$GOLDSTANDARD_DIR/$gs_file", $test_file);
}

#print "$JTagger->{correct}\t$JTagger->{sum}\t" . sprintf("%.2f", $JTagger->{correct} / $JTagger->{sum} * 100);
printf("RS-I\t%.2f\n", $JTagger->{correct} / $JTagger->{sum} * 100) if ($JTagger->{sum} > 0);
printf("RS-I\t%.2f\n", 0) if ($JTagger->{sum} == 0);
