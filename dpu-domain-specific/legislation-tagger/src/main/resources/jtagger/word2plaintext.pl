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

if (scalar(@ARGV) < 2) {
    print STDERR "./word2plaintext.pl <OUTPUT_DIR> <HTML FILES>\n";
    print STDERR "App extracts data from HTML file and prepares data for annotating in Brat tool.\n";
    exit 1;
}

my $OUTPUT_DIR = shift(@ARGV);

foreach my $file (@ARGV) {
    ## Prvych 5 riadkov zo suboru preskakujem.
    ## (Zatial)
    my $nol = 0;

    ## Nacitame subor do pola
    my @lines = ();
    open(FILE, "<$file");
    while (<FILE>) {
        $nol++;
        next if ($nol <= 5);

        chomp($_);
        push(@lines, $_);
    }
    close(FILE);

    ## Odstranime z HTML znaciek atributy
    foreach my $line (@lines) {
        #print STDERR "POVODNE: $line\n";
        $line =~ s/<(\w+)( \w+="?[^>]+"?)+>/<$1>/g;
        #print STDERR "NOVINKA: $line\n";
        #print STDERR "\n------------------------------\n\n";
    }

    ## Znacku </td><td> prevedieme na tabulator
    foreach my $line (@lines) {
        print STDERR "LINE: $line -->";
        $line =~ s/(?:<br>|<p>)/\n/g;
        print STDERR "$line\n";

        $line =~ s/<\/tr>/\n/g;
        $line =~ s/<\/td><td>/\t/g;
    }

    ## Odstranim HTML znacky (uz teraz vsetky zvysne)
    foreach my $line (@lines) {
        $line =~ s/<[^>]+>//g;
        $line =~ s/\&[a-z]+;/ /g;
    }

    ## Ak je za bodkou velke pismeno, odriadkujeme...
    ## Nesmie tam byt ale za tym velkym pismenom hned bodka
    #foreach my $line (@lines) {
    #    $line =~ s/(\.)\s+([A-Z][^\.])/$1\n$2/g;
    #}

    ## Ulozime
    my $vystupny_subor = $file;
    $vystupny_subor =~ s/^(.*\/)//;
    $vystupny_subor =~ s/Rozhodnuti - //;
    $vystupny_subor =~ s/\.doc/.txt/;
    $vystupny_subor =~ s/\s/_/g;

    open(FILE, ">$OUTPUT_DIR/$vystupny_subor");
    print FILE join("", @lines);
    close(FILE);
}