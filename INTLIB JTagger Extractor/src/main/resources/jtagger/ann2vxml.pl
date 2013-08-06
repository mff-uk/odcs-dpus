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

use INTLIB::Brat;

if (scalar(@ARGV) < 3) {
    print STDERR "./ann2vxml.pl <DIR WITH TXT FILES> <OUTPUT DIR> <ANN FILES>\n";
    exit 1;
}

my $DIR_TXT = shift(@ARGV);
my $DIR_OUTPUT = shift(@ARGV);

foreach my $ann_file (@ARGV) {
    my $file_name = $ann_file;
    $file_name =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $file_name =~ s/^(.*)\.(.*)$/$1/;

    ## Load annotation
    my $Brat = new INTLIB::Brat();
    $Brat->loadAnnotation($ann_file);

    ## Save new file
    open(OUTPUT, ">$DIR_OUTPUT/$file_name.vxml");
    binmode(OUTPUT, ":encoding(utf-8)");
    print OUTPUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print OUTPUT "<Document>";
    print OUTPUT $Brat->ann2vxml("$DIR_TXT/$file_name.txt");
    print OUTPUT "</Document>";
    close(OUTPUT);
}

