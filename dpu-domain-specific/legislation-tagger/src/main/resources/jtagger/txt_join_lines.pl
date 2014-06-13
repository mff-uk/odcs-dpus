#!/usr/bin/perl

use strict;
use warnings;

if (scalar(@ARGV) < 2) {
    print STDERR "./txt_join_lines.pl <OUTPUT DIR> <INPUT FILES>\n";
    exit 1
}

my $OUTPUT_DIR = shift(@ARGV);

foreach my $filename (@ARGV) {
    my $output_file = $filename;
    $output_file =~ s/(.*)\/([^\/]+)/$2/;

    open(INPUT, "<$filename");
    open(OUTPUT, ">$OUTPUT_DIR/$output_file");

    my $line = 0;
    while (<INPUT>) {
	$line++;

	## Odstranim koncove znaky
	$_ =~ s/\r//g;
	$_ =~ s/\n//g;

	## Odstranim pociatocne medzery
	$_ =~ s/^\s*//g;

	## Zapisem riadok na vystup
	print OUTPUT $_;

	## Ak je to prazdny riadok, zapisem medzeru
	if ($_ =~ /^\s*$/) {
	    print OUTPUT "\n\n";
	    next;
	}

	## Ak riadok NEkoncil medzerou, zapisem novy riadok
	if ($_ !~ /\s+$/) {
	    print OUTPUT "\n";
	}
    }

    close(INPUT);
    close(OUTPUT);
}