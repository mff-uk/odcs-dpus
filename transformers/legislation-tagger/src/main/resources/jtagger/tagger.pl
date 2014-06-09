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

if (scalar(@ARGV) != 2) {
    print STDERR "./tagger.pl <TXT FILE> <OUTPUT FORMAT>\n";
    print STDERR "<OUTPUT FORMAT> could be {ANN, XML}\n";
    exit 1;
}

my $TAGGER = '../apps/tagger/tagger';
my $MODEL = '../data/judikatury_demo/hmm/model.hmm';

my $Brat = new INTLIB::Brat();
    
## Vytvorime zo vstupneho suboru XML
open(FILE, ">/tmp/judikatura.xml");
binmode(FILE, ":encoding(utf8)");
print FILE $Brat->ann2xml($ARGV[0]);
close(FILE);

## Zo XML vytvorime format ktory pozaduje tagger
open(FILE, ">/tmp/judikatura.hmm");
binmode(FILE, ":encoding(utf8)");
print FILE $Brat->xml2hmm("/tmp/judikatura.xml");
close(FILE);

## Zavolam tagger
open(FILE, ">/tmp/judikatura.out");
my $vystup = `cat /tmp/judikatura.hmm | cut -f 1 | $TAGGER $MODEL 2>/dev/null`;
print FILE $vystup;
close(FILE);

## Prevod na XML
open(FILE, ">/tmp/judikatura.xml");
binmode(FILE, ":encoding(utf8)");
print FILE $Brat->hmm2xml($ARGV[0], "/tmp/judikatura.out");
close(FILE);

## Pravidlovy modul
$Brat->fishingRules("/tmp/judikatura.xml", "/tmp/opravena_judikatura.xml");
    
## Doplnime do XML cisla
open(FILE, ">/tmp/pekna_xml_judikatura.xml");
binmode(FILE, ":encoding(utf8)");
print FILE $Brat->addPositionToXML($ARGV[0], "/tmp/opravena_judikatura.xml");
close(FILE);

## Podla typu vystupu dame but ANN alebo XML
if ($ARGV[1] eq "ANN") {
    print $Brat->xml2ann("/tmp/pekna_xml_judikatura.xml");
}

if ($ARGV[1] eq "XML") {
    system("cat /tmp/pekna_xml_judikatura.xml");
}

## Zmazanie suborov
#system("rm /tmp/pekna_xml_judikatura.xml");
#system("rm /tmp/opravena_judikatura.xml");
#system("rm /tmp/judikatura.xml");
#system("rm /tmp/judikatura.hmm");
#system("rm /tmp/judikatura.out");
