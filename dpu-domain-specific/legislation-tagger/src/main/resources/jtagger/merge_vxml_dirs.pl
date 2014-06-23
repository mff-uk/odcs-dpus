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
    print STDERR "./merge_vxml.pl <DIR WITH TXT FILES> <OUTPUT DIR> <VXML DIRS> <FILES>\n";
    exit 1;
}

my $DIR_TXT = shift(@ARGV);
my $DIR_OUTPUT = shift(@ARGV);
my @FILES = ();
my @DIRS = ();

foreach my $arg (@ARGV) {
    if (-d $arg) {
        push(@DIRS, $arg);
        next;
    }

    push(@FILES, $arg);
}

foreach my $ann_file (@FILES) {
    my $file_name = $ann_file;
    $file_name =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $file_name =~ s/^(.*)\.(.*)$/$1/;

    print STDERR "$file_name\n";

    ## Load annotation
    my $VXML = new INTLIB::VXML();

    ## Save new file
    open(OUTPUT, ">$DIR_OUTPUT/$file_name.vxml");
    binmode(OUTPUT, ":encoding(utf-8)");
    print OUTPUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print OUTPUT "<Document>";
    print OUTPUT $VXML->merge($DIR_TXT . "/$file_name.txt", map {$_ . "/$file_name.vxml"} @DIRS);
    print OUTPUT "</Document>";
    close(OUTPUT);
}

