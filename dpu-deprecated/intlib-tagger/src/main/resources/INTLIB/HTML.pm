#!/usr/bin/perl

## Author: Vincent Kriz, 2012
## E-mail: kriz@ufal.mff.cuni.cz

my $debug = 0;

use strict;
use warnings;

package INTLIB::HTML;

sub new {
    my $self = {};

    bless $self;
    return $self;
}

## Ulozime si cely subor do pola po jednotlivych riadkoch.
sub loadFile {
    my ($self, $filname) = @_;

    $self->{original_data} = [];

    open(FILE, "<$filname");
    while (<FILE>) {
        chomp($_);
        push(@{$self->{original_data}}, $_);
    }
    close(FILE);
}

## Vytvorime hierarchicku strukturu z HTML, ktory
## generuje server zakonyprolidy.cz
##
## Malo by to byt tak, ze z kazdeho riadku by som mal ziskat jednu
## strukturu, ktoru som si vymyslel. Struktura by sa nejak
## po jej naparsovani vyhodnotila a zaradila do hierarchie a doplnil by
## sa jej atribut order. Predpokladom pre toto je, ze kazdy riadok
## obsahuje prave data potrebne na zostavenie 1 strkutury
sub parse {
    my ($self) = @_;

    $self->{data} = [];

    my @pointre_na_nadradene_struktury = ($self->{data}, undef, undef, undef, undef, undef);
    my @poradie_struktur = (1, 1, 1, 1, 1, 1);

    my $minula_struktura = undef;
    my $aktualna_struktura = undef;

    my $posunutie_indexu = 0;
    my $pozicia_posunuteho_indexu = 0;

    foreach my $line (@{$self->{original_data}}) {
        my $structure = $self->_parseLine($line);

        if ($structure->{type} eq "title") {
            push(@{$aktualna_struktura->{title}}, $structure->{title});
            next;
        }

        if ($structure->{type} eq "text") {
            $aktualna_struktura->{text} = $structure->{text};
            next;
        }

        if ($structure->{type} =~ /^\d+$/) {
            $minula_struktura = $aktualna_struktura;
            $aktualna_struktura = $structure;

            #print "NACITANA STRUKTURA UROVNE $structure->{type}\n";
            #print "\tMinula: $minula_struktura->{type}\n";
            #print "\tAktualna: $aktualna_struktura->{type}\n";

            $structure->{order} = $poradie_struktur[$structure->{type} - 1];
            $poradie_struktur[$structure->{type} - 1]++;

            if (!$posunutie_indexu and
                $aktualna_struktura->{type} > $minula_struktura->{type} and
                abs($minula_struktura->{type} - $aktualna_struktura->{type}) > 1) {
                $posunutie_indexu = 1;
                $pozicia_posunuteho_indexu = $minula_struktura->{type};

                #print "\t******* NASTAVUJEM POSUNUTIE ($pozicia_posunuteho_indexu) *******\n";
            }

            if ($posunutie_indexu and $pozicia_posunuteho_indexu < $aktualna_struktura->{type}) {
                #print "\t******* ZNIZUJEM UROVEN O JEDNA *******\n";
                $structure->{type}--;
            }

            if ($posunutie_indexu and $aktualna_struktura->{type} == $pozicia_posunuteho_indexu) {
                $posunutie_indexu = 0;
            }

            if ($structure->{type} == 1) {
                push(@{$self->{data}}, $structure);
                #print "Vkladam do pola \$self->{data}\n";
            }
            else {
                push(@{$pointre_na_nadradene_struktury[$structure->{type} - 2]->{sub}}, $structure);
                #print "Vkladam do pola sub sktruktury na indexe $structure->{type} - 1\n";
            }
            $pointre_na_nadradene_struktury[$structure->{type} - 1] = $structure;

            for (my $i = $structure->{type}; $i < scalar(@pointre_na_nadradene_struktury); $i++) {
                $pointre_na_nadradene_struktury[$i] = undef;
                $poradie_struktur[$i] = 1;
            }
        }
    }
}

## Ulohou tejto metody je naparsovat jeden riadok a vratit
## strukturu, ktoru niekto nad nami spravne zaradi do hierarchie.
sub _parseLine {
    my ($self, $line) = @_;

    my %structure = ();
    $structure{type} = "";
    $structure{marker} = "";
    $structure{text} = "";
    $structure{title} = [];
    $structure{sub} = [];

    if ($line =~ /^.*<p id="[^"]+" class="([^"]+)">(.*)<\/p>$/) {
        my $type = $1;
        my $text = $2;

        if ($type =~ /PARA/) {
            $structure{marker} = $text;
            $text = "";
        }

        if ($type =~ /.*(\d+).*/) {
            $structure{type} = $1;
        }

        if ($type =~ /.*(\d+)\s*$/) {
            $structure{type} = "text";
            $structure{text} = $text;

            return \%structure;
        }

        ## Extrahujeme marker
        if ($text =~ s/<var>(.*)<\/var>//) {
            $structure{marker} = $1;
        }

        $text =~ s/^\s+//;
        $text =~ s/\s+$//;
        $structure{text} = $text;
    }

    if ($line =~ /^.*<h3 id="[^"]+" class="([^"]+)">(.*)<\/h3>$/) {
        $structure{type} = "title";
        $structure{title} = $2;
    }

    return \%structure;
}

## Z textu odstranime vsetky HTML tagy a znacky. Chceme zatial zisakt cisty text
sub clean {
    my ($self, $ra_structures) = @_;

    if (!defined($ra_structures)) {
        $ra_structures = $self->{data};
    }
    
    foreach my $structure (@{$ra_structures}) {
        foreach my $title (@{$structure->{title}}) {
            $title =~ s/<sup>[^<]+<\/sup>\)?//g;
            $title =~ s/<[^>]+>//g;
        }

        $structure->{text} =~ s/<sup>[^<]+<\/sup>\)?//g;
        $structure->{text} =~ s/<[^>]+>//g;

        $self->clean($structure->{sub});
    }
}

