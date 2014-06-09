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
    print STDERR "./txt_tokenization.pl <OUTPUT DIR> <TXT FILES>\n";
    exit 1;
}

my $OUTPUT_DIR = shift(@ARGV);

foreach my $input_file (@ARGV) {
    my $output_file = $input_file;
    $output_file =~ s/(.*)\/([^\/]+)/$2/;

    my $Brat = new INTLIB::Brat();
    open(FILE, ">$OUTPUT_DIR/$output_file");
    binmode(FILE, ":encoding(utf8)");
    print FILE $Brat->txtPreprocessing($input_file);
    close(FILE);
}
