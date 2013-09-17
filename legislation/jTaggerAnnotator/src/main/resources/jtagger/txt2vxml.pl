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

if (scalar(@ARGV) < 2) {
    print STDERR "./txt2vxml.pl <OUTPUT DIR> <TXT FILES>\n";
    exit 1;
}

my $DIR_OUTPUT = shift(@ARGV);

foreach my $txt_file (@ARGV) {
    my $file_name = $txt_file;
    $file_name =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $file_name =~ s/^(.*)\.(.*)$/$1/;

    print STDERR "$file_name\n";
    
    ## Save new file
    open(OUTPUT, ">$DIR_OUTPUT/$file_name.vxml");
    binmode(OUTPUT, ":encoding(utf-8)");
    print OUTPUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print OUTPUT "<Document>";
    
    open(INPUT, "<$txt_file");
    binmode(INPUT, ":encoding(utf-8)");
    while (<INPUT>) {
        chomp($_);
        print OUTPUT "$_\n";
    }
    close(INPUT);

    print OUTPUT "</Document>";
    close(OUTPUT);
}

