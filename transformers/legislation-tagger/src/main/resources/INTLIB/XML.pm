#!/usr/bin/perl

## Author: Vincent Kriz, 2012
## E-mail: kriz@ufal.mff.cuni.cz

## This library contains methods for converting
## VXML files with manual/automatical annotation to XML which
## follow the INTLIB XML Schema

my $debug = 0;

use strict;
use warnings;
use utf8;

use INTLIB::JTagger;

package INTLIB::XML;

sub new {
    my $self = {};

    bless $self;
    return $self;
}

sub vxml2xml {
    my ($self, $input_file, $output_file) = @_;

    ## Translations are defined here
    my %translations = (
        'Zakon' => 'act',
        'Zakon_CNR' => 'act',
        'Zakon_Federalni' => 'act',
        'Narizeni_vlady' => 'act',
        'Rozhodnuti_soudu' => 'judgment',
        'Ucinnost' => 'effectiveness',
        'Vyhlaska' => 'notice',
        'Instituce' => 'institution',
        'Plne_zneni' => 'abbreviation_definition',
        'Zkratka' => 'abbreviation_label',
        'Cenovy_vymer' => 'price_spec',
    );

    ## Open output file and write header and
    ## metadata section
    open(OUTPUT_FILE, ">$output_file");
    print OUTPUT_FILE "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print OUTPUT_FILE "<document\n\txmlns=\"http://xrg.cz/link/sourceOfLaw/1\"\n\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n\txsi:schemaLocation=\"http://xrg.cz/link/sourceOfLaw/1\n\tjudgment.xsd\">\n";
    print OUTPUT_FILE "<metadata>\n";
    print OUTPUT_FILE "\t<type_of_work>judgement</type_of_work>\n";
    print OUTPUT_FILE "\t<country_of_issue>cz</country_of_issue>\n";
    #print OUTPUT_FILE "\t<number/>\n";
    print OUTPUT_FILE "\t<language>cz</language>\n";
    #print OUTPUT_FILE "\t<issued>2012-10-11</issued>\n";
    #print OUTPUT_FILE "\t<valid>2012-10-11</valid>\n";
    print OUTPUT_FILE "\t<issuer/>\n";
    print OUTPUT_FILE "\t<original_format>word</original_format>\n";
    print OUTPUT_FILE "\t<consolidated_by/>\n";
    print OUTPUT_FILE "</metadata>\n";
    print OUTPUT_FILE "<body>\n";

    ## Read input file
    open(INPUT_FILE, "<$input_file");
    while (<INPUT_FILE>) {
        chomp($_);

        ## Skip defition of XML
        next if ($_ =~ /^<\?xml/);

        ## Remove tag <Document>
        $_ =~ s/<\/?Document>//;

        ## Remove attributes start and end
        $_ =~ s/start="\d+"//g;
        $_ =~ s/end="\d+"//g;
        $_ =~ s/label="T\d+"//g;
        $_ =~ s/\s*>/>/g;

        ## Translate tagset to English
        foreach my $tag_name (keys %translations) {
            $_ =~ s/<(\/?)$tag_name([^>]*)>/<$1$translations{$tag_name}$2>/g;
        }

        print OUTPUT_FILE "$_\n";
    }
    close(INPUT_FILE);

    print OUTPUT_FILE "</body>\n";
    print OUTPUT_FILE "</document>\n";

    close(OUTPUT_FILE);
}

sub _normalizaceInstituci {
    my ($self, $name) = @_;

    $name =~ s/([Kk]rajsk|[Mm]ěstsk)(?:ého|ému|ém|ým)/$1ý/;
    $name =~ s/([Nn]ejvyšší|[úÚ]stavní|[Oo]kresní|[Oo]bvodní)(?:[^\s]*)/$1/;
    $name =~ s/(soud)(?:[^\s]*)/$1/;
    $name =~ s/\s*,\s*$//;

    return $name;
}

sub xml2htmlRelationsBox {
    my ($self, $xml_file) = @_;

    ## Loading file
    my %rs2id = ();
    my %id2inst = ();
    open(XML_FILE, "<$xml_file");
    binmode(XML_FILE, ":encoding(utf8)");
    while (<XML_FILE>) {
        chomp($_);

        while ($_ =~ s/<institution[^>]*id="(\d+)"[^>]*>([^<]+)<\/institution>//) {
            if (!defined($id2inst{$1})) {
                $id2inst{$1} = $self->_normalizaceInstituci($2);
            }
        }

        while ($_ =~ s/<judgment[^>]*refers_to="(\d+)"[^>]*>([^<]+)<\/judgment>//) {
            if (!defined($rs2id{$2})) {
                $rs2id{$2} = $1;
            }
        }
    }
    close(XML_FILE);

    ## HTML Presentation
    my $output = "";
    $output .= "<table class='relations'>";
    $output .= "<tr><td class='head'>Judgement</td><td class='head'>Publisher</td></tr>";
    foreach my $rs (keys %rs2id) {
        $output .= "<tr><td class='data'>$rs</td><td class='data'>$id2inst{$rs2id{$rs}}</td></tr>";
    }
    $output .= "</table>";

    return $output;
}

