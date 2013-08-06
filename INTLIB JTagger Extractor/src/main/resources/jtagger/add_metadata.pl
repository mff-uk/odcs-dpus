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

if (scalar(@ARGV) < 3) {
    print STDERR "./add_metadata.pl <OUTPUT DIR> <ORIGINAL TXT FILES DIR> <XML FILES>\n";
    exit 1;
}

my $DIR_OUTPUT = shift(@ARGV);
my $ORIGINAL_TXT_FILES_DIR = shift(@ARGV);

foreach my $file (@ARGV) {
    my $output_file = $file;
    $output_file =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $output_file =~ s/^(.*)\.(.*)$/$1/;

    ## Z originalneho TXT nacitam spisovu znacku
    my $number = "";
    open(ORIGINAL, "<$ORIGINAL_TXT_FILES_DIR/$output_file.txt");
    while (<ORIGINAL>) {
        chomp($_);
        if ($_ =~ /^Spisová značka(.*?)Paraleln/) {
            $number = $1;
            last;
        }
    }
    close(ORIGINAL);

    if (!$number) {
        print STDERR "No number for file $file\n";
        next;
    }

    ## Teraz to doplim do XML
    open(INPUT, "<$file");
    open(OUTPUT, ">$DIR_OUTPUT/$output_file.xml");
    while (<INPUT>) {
        chomp($_);
        $_ =~ s/<number\/>/<number>$number<\/number>/;
        print OUTPUT "$_\n";
    }
    close(INPUT);
    close(OUTPUT);
}
