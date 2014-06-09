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

if (scalar(@ARGV) < 2) {
    print STDERR "./get_html_from_xml_file.pl <OUTPUT DIR> <XML FILES>\n";
    exit 1;
}

my $OUTPUT_DIR = shift(@ARGV);

foreach my $vxml_file (@ARGV) {
    my $html_file = $vxml_file;
    $html_file =~ s/^(.*?\/?)([^\/]+)$/$2/;
    $html_file =~ s/^(.*)\.(.*)$/$1/;

    my $VXML = new INTLIB::VXML;
    
    open(OUTPUT, ">$OUTPUT_DIR/$html_file.html");
    binmode(OUTPUT, ":encoding(utf-8)");
    print OUTPUT "<html>";
    print OUTPUT "<title>$vxml_file</title>";
    print OUTPUT "<meta http-equiv=Content-Type content=\"text/html; charset=utf-8\">";
    print OUTPUT "</head>";
    print OUTPUT "<body>";
    print OUTPUT "<code>";
    print OUTPUT $VXML->vxml2html($vxml_file);
    print OUTPUT "</code>";
    print OUTPUT "</body>";
    print OUTPUT "</html>";
    close(OUTPUT);
}