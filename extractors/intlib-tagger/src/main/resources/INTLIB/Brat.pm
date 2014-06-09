#!/usr/bin/perl

## Author: Vincent Kriz, 2012
## E-mail: kriz@ufal.mff.cuni.cz

my $debug = 0;

use strict;
use warnings;
use utf8;

package INTLIB::Brat;

sub new {
    my $self = {};

    bless $self;
    return $self;
}

sub loadAnnotation {
    my ($self, $file) = @_;

    $self->{annotation} = [];
    $self->{relation} = [];

    open(FILE, "<$file");
    while (<FILE>) {
        chomp($_);
        $_ =~ s/Sbirka_rozhodnuti/Dokument/g;
        $_ =~ s/Vyhlaska/Zakon/g;

        if ($_ =~ /^(.*)\t(.*) (\d+) (\d+)\t(.*)$/) {
            push(@{$self->{annotation}}, {id => $1, tag => $2, start => $3, end => $4});
            next;
        }

        if ($_ =~ /^(.*)\t(.*) Arg1:(.*) Arg2:(.*?)\s*$/) {
            push(@{$self->{relation}}, {id => $1, type => $2, arg1 => $3, arg2 => $4});
            next;
        }

        print STDERR "[Brat::loadAnnotation] Zly format na riadku '$_'\n";
    }
    close(FILE);

    return 1;
}

sub whatToAdd {
    my ($self, $ra_previous, $ra_current) = @_;

    my @result = ();
    
    foreach my $tag (@$ra_current) {
        if (!grep(/^$tag$/, @$ra_previous)) {
            push(@result, $tag);
        }
    }

    return @result;
}

sub whatToDel {
    my ($self, $ra_previous, $ra_current) = @_;

    my @result = ();

    foreach my $tag (@$ra_previous) {
        if (!grep(/^$tag$/, @$ra_current)) {
            push(@result, $tag);
        }
    }

    return @result;
}