sub xml2htmlAbbreviationBox {
    my ($self, $xml_file) = @_;

    ## Loading file
    my %labels = ();
    my %definitions = ();

    open(XML_FILE, "<$xml_file");
    binmode(XML_FILE, ":encoding(utf8)");
    while (<XML_FILE>) {
        chomp($_);

        ## Odstranim ine znacky, nech nezavadzaju
        while ($_ =~ s/<(act|judgment|institution)[^>]*>//g or
               $_ =~ s/<\/(act|judgment|institution)>//g) {
        }

        while ($_ =~ s/<abbreviation_definition[^>]*name="([^"]+)"[^>]*id="(\d+)"[^>]*>([^<]+)<\/abbreviation_definition>//) {
            $definitions{$2} = $1;
        }

        while ($_ =~ s/<abbreviation_label[^>]*refers_to="(\d+)"[^>]*>([^<]+)<\/abbreviation_label>//) {
            $labels{$1} = $2;
        }
    }
    close(XML_FILE);

    ## HTML Presentation
    my $output = "";
    $output .= "<table class='relations'>";
    $output .= "<tr><td class='head'>Abbreviation</td><td class='head'>Meaning</td></tr>";
    foreach my $def (keys %definitions) {
        $output .= "<tr><td class='data'>$labels{$def}</td><td class='data'>$definitions{$def}</td></tr>";
    }
    $output .= "</table>";

    return $output;
}

sub xml2html {
    my ($self, $xml_file) = @_;
    
    ## Tagset definition
    my @visualization = (
        "Dokument           black           white       Document",
        "act                red             black       Act",
        #"Cenovy_vymer	    Navy	    white       Price",
        "judgment           green           white       Court_decision",
        "effectiveness      yellow	    black       Effectiveness",
        "institution        DarkGoldenRod   black       Institution",
        "abbreviation_label	    Aqua	    black       Abbreviation",
        "abbreviation_definition    Navy	    white       Abbreviation_definition",
        "Plne_zneni	    Aquamarine	    black       Full meaning"
    );

    my %labels = ();
    foreach my $visualization (@visualization) {
        my @fields = split(/\s+/, $visualization);
        $labels{$fields[0]}{label} = $fields[0];
        $labels{$fields[0]}{type} = "phrase";
        $labels{$fields[0]}{bg_colour} = $fields[1];
        $labels{$fields[0]}{fg_colour} = $fields[2];
        $labels{$fields[0]}{title} = $fields[3];

        $labels{$fields[0]}{title} =~ s/_/ /g;
    }

    my $output = "";
    my $i = 0;
    my $max_id = 0;
    my $document_started = 0;
    open(FILE, "<$xml_file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        chomp($_);

        ## Tento kod pocka, kym nezacne cast <body>
        if ($_ =~ /<body>/) {
            $document_started = 1;
        }
        if ($document_started == 1) {
            $document_started++;
            next;
        }
        if ($document_started != 2) {
            next;
        }

        ## Prevod znaciek na HTML
        $_ =~ s/<[^>]+\/>//g;
        #print "Riadok: <xmp>$_</xmp><br>";
        foreach my $tag (keys %labels) {
#             if ($_ =~ /<$tag[^>]*rdf:about/) {
                $_ =~ s/<$tag[^>]*rdf:about="([^"]+)"[^>]*>(.*?)<\/$tag>/<a href="$1" target="_blank" style="padding: 3px; border: 2px solid $labels{$tag}{bg_colour}; background: $labels{$tag}{bg_colour}; color: $labels{$tag}{fg_colour}" title="$labels{$tag}{title}">$2<\/a>/g;
#             }
#             else {
                $_ =~ s/<$tag[^>]*>/<span style="padding: 3px; border: 2px solid $labels{$tag}{bg_colour}; background: $labels{$tag}{bg_colour}; color: $labels{$tag}{fg_colour}" title="$labels{$tag}{title}">/g;
                $_ =~ s/<\/$tag>/<\/span>/g;
#             }
        }

        $output .= "$_<br>";	
    }

    close(FILE);
    $output .= "</code>";
    $output .= "</body>";
    $output .= "</html>";

    return $output;
}


1;