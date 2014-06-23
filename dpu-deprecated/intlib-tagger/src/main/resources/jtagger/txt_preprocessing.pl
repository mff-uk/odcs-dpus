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

if (scalar(@ARGV) < 1) {
    print STDERR "./txt_preprocessing.pl <TXT FILE>\n";
    exit 1;
}

my $TXT_FILE = shift(@ARGV);

my $is_us = undef;
my $is_ns = undef;
my $was_note = 0;
my $line = 0;

binmode(STDOUT, ":encoding(utf8)");

open(TXT_FILE, "<$TXT_FILE");
binmode(TXT_FILE, ":encoding(utf8)");
while (<TXT_FILE>) {
    $line++;

    ## Dokumenty US zacneme s poznamkou
    chomp($_);
    if (!$_ and !$was_note) {
        print STDERR "$line: PRESKAKUJEM PRAZDNY RIADOK\n";
        next;
    }

    ## Tu sa rozhodneme ci je to US alebo NS
    if ($_ and
        !defined($is_us)) {
        if ($_ =~ /^(I|II|III|IV)..S/) {
            print STDERR "$line: DEFINUJEM US\n";
            $is_us = 1;
            $is_ns = 0;
        }
        else {
            print STDERR "$line: DEFINUJEM NS\n";
            $is_ns = 1;
            $is_us = 0;
        }
    }

    ## Pre US zacneme tlacit az od riadku zacinajuceho abstraktom
    if ($is_us and !$was_note and $_ =~ /^Pozn/) {
        print STDERR "$line: POZNAMKA\n";
        $was_note = 1;
    }

    ## Pre NS zacneme tlacit od USNESENI alebo CESKA REPUBLIKA
    if ($is_ns and !$was_note and $_ =~ /(?:U S N E S E N|ROZSUDEK|.ESK. REPUBLIKA|.esk. republika|S t a n o v i s k o)/) {
        print STDERR "$line: ZACIATOK NS\n";
        $was_note = 1;
    }

    if ($was_note) {
        print $_ . "\n";
    }
}