sub expand {
    my ($self, $prefix, $ra_structures) = @_;

    if (!defined($prefix)) {
        $prefix = "";
    }

    if (!defined($ra_structures)) {
        $ra_structures = $self->{data};
    }

    foreach my $structure (@{$ra_structures}) {
        my $prefix = $prefix;

        ## Ak nie je text, zmazeme prefix
        if (!$structure->{text}) {
            ## Titulok, ak existuje
            foreach my $title (@{$structure->{title}}) {
                print "$title\n";
            }

            $self->expand("", $structure->{sub});
            next;
        }

        ## Ak je text a mame potomkou, tak nic nepiseme, ale
        ## zapamatame si to do prefixu
        if (scalar(@{$structure->{sub}})) {
            if ($self->removeDiacritic($structure->{sub}[0]->{text}) =~ /^[abcdefghijklmnopqrstuvwxyzáčďěéíľĺňóôřŕšťúůýž]/) {
                #print "\n*** $structure->{type}: MAME POD NAMI ZOZNAM, UKLADAME PREFIX***\n";
                $prefix .= " $structure->{text}" if ($prefix);
                $prefix .= "$structure->{text}" if (!$prefix);
                
                ## Titulok, ak existuje
                foreach my $title (@{$structure->{title}}) {
                    print "$title\n";
                }
            }
            else {
                #print "\n*** $structure->{type}: POD NAMI NOVA VETA ($structure->{sub}[0]->{text}), TLACIME OBSAH***\n";
                print "$structure->{text}\n";
         
                ## Titulok, ak existuje
                foreach my $title (@{$structure->{title}}) {
                    print "$title\n";
                }
            }
        }

        ## Ak nie je, tlacime
        if (!scalar(@{$structure->{sub}})) {
            $prefix =~ s/:$//;
            $structure->{text} =~ s/[,;]$/./;
            if ($prefix and $self->removeDiacritic($structure->{text}) =~ /^[abcdefghijklmnopqrstuvwxyzáčďěéíľĺňóôřŕšťúůýž]/) {
                #print "\n*** $structure->{type}: TEXT ZACINA MALYM PISMENOM ***\n";
                print "$prefix ";
            }
            print "$structure->{text}\n";
        }

        $self->expand($prefix, $structure->{sub});
    }
}

sub printStructure {
    my ($self, $ra_structures, $prefix) = @_;

    if (!defined($ra_structures)) {
        $ra_structures = $self->{data};
    }

    if (!defined($prefix)) {
        $prefix = "";
    }

    foreach my $structure (sort {$a->{order} <=> $b->{order}} @{$ra_structures}) {
        print "$prefix" . "TYPE:\t$structure->{type}\n";
        print $prefix . "MARKER:\t$structure->{marker}\n";
        foreach my $title (@{$structure->{title}}) {
            print $prefix . "TITLE:\t$title\n";
        }
        print $prefix . "TEXT:\t$structure->{text}\n";
        $self->printStructure($structure->{sub}, "$prefix\t");
    }

    print "\n";

    return 1;
}

sub removeDiacritic {
    my ($self, $text, $use_utf8) = @_;

    #if ($use_utf8) {
    #    use utf8;
        
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
        #print "REMOVE DIA = $text\n";
        $text =~ s/ú/u/g;
        #print "REMOVE DIA = $text\n";
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
        $text =~ s/Ŕ/R/g;
        $text =~ s/Ŕ/R/g;
        $text =~ s/Š/S/g;
        $text =~ s/Ť/T/g;
        $text =~ s/Ú/U/g;
        $text =~ s/Ů/U/g;
        $text =~ s/Ý/Y/g;
        $text =~ s/Ž/Z/g;

        return $text;
    #}
    #
    #$text =~ s/á/a/g;
    #$text =~ s/č/c/g;
    #$text =~ s/ď/d/g;
    #$text =~ s/é/e/g;
    #$text =~ s/ě/e/g;
    #$text =~ s/í/i/g;
    #$text =~ s/ĺ/l/g;
    #$text =~ s/ľ/l/g;
    #$text =~ s/ň/n/g;
    #$text =~ s/ó/o/g;
    #$text =~ s/ô/o/g;
    #$text =~ s/ř/r/g;
    #$text =~ s/ŕ/r/g;
    #$text =~ s/š/s/g;
    #$text =~ s/ť/t/g;
    #$text =~ s/ú/u/g;
    #$text =~ s/ů/u/g;
    #$text =~ s/ý/y/g;
    #$text =~ s/ž/z/g;
    #
    #$text =~ s/Á/A/g;
    #$text =~ s/Č/C/g;
    #$text =~ s/Ď/D/g;
    #$text =~ s/É/E/g;
    #$text =~ s/Ě/E/g;
    #$text =~ s/Í/I/g;
    #$text =~ s/Ĺ/L/g;
    #$text =~ s/Ľ/L/g;
    #$text =~ s/Ň/N/g;
    #$text =~ s/Ó/O/g;
    #$text =~ s/Ŕ/R/g;
    #$text =~ s/Ŕ/R/g;
    #$text =~ s/Š/S/g;
    #$text =~ s/Ť/T/g;
    #$text =~ s/Ú/U/g;
    #$text =~ s/Ů/U/g;
    #$text =~ s/Ý/Y/g;
    #$text =~ s/Ž/Z/g;
    #
    #return $text;
}


1;