sub ann2vxml {
    my ($self, $file) = @_;
    my $output = "";

    my $line = 1;
    my $position = 0;
    my @previous_tags = ();

    ## Tento zoznam si musim zachovat kvoli spravnu poradiu zatvaracich tagov...
    my @zoznam_otvorenych_tagov = ();
    
    my $tag_id = 1;

    ## Zapamatame si mapovanie label <-> id, aby som to mohol
    ## pouzit pri vyplnani refers_to...
    my %label2id = ();

    open(FILE, "<$file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        my @characters = split(//, $_);

        #print "-------------------------------------------------------\n";
        #print "LINE: $line\n";
        #print "-------------------------------------------------------\n";

        foreach my $char (@characters) {
            my @tags = $self->getTag($position);

            my @tag_names = map {$_->{tag}} @tags;

            my @todel = $self->whatToDel(\@previous_tags, \@tag_names);
            my @toadd = $self->whatToAdd(\@previous_tags, \@tag_names);

            #print "[$position:$char]\t[" . join(", ", @tag_names) . "]\t[" . join(", ", @todel) . "]\t[" . join(", ", @toadd) . "]\n";

            while (scalar(@todel)) {
                #print "Potrebujem zmazat @todel\n";
                #print "Fronta: @zoznam_otvorenych_tagov\n";
                for (my $i = 0; $i < scalar(@todel); $i++) {
                    if ($todel[$i] eq $zoznam_otvorenych_tagov[$#zoznam_otvorenych_tagov]) {
                        $output .= "</$todel[$i]>";
                        pop(@zoznam_otvorenych_tagov);
                        splice(@todel, $i, 1);
                        #print "Nove todel pole: @todel\n";
                    }
                }   
            }

            ## Najskor musim zistit, ktory skonci skor a podla toho bude poradie
            my %id2end = ();
            foreach my $tag (@toadd) {
                $id2end{$tag} = $self->getEndPosition($tag, $position);
            }
            foreach my $tag (@tags) {
                $id2end{$tag->{tag}} = 0 if (!grep(/^$tag->{tag}$/, @toadd));
            }

            foreach my $tag (reverse sort {$id2end{$a->{tag}} <=> $id2end{$b->{tag}}} @tags) {
                if (!grep(/^$tag->{tag}$/, @toadd)) {
                    next;
                }

                my $end_of_annotaion = $self->getEndPosition($tag->{tag}, $position);
                $output .= "<$tag->{tag} id=\"$tag_id\" label=\"$tag->{id}\" refers_to=\"0\" start=\"$position\" end=\"$end_of_annotaion\">";
                $label2id{$tag->{id}} = $tag_id;
                $tag_id++;
                push(@zoznam_otvorenych_tagov, $tag->{tag});
                #print "Otvoril som tag $tag->{tag}. Fronta: @zoznam_otvorenych_tagov\n";
            }

            @previous_tags = @tag_names;
            $output .= "$char";
            $position++;
        }
        
        #foreach my $char (@characters) {
        #    my @tags = $self->getTag($position);
        #    if (scalar(@tags)) {
        #        foreach my $tag (@tags) {
        #            if (!grep(/^$tag$/, @previous_tags)) {
        #                if ($previous_tag and $previous_tag ne "None") {
        #                    $output .= "</$previous_tag>";
        #                }
        #
        #                my $end_of_annotaion = $self->getEndPosition($tag, $position);
        #                $output .= "<$tag id=\"$tag_id\" label=\"$self->{label}\" refers_to=\"0\" start=\"$position\" end=\"$end_of_annotaion\">" if ($tag ne "None");
        #                $label2id{$self->{label}} = $tag_id if ($tag ne "None");
        #                #print "$self->{label} ~~> $tag_id\n" if ($tag ne "None");
        #                $tag_id++ if ($tag ne "None");
        #                $previous_tag = $tag;
        #            }
        #            if ($tag eq $previous_tag) {
        #                $output .= "$char";
        #            }
        #        }
        #    }
        #    else {
        #        $output .= "$char";
        #    }
        #    $position++;
        #}
        $line++;
    }
    close(FILE);

    ## Teraz prejdeme vystup este raz a doplnime data
    ## z anotacii relacii
    my $new_output = "";
    foreach my $line (split(/\n/, $output)) {
        while ($line =~ /<(\w+) id="(\d+)" label="(T\d+)" refers_to="0" start="(\d+)" end="(\d+)">/) {
            #print "LINE = $line\n";
            my $tag = $1;
            my $id = $2;
            my $label = $3;
            my $start = $4;
            my $end = $5;
            #print "\t LABEL = $label\n";

            my $refers_to = 0;
            foreach my $relation (@{$self->{relation}}) {
                #print "\t\t ARG1: '$relation->{arg1}'\t\tARG2: '$relation->{arg2}'\n";
                if ($relation->{arg2} eq "$label") {
                    $refers_to = $label2id{$relation->{arg1}};
                    #print "\t\t NASLI SME. $relation->{arg1} ~~> $label2id{$relation->{arg1}}\n";
                    last;
                }
            }

            #print "\t REFERS TO = $refers_to\n";
            #print "\n";

            if (!$refers_to) {
                $line =~ s/<(\w+) id="(\d+)" label="(T\d+)" refers_to="0" start="(\d+)" end="(\d+)">/<$1 id="$2" label="$3" start="$4" end="$5">/;
                next;
            }

            $line =~ s/<(\w+) id="(\d+)" label="(T\d+)" refers_to="0" start="(\d+)" end="(\d+)">/<$1 id="$2" label="$3" refers_to="$refers_to" start="$4" end="$5">/;
        }
        $new_output .= "$line\n";
    }

    return $new_output;
}

sub xml2ann {
    my ($self, $xml_file) = @_;
    my $output = "";

    ## Otvorime a nacitame XML
    open(XML_FILE, "<$xml_file");
    binmode(XML_FILE, ":encoding(utf8)");
    my $soubor = "";
    while (<XML_FILE>) {
        $soubor .= $_;
    }
    close(XML_FILE);

    ## Hladame tagy
    my $id = 1;
    while ($soubor =~ /<(Zakon|Rozhodnuti_soudu|Vyhlaska|Ucinnost) start='(\d+)' end='(\d+)'>([^<]+)<\/(Zakon|Rozhodnuti_soudu|Vyhlaska|Ucinnost)>/) {
        $output .= "T$id\t$1 $2 $3\t$4\n";
        $id++;
        $soubor =~ s/<(Zakon|Rozhodnuti_soudu|Vyhlaska|Ucinnost) start='(\d+)' end='(\d+)'>([^<]+)<\/(Zakon|Rozhodnuti_soudu|Vyhlaska|Ucinnost)>//;
    }

    return $output;
}

sub addPositionToXML {
    my ($self, $txt_file, $xml_file) = @_;

    ## Zacnem s XML hlavickou
    my $output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    $output   .= "<Document>";

    my $position_txt = 0;

    open(TXT_FILE, "<$txt_file");
    binmode(TXT_FILE, ":encoding(utf8)");

    open(XML_FILE, "<$xml_file");
    binmode(XML_FILE, ":encoding(utf8)");

    while (<TXT_FILE>) {
        chomp($_);
        my @characters = split(//, $_);
        
        my $xml_line = <XML_FILE>;
        chomp($xml_line);

        my $xml_output_line = $xml_line;
        my @xml_line = split(//, $xml_line);

        #print STDERR "XML LINE = $xml_line\n";
        #print STDERR "TXT LINE = $_\n";

        my $position_xml = 0;

        foreach my $char (@characters) {
            if (defined($xml_line[$position_xml]) and
                $char ne $xml_line[$position_xml] and
                $xml_line[$position_xml] eq "<") {

                if ($xml_output_line =~ s/<(\w+) start='(\d+)'>/<$1 start='$2' end='$position_txt'>/ or
                    $xml_output_line =~ s/<(\w+)>/<$1 start='$position_txt'>/) {
                    ## Tiez nic
                }

                $position_xml++ while ($xml_line[$position_xml] ne ">");
                $position_xml++;
            }

            $position_xml++;
            $position_txt++;
        }

        ## Ak sme vypadli z cyklu, znamena to, ze skoncil originalny riadok.
        ## Moze este ale nasledovat uzatvaraci tak nejakeho tagu...
        #print "PO VYSKOCENI Z CYKLU: XML[$position_xml] = $xml_line[$position_xml]\n";
        #print "PO VYSKOCENI Z CYKLU: TXT[$position_txt]";

        if (defined($xml_line[$position_xml]) and $xml_line[$position_xml] eq "<") {
            $xml_output_line =~ s/<(\w+) start='(\d+)'>/<$1 start='$2' end='$position_txt'>/;
        }

        $output .= $xml_output_line . "\n";

        ## Plus jedna pozicia pre \n
        $position_txt++;
    }

    $output .= "</Document>";

    return $output;
}

sub hmm2xml {
    my ($self, $txt_file, $hmm_file) = @_;
    my $output = "";

    my $previous_tag = "";

    open(TXT_FILE, "<$txt_file");
    binmode(TXT_FILE, ":encoding(utf8)");

    open(HMM_FILE, "<$hmm_file");
    binmode(HMM_FILE, ":encoding(utf8)");

    my $line = 1;
    while (<TXT_FILE>) {
        chomp($_);
        my @tokens = $self->tokenization($_);

        #print "LINE = $line\n";
        #print "NUMBER OF TOKENS = " . scalar(@tokens) . "\n";
        #print "TOKENS = " . join(" | ", @tokens) . "\n";
        $line++;

        if (!scalar(@tokens)) {
            my $hmm_output = <HMM_FILE>;
            $output .= "\n";
            next;
        }

        my $index = 0;
        foreach my $token (@tokens) {
            my $hmm_output = <HMM_FILE>;
            chomp($hmm_output);

            #print "\tHMM TOKEN: $hmm_output --> ";

            my @fields = split(/\t/, $hmm_output);
            $hmm_output = $fields[1];

            #print "$hmm_output\n";

            $output .= " " if ($index); # and $token ne ")" and defined($tokens[$index - 1]) and $tokens[$index - 1] ne "(");

            my $znacka_tagu = 0;
            if ($hmm_output ne $previous_tag) {
                if ($previous_tag and $previous_tag ne "None") {
                    $output =~ s/ $//;
                    $output .= "</$previous_tag>";
                    #print "\t*** </$previous_tag>\n";
                    $output .= " " if ($index); # and $token ne ")" and defined($tokens[$index - 1]) and $tokens[$index - 1] ne "(");
                }
                #print "\t*** <$hmm_output>\n";
                $output .= "<$hmm_output>" if ($hmm_output ne "None");
                $previous_tag = $hmm_output;
                $znacka_tagu = 1 if ($previous_tag and $previous_tag ne "None");
            }

            $output .= "$token";

            $index++;
        }

        ## Ak je koniec riadku, musime uzavriet tag
        if ($previous_tag ne "None") {
            $output .= "</$previous_tag>";
            $previous_tag = "None";
        }

        $output .= "\n";

        ## Na konci musim nacitat este jeden riadok, ktory predstavuje
        ## oznacenie konca vety...
        my $hmm_output = <HMM_FILE>;
    }

    return $output;
}

sub vxml2hmm {
    my ($self, $file) = @_;
    my $output = "";

    open(FILE, "<$file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        chomp($_);

        if ($_ =~ /^<\?xml/) {
            next;
        }
        $_ =~ s/^<\/?Document>//;

        my $tag = "None";
        my @segments = split(/[<>]/, $_);
        foreach my $segment (@segments) {
            if ($segment =~ /^(Zakon|Rozhodnuti_soudu|Ucinnost|) position='\d+'$/) {
                $tag = $1;
                next;
            }
            if ($segment =~ /^\/.* position='\d+'$/) {
                $tag = "None";
                next;
            }

            ## Tokenizacia
            my @tokens = $self->tokenization($segment);
            foreach my $token (@tokens) {
                $output .= "$token\t$tag\n";
            }
        }

        $output .= "\n";
    }

    return $output;
}

sub xml2hmm {
    my ($self, $file) = @_;
    my $output = "";

    open(FILE, "<$file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        chomp($_);

        my $tag = "None";

        my @segments = split(/[<>]/, $_);
        foreach my $segment (@segments) {
            if ($segment =~ /^(Zakon|Rozhodnuti_soudu|Vyhlaska|Ucinnost) position='\d+'$/) {
                $tag = $1;
                next;
            }
            if ($segment =~ /^\/.* position='\d+'$/) {
                $tag = "None";
                next;
            }

            ## Tokenizacia
            my @tokens = $self->tokenization($segment);
            foreach my $token (@tokens) {
                $output .= "$token\t$tag\n";
            }
        }

        $output .= "\n";
    }

    return $output;
}

sub txtPreprocessing {
    my ($self, $txt_file) = @_;
    my $output = "";

    open(TXT_FILE, "<$txt_file");
    binmode(TXT_FILE, ":encoding(utf8)");
    while (<TXT_FILE>) {
        chomp($_);
        $output .= join(" ", $self->tokenization($_)) . "\n";
    }
    close(TXT_FILE);

    return $output;
}

sub tokenization {
    my ($self, $text) = @_;

    #print "TEXT = $text\n";

    ## Upravy pred tokenizaciou
    ## -- pridam medzeru medzi zatvorku a text a medzi uvodzovky a text
    $text =~ s/(„|\()([^\s])/$1 $2/g;
    $text =~ s/([^\s])(\)|“|:)/$1 $2/g;

    ## -- specialne musim osetrit uvodzovku a zatvorku
    $text =~ s/(“)(\))/$1 $2/g;

    #print "TEXT = $text\n\n\n";
    
    ## Tokenizacia
    my @tokens = split(/\s+/, $text);

    return @tokens;
}

sub getDataForTagger {
    my ($self, $file) = @_;

    $self->{positions} = [];

    my $position_prazdneho_zasobnika = 0;
    my $position = 0;
    my $output = "";

    open(FILE, "<$file");
    my $line = 0;
    while (<FILE>) {
        $line++;

        my $text = lc($self->removeDiacritic($_));

        my @characters = split(//, $text);
        my $token = 0;
        my $tag = "None";
        my $zasobnik = "";

        foreach my $znak (@characters) {
            my $tag_znaku = $self->getTag($position);

            #$output .= "\tZNAK = $znak | POSITION = $position | ZASOBNIK = $zasobnik\n";
            
            if ($znak =~ /(\n|\s|\(|\)|,|"|\/)/) {
                if ($tag_znaku) {
                    if ($zasobnik) {
                        $zasobnik = $self->normalizeWord($zasobnik);
                        $output .= "$zasobnik\t$tag\n";
                        push(@{$self->{positions}}, $position_prazdneho_zasobnika);
                    }

                    if ($znak !~ /\s/ and $znak !~ /\n/) {
                        $output .= "$znak\t$tag_znaku\n";
                        push(@{$self->{positions}}, $position);
                    }
                }
                else {
                    if ($zasobnik) {
                        $zasobnik = $self->normalizeWord($zasobnik);
                        $output .= "$zasobnik\n";
                        push(@{$self->{positions}}, $position_prazdneho_zasobnika);
                    }

                    if ($znak !~ /\s/ and $znak !~ /\n/) {
                        $output .= "$znak\n";
                        push(@{$self->{positions}}, $position);
                    }
                }

                $zasobnik = "";
                $tag = $tag_znaku;
                $position++;
                $position_prazdneho_zasobnika = $position;
                next;
            }

            $zasobnik .= "$znak";
            $tag = $tag_znaku;
            $position++;
        }

        push(@{$self->{positions}}, -1);
        $output .= "\n";
    }
    close(FILE);

    #print "POCET POZICII = " . scalar(@{$self->{positions}}) . "\n";
    
    return $output;
}

sub savePositions {
    my ($self, $filename) = @_;

    open(FILE, ">$filename");
    foreach my $position (@{$self->{positions}}) {
        print FILE "$position\n";
    }
    close(FILE);

    return 1;
}

sub getTag {
    my ($self, $position) = @_;

    my @results = ();

    if (!defined($self->{annotation}) or
        !scalar(@{$self->{annotation}})) {
        return "";
    }

    foreach my $annotation (@{$self->{annotation}}) {
        if ($annotation->{start} <= $position and
            $annotation->{end} > $position) {
            #$self->{label} = $annotation->{id};
            push(@results, {tag => "$annotation->{tag}", id => "$annotation->{id}"});
        }
    }

    return sort {$a->{tag} cmp $b->{tag}} @results;
}

sub getEndPosition {
    my ($self, $tag, $position) = @_;

    if (!defined($self->{annotation}) or
        !scalar(@{$self->{annotation}})) {
        return 0;
    }

    foreach my $annotation (@{$self->{annotation}}) {
        if ($annotation->{tag} eq $tag and
            $annotation->{start} <= $position and
            $annotation->{end} > $position) {
            return $annotation->{end};
        }
    }

    return 0;
}

sub normalizeWord {
    my ($self, $word) = @_;

    #if ($word =~ /\d/) {
    #    my $pocet_cislic = grep(/\d/, split(//, $word));
    #    $word = "NUM$pocet_cislic";
    #}

    return $word;
}

sub getXML {
    my ($self, $txt_file, $out_file) = @_;

    ## Naparsujem si zoznam tagov do pola
    my @tagy = ();

    open(FILE, "<$out_file");
    while (<FILE>) {
        chomp($_);
        #print "LINE = $_\n";
        if ($_ =~ /^(.*)[\t\s]+(.*)\t.*$/) {
            #print "$1\n$2\n";
            push(@tagy, $2);
            next;
        }

        push(@tagy, "None");
    }
    close(FILE);

    ## Zavolam getDataForTagger a ziskam tym pozicie, na
    ## ktorych by mali zacinat jednotlive tokeny!
    $self->getDataForTagger($txt_file);

    #print join("\n", @{$self->{positions}});

    ## Teraz budem znak po znaku vypisovat subor a vzdy vrazim
    ## XML tag ak napocitam zlomovu poziciu...
    my $position = 0;
    my $last_tag = "";
    my $output = "";

    #print "POCET TAGOV = " . scalar(@tagy) . "\n";
    #print "POCET POZIC = " . scalar(@{$self->{positions}}) . "\n";
    #
    #print "TAGY = @tagy\n";
    
    #return "";

    open(FILE, "<$txt_file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        foreach my $char (split(//, $_)) {
            #print "POSITION = $position\tCHAR = $char\n";

            my $index = -1;
            for (my $i = 0; $i < scalar(@tagy); $i++) {
                if ($self->{positions}[$i] == $position) {
                    $index = $i;
                    last;
                }
            }

            #print "INDEX: $index\n";

            if ($index != -1) {
                $output  .= "</$last_tag>" if ($last_tag and $tagy[$index] ne $last_tag and $last_tag ne "None");
                $output  .= "<$tagy[$index]>" if ($tagy[$index] ne $last_tag and $tagy[$index] ne "None");
                $last_tag = $tagy[$index];
            }

            $output .= $char;
            $position++;
        }
    }

    return $output;
}

sub removeDiacritic {
    my ($self, $text) = @_;

    $text =~ s/á/a/g;
    $text =~ s/č/c/g;
    $text =~ s/ď/d/g;
    $text =~ s/é/e/g;
    $text =~ s/ě/e/g;
    $text =~ s/í/i/g;
    $text =~ s/ĺ/l/g;
    $text =~ s/ľ/l/g;
    $text =~ s/ň/n/g;
    $text =~ s/ó/o/g;
    $text =~ s/ô/o/g;
    $text =~ s/ř/r/g;
    $text =~ s/ŕ/r/g;
    $text =~ s/š/s/g;
    $text =~ s/ť/t/g;
    $text =~ s/ú/u/g;
    $text =~ s/ů/u/g;
    $text =~ s/ý/y/g;
    $text =~ s/ž/z/g;

    $text =~ s/Á/A/g;
    $text =~ s/Č/C/g;
    $text =~ s/Ď/D/g;
    $text =~ s/É/E/g;
    $text =~ s/Ě/E/g;
    $text =~ s/Í/I/g;
    $text =~ s/Ĺ/L/g;
    $text =~ s/Ľ/L/g;
    $text =~ s/Ň/N/g;
    $text =~ s/Ó/O/g;
    $text =~ s/Ö/O/g;
    $text =~ s/Ŕ/R/g;
    $text =~ s/Ŕ/R/g;
    $text =~ s/Š/S/g;
    $text =~ s/Ť/T/g;
    $text =~ s/Ú/U/g;
    $text =~ s/Ů/U/g;
    $text =~ s/Ý/Y/g;
    $text =~ s/Ž/Z/g;

    $text =~ s/…/#/g;
    $text =~ s/„/"/g;
    $text =~ s/“/"/g;
    $text =~ s/§/\$/g;
    $text =~ s/–/-/g;

    return $text;
}

sub fishingRules {
    my ($self, $input_xml, $output_xml) = @_;

    my %texty_v_tagoch = ();

    open(INPUT, "<$input_xml");
    open(OUTPUT, ">$output_xml");

    while (<INPUT>) {
        chomp($_);

        ## Ulozim si texty, aby som nasledne tagoval konzistente...
        my $text = $_;
        while ($text =~ s/<Zakon>([^<]+)<\/Zakon>//) {
            $texty_v_tagoch{zakon}{$1} = 1;
        }

        ## Rozhodnutia sudu
        ##  - ak nasleduje cislo alebo lomitko a cislo, zaradime to
        ##    k predchadzajucej znacke
        $_ =~ s/(\d+\s*)(<Rozhodnuti_soudu>)/$2$1/g;
        $_ =~ s/(<\/Rozhodnuti_soudu>)(\s*\d+)/$2$1/g;
        $_ =~ s/(<\/Rozhodnuti_soudu>)(\/\s*[\d\-]+)/$2$1/g;

        ## Ucinnost
        ##  - ak je za tagom ucinnost datum, rozsirime znacku az za jeho koniec
        $_ =~ s/(<\/Ucinnost>)([^<]+\d{1,2}\.\s*\d{1,2}\.\s*\d{2,4})/$2$1/g;
        $_ =~ s/(<\/Ucinnost>)(\s*p.edpis.)([,\.\s+])/$2$1$3/g;
        $_ =~ s/(<\/Ucinnost>)(\s*\d{4})/$2$1/g;

        ## Zakon
        ##  - skusime do znacky pridat aj nejake dalsie slova, ktore model zabudol
        $_ =~ s/(<\/Zakon>)(\sz.*?k(?:on[^\s]*?ku)?)([,\.\s+])/$2$1$3/g;
        $_ =~ s/(za\s*)(<\/Zakon>)(\sst.*?edn.*?kem)/$1$3$2/g;
        $_ =~ s/(p[^\s]*?sm\.\s*)(<\/Zakon>)(\s*[a-z]\/)/$1$3$2/g;
        $_ =~ s/(v.*ta\s*)(<\/Zakon>)(\s*druh.*)([,\s])/$1$3$2$4/g;
        $_ =~ s/(odst\.\s*)(<\/Zakon>)(\s*\d+)/$1$3$2/g;
        $_ =~ s/\s+([a-z]\/)<\/Zakon>([,\s]*)<Zakon>([a-b]\/)/$1$2$3/g;
        $_ =~ s/<Zakon>(v..ty|v..ta)<\/Zakon>/$1/g;

        ## Zakon
        ##  - skusime pridat do znacky texty typu "cl. II"
        $_ =~ s/(\()(.*?l\.\s+[IVXLCDM]+[,\s]+)(<Zakon>)/$1$3$2/g;

        ## Prehodime tak, aby medzery a ciarky boli za koncom tagu
        $_ =~ s/([\s,])(<\/[^>]+>)/$2$1/g;

        print OUTPUT "$_\n";
    }

    close(INPUT);
    close(OUTPUT);

    return 1;
}

sub evaluation {
    my ($self, $goldstandard_xml, $tested_xml) = @_;

    ## Nacitam data do pola
    my @gs = $self->_readFileForEvaluation($goldstandard_xml);
    my @te = $self->_readFileForEvaluation($tested_xml);

    ## Pocet tokenov musi sediet
    #if (scalar(@gs) != scalar(@te)) {
    #    print STDERR "ERROR: There is a different number of tokens in test and goldstandard files:\n";
    #    print STDERR "\tNumber of tokens in GS: \t" . scalar(@gs) . "\n";
    #    print STDERR "\tNumber of tokens in TEST: \t" . scalar(@te) . "\n";
    #    return 0;
    #}

    print "Tag\t\tA\tB\tC\tD\n";

    ## Prechadzam jednotlive tagy a vyplnam maticu konfuzie
    #foreach my $tag ("Zakon", "Rozhodnuti_soudu", "Vyhlaska", "Ucinnost") {
    foreach my $tag ("Rozhodnuti_soudu") {
        my %confusion_matrix = (
            tagger_yes_true_yes => 0,
            tagger_yes_true_no => 0,
            tagger_no_true_yes => 0,
            tagger_no_true_no => 0
        );

        for (my $i = 0; $i < scalar(@gs); $i++) {
            #print "TOKEN $i: $gs[$i]->{token}:$gs[$i]->{tag} vs. $te[$i]->{token}:$te[$i]->{tag}\n";

            ## Kontrola na zhodnost tokenov
            if ($gs[$i]->{token} ne $te[$i]->{token}) {
                print STDERR "ERROR: Tokens differ in test and goldstandard files:\n";
                print STDERR "\tToken id $i in GS: \t" . $gs[$i]->{token} . "\n";
                print STDERR "\tToken in $i in TEST: \t" . $te[$i]->{token} . "\n";
                next;
            }

            ## Teraz pripocitame token jednej bunky matice
            if ($gs[$i]->{tag} eq $tag and
                $gs[$i]->{tag} eq $te[$i]->{tag}) {
                $confusion_matrix{tagger_yes_true_yes}++;
                next;
            }

            if ($gs[$i]->{tag} eq $tag and
                $gs[$i]->{tag} ne $te[$i]->{tag}) {
                $confusion_matrix{tagger_no_true_yes}++;
                #print STDERR "\tTOKEN\t$i\t$gs[$i]->{token}\t$gs[$i]->{tag}\t$te[$i]->{tag}\n";
                #print STDERR "\t\tKONTEXT: ";
                #for (my $j = $i - 5; $j < $i + 5; $j++) {
                #    print STDERR "$gs[$j]->{token}:$gs[$j]->{tag} ";
                #}
                #print STDERR "\n";
                next;
            }

            if ($gs[$i]->{tag} ne $tag and
                $tag ne $te[$i]->{tag}) {
                $confusion_matrix{tagger_no_true_no}++;
                next;
            }

            if ($gs[$i]->{tag} ne $tag and
                $tag eq $te[$i]->{tag}) {
                $confusion_matrix{tagger_yes_true_no}++;
                #print STDERR "\tTOKEN\t$i\t$gs[$i]->{token}\t$gs[$i]->{tag}\t$te[$i]->{tag}\n";
                #print STDERR "\t\tKONTEXT: ";
                #for (my $j = $i - 5; $j < $i + 5; $j++) {
                #    print STDERR "$gs[$j]->{token}:$gs[$j]->{tag} ";
                #}
                #print STDERR "\n";
                next;
            }
        }

        ## Vypisem (zatial maticu)
        print "$tag\t\t$confusion_matrix{tagger_yes_true_yes}\t$confusion_matrix{tagger_no_true_yes}\t$confusion_matrix{tagger_yes_true_no}\t$confusion_matrix{tagger_no_true_no}\n";
    }

    ## Teraz mocou ANN formatu spocitame dalsie 3 zaujimave cisla, ktore sa nepocitaju po tokenoch, ale po
    ## tagoch...
    my $gs = $self->xml2ann($goldstandard_xml);
    my $te = $self->xml2ann($tested_xml);

    
    
    return 1;
}

sub _getAnnDataForEvaluation {
    my ($self, $xml_filename) = @_;

    my $data = $self->xml2ann($xml_filename);

    my %data = ();
    foreach my $line (split(/\n/, $data)) {
        if ($line !~ /(\w+)\t(\w+) (\d+) (\d)/) {
            next;
        }

        $data{$1}{tag} = $2;
        $data{$1}{start} = $3;
        $data{$1}{end} = $4;
        $data{$1}{used} = 0;
    }

    return %data;
}

sub ruleBasedTagger {
    my ($self, $input_xml, $output_xml) = @_;

    open(INPUT, "<$input_xml");
    binmode(INPUT, ":encoding(utf8)");
    open(OUTPUT, ">$output_xml");
    binmode(OUTPUT, ":encoding(utf8)");

    while (<INPUT>) {
        chomp($_);

        ## Rozhodnutia sudu
        $_ =~ s/(č(?:\.\s)?j. \d+ [A-Za-z]+ \d+\/\d{4}\s*-\s*\d+)/<Rozhodnuti_soudu>$1<\/Rozhodnuti_soudu>/g;
        $_ =~ s/(sp\. zn\. [^\d]+ \d+\/\d{2})/<Rozhodnuti_soudu>$1<\/Rozhodnuti_soudu>/g;

        print OUTPUT "$_\n";
    }

    close(INPUT);
    close(OUTPUT);

    return 1;
}

1;