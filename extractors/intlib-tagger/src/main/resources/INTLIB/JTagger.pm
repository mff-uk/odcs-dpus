#!/usr/bin/perl

## Author: Vincent Kriz, 2012
## E-mail: kriz@ufal.mff.cuni.cz

## This library contains methods for tokenization
## of the text of judicatures

my $debug = 0;

use strict;
use warnings;
use utf8;

package INTLIB::JTagger;

sub new {
    my $self = {};

    bless $self;
    return $self;
}

sub tokenization {
    my ($self, $text, $just_tokenize) = @_;

    ## Odstranim medzery pred a za...
    $text =~ s/^\s*//;
    $text =~ s/\s*$//;

    ## Upravy pred tokenizaciou
    ## -- pridam medzeru medzi zatvorku a text a medzi uvodzovky a text
    $text =~ s/(„|\(|\[|"|-|:|\.|…|â|€)([^\s])/$1 $2/g;
    $text =~ s/(„|\(|\[|"|-|:|\.|…|â|€)([^\s])/$1 $2/g;
    $text =~ s/([^\s])(-|"|\]|\)|“|:|\.|,|;|\/|â|€)/$1 $2/g;
    $text =~ s/([^\s])(-|"|\]|\)|“|:|\.|,|;|\/|â|€)/$1 $2/g;

    ## -- specialne musim osetrit uvodzovku a zatvorku
    $text =~ s/(“)(\))/$1 $2/g;

    ## Este doplnim specialne pravidla, ktore mi opravia preklepy v datach...
    $text =~ s/([^\s])(§)/$1 $2/g;
    
    ## Tokenizacia
    my @tokens = split(/\s+/, $text);

    ## Ak robime len tokenizaciu, tak tu koncime...
    if (defined($just_tokenize) and $just_tokenize) {
        return @tokens;
    }

    ## Na tomto mieste skusim tokeny nejak zjednodusit -- zlemmatizovat
    ## ostemovat alebo nahradit cisla a pod...
    foreach my $token (@tokens) {
        ## Prevod na male pismenka
        $token = lc($token);

        ## Cisla nahradim jednickou (z implementacnych dovodou)
        $token =~ s/^\d+$/1/;

        ## Cislo lomeno cislo nahradim tagom #slash
        $token =~ s/^\d+\/\d+(?:-\d+)?$/#slash#/;
    }
    
    return @tokens;
}

sub vxml2hmm {
    my ($self, $mode, $ra_tagset, $file) = @_;
    my $output = "";

    my $tagset_regexp = "(" . join("|", @{$ra_tagset}) . ")";

    open(FILE, "<$file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        chomp($_);

        if ($_ =~ /^<\?xml/) {
            next;
        }
        $_ =~ s/^<\/?Document>//;

        my $tag = "None";
        my @segments = split(/</, $_);
        foreach my $segment (@segments) {
            #print "SEGMENT = $segment\n";
            if ($segment =~ s/^$tagset_regexp[^>]*>(.*)$/$2/) {
                #print "TAG $1\n";
                $tag = $1;
            }
            elsif ($segment =~ s/^\/[^>]+>(.*)$/$1/) {
                $tag = "None";
            }
            elsif ($segment =~ s/^([^\/>]+)>(.*)/$2/) {
                $tag = "None";
            }
            #print "SEGMENT = $segment\n";
            #print "******************************\n\n\n\n\n";

            ## Tokenizacia
            my @tokens = $self->tokenization($segment);
            foreach my $token (@tokens) {
                $output .= "$token\t$tag\n" if ($mode eq "train");
                $output .= "$token\n" if ($mode eq "test"); 
            }
        }

        $output .= "\n";
    }

    return $output;
}

sub hmm2vxml {
    my ($self, $hmm_file, $vxml_file) = @_;
    my $output = "";
    my $previous_tag = "";

    open(VXML_FILE, "<$vxml_file");
    binmode(VXML_FILE, ":encoding(utf8)");

    open(HMM_FILE, "<$hmm_file");
    binmode(HMM_FILE, ":encoding(utf8)");

    my $line = 1;
    while (<VXML_FILE>) {
        chomp($_);

        if ($_ =~ /^<\?xml/) {
            next;
        }
        $_ =~ s/^<\/?Document>//;

        my @tokens = $self->tokenization($_, 1);
        $line++;

        if (!scalar(@tokens)) {
            my $hmm_output = <HMM_FILE>;
            $output .= "\n";
            next;
        }

        #print "LINE = $line\n";
        #print "POCET TOKENOV = " . scalar(@tokens) . "\n";
        #print "TOKENY = " . join(" ", @tokens) . "\n";

        ## Nacitam si z HMM tolko tokenov, kolko mam tokenov na VXML riadku...
        my @hmm_tokens = ();
        for (my $i = 1; $i <= scalar(@tokens); $i++) {
            my $hmm_output = <HMM_FILE>;
            chomp($hmm_output);
            #print "\t$i: $hmm_output\n";
            my @fields = split(/\t/, $hmm_output);
            push(@hmm_tokens, "$fields[1]");
        }

        ## Nacitame este jeden riadok ako rozdelovnik viet...
        my $hmm_output = <HMM_FILE>;

        foreach my $token (@tokens) {
            $token =~ s/([\[\]\(\)\?\*\+\.\/\\])/\\$1/g;
        }

        ## Teraz hladam zmeny v hmm_tokens a pre kazdu spravim tag
        my @previous = ();
        my @news = @tokens;
        my $previous_tag = "";

        foreach my $tag (@hmm_tokens) {
            if ($previous_tag ne $tag) {
                if ($tag ne "None") {
                    my $previous = join("(?:<[^>]+>)?\\s*(?:<[^>]+>)?", @previous);
                    my $next = join("(?:<[^>]+>)?\\s*(?:<[^>]+>)?", @news);

                    $_ =~ s/($previous(?:<[^>]+>)?\s*(?:<[^>]+>)?)($next)/$1<$tag>$2/;
                }

                if ($previous_tag ne "None" and $previous_tag ne "") {
                    my $previous = join("(?:<[^>]+>)?\\s*(?:<[^>]+>)?", @previous);
                    my $next = join("(?:<[^>]+>)?\\s*(?:<[^>]+>)?", @news);

                    $_ =~ s/($previous)((?:<[^>]+>)?\s*(?:<[^>]+>)?$next)/$1<\/$previous_tag>$2/;
                }
            }

            $previous_tag = $tag;
            push(@previous, shift(@news));
        }

        ## Ak nakoniec ostal este nejaky tag v $previous, musime ho uzatvorit...
        if ($previous_tag ne "None" and $previous_tag ne "") {
            $_ =~ s/$/<\/$previous_tag>/;
        }

        $output .= "$_\n";
    }

    return $output;
}

sub rulebasedVxmlValidation {
    my ($self, $filename) = @_;
    #binmode(STDERR, ":encoding(utf8)");

    my $output = "";

    open(VXML_FILE, "<$filename");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        ## Natvrdo zoznam stringov, ktore su zakon!
        $_ =~ s/(občansk(ý|ého|ému|ém|ým) soudní(ho|mu|m)? řád(u|em)?)/<Zakon>$1<\/Zakon>/g;

        ## Hned zaroven odstranim tieto znacky, ak som ich vlozil do existujuceho tagu
        $_ =~ s/(<Zakon[^>]*>[^<]*)<Zakon>/$1/;
        $_ =~ s/<\/Zakon[^>]*>([^<]*<\/Zakon>)/$1/;

        ## Sama bodka nie je zakon
        while ($_ =~ s/<Zakon[^>]*>(\.+)<\/Zakon>/$1/) {
            #print "MECUJEM RIADOK $_\n";
        }

        ## Rozdhonutie sudu nesmie obsahovat datum
        while ($_ =~ s/(<Rozhodnuti_soudu[^>]*>)([^<>]*?[012]?\d\.\s*[01]?\d+\.\s*\d{4}\s*)(.*)(<\/Rozhodnuti_soudu>)/$2$1$3$4/) {
            #print "MECUJEM RIADOK $_\n";
        }

        ## Zakon nesmie obsahovat datum (USCR)
        while ($_ =~ s/(<Zakon[^>]*>)([^<>]*?[012]?\d\.\s*[01]?\d+\.\s*\d{4}\s*)([^<]*)(<\/Zakon>)/$2$3/) {
            #print "MECUJEM RIADOK $_\n";
        }
        
        ## Zakon o ustavni soudu doanotujeme, ak pred tym nie je Sb. (USCR)
        while ($_ =~ s/(<Zakon[^>]*>)([^<]+)(<\/Zakon>)( Ústavním soudu)/$1$2$4$3/) {
            #print "MECUJEM RIADOK $_\n";
        }
        
        ## Zakon nie je text "základních práv a svobod" (USCR)
        while ($_ =~ s/(<Zakon[^>]*>)((?:zachování )?základních práv a svobod\.?)(<\/Zakon>)/$2/) {
            #print "MECUJEM RIADOK $_\n";
        }
        
        ## Zakon a Rozhodnuti_soudu nie je text "c. \d+" (USCR)
        while ($_ =~ s/(<(?:Zakon|Rozhodnuti_soudu)[^>]*>)(č\. (?:p\. )?\d+\.?)(<\/(?:Zakon|Rozhodnuti_soudu)>)/$2/) {
            #print "MECUJEM RIADOK $_\n";
        }
        
        ## Rozhodnuti_soudu nie je text "c. \d+/\d+" (USCR)
        while ($_ =~ s/(<(?:Rozhodnuti_soudu)[^>]*>)(č\. \d+\/\d+)(<\/(?:Rozhodnuti_soudu)>)/$2/) {
            #print "MECUJEM RIADOK $_\n";
        }
        
        ## Rozhodnuti_soudu nie je text "Sbirka" (USCR)
        while ($_ =~ s/(<(?:Rozhodnuti_soudu)[^>]*>)([^<>]*Sbírka[^<>]*)(<\/(?:Rozhodnuti_soudu)>)/$2/) {
            #print "MECUJEM RIADOK $_\n";
        }
        
        ## Rozhodnuti_soudu nie je text "str. \d+" (USCR)
        while ($_ =~ s/(<(?:Rozhodnuti_soudu)[^>]*>)([^<>]*str\. \d+[^<>]*)(<\/(?:Rozhodnuti_soudu)>)/$2/) {
            #print "MECUJEM RIADOK $_\n";
        }

        ## Rozhodnutie sudu musi obsahovat cislo...
        while ($_ =~ s/<Rozhodnuti_soudu[^>]*>([^\d<]+)<\/Rozhodnuti_soudu>/$1/) {
            # print daco
        }

        ## (2013-06-06) Ak je za tym este lomitko a rok, pridam to do znacky...
        while ($_ =~ s/(<\/Rozhodnuti_soudu>)(\/\d{4})/$2$1/) {
            # print daco
        }

        ## (2013-05-17) Ak su za sebou 2 rozdhonutia a medzi nimi je len nejaka zkratka, bude z toho
        ## jedna znacka...
        while ($_ =~ s/<\/Rozhodnuti_soudu>(\s*[A-Z][a-z]{1,3}\s*)<Rozhodnuti_soudu>/$1/) {
            # print daco
        }

        ## (2013-05-17) Rozhodntie nie je, ak je za tym Kc
        while ($_ =~ s/<Rozhodnuti_soudu[^>]*>([^<]+)<\/Rozhodnuti_soudu>([,\-\s]*Kč)/$1$2/) {
            # print daco
        }

        ### Rozhodnutie sudu, ktore obsahuje "Sb." zmenim na zakon
        while ($_ =~ s/(<Rozhodnuti_soudu[^>]*>)([^<>]+Sb.[^<>]*)(<\/Rozhodnuti_soudu>)/<Zakon>$2<\/Zakon>/) {
            #print "MECUJEM RIADOK $_\n";
        }

        ## Ak za rozhodnutim hned ide Sb, je to zle...
        while ($_ =~ s/<Rozhodnuti_soudu[^>]*>([^<]+)<\/Rozhodnuti_soudu>(\s*Sb\.)/$1$2/) {
            # nic
        }

        ## Ak Rozhodnute sudu obsahuje ciarku, rozdelime ho na 2
        while ($_ =~ s/<Rozhodnuti_soudu[^>]*>([^<,]+)(\s*,\s*)([^<,]+)<\/Rozhodnuti_soudu>/<Rozhodnuti_soudu>$1<\/Rozhodnuti_soudu>$2<Rozhodnuti_soudu>$3<\/Rozhodnuti_soudu>/) {
            # nic
        }

        ## Dokument, ktory obsahuje len cisla a Sb. menim na zakon
        while ($_ =~ s/(<Dokument[^>]*>)(č. \d+\/\d+ Sb.)(<\/Dokument>)/<Zakon>$2<\/Zakon>/) {
            #print "MECUJEM RIADOK $_\n";
        }

        ## Zakon konci v momente, ked je "Sb."
        while ($_ =~ s/(<Zakon[^>]*>)([^<>]*Sb\.)([^<>]+)(<\/Zakon>)/$1$2$4$3/) {
            #print "MECUJEM RIADOK $_\n";
        }

        ## Rusim anotaciu "Zakon", ak je pred nou "dne", "mezi"
        while ($_ =~ s/(dne|mezi) <(?:Ucinnost|Zakon)[^>]*>([^<>]+)<\/(?:Zakon|Ucinnost)>/$1 $2/) {
            #print "DACO";
        }

        ## Ak su 2 zakony a medzi nimi je len jedno slovo, spojim to do jednej znacky...
        while ($_ =~ s/<\/Zakon>([^\s]*\s+[^\s]*)<Zakon[^>]*>/$1/g) {
            #print "DACO";
        }
        while ($_ =~ s/<\/Zakon>(\s+[^\s]+\s+)<Zakon[^>]*>/$1/g) {
            #print "DACO";
        }
        
        ## Ak ostane prazdna znacka, zmazeme ju...
        $_ =~ s/<\w+[^>]*>\s*<\/\w+>//g;

        ## Zrusim ucinnost, ak zacina na "od a potom datum"
        while ($_ =~ s/<Ucinnost[^>]*>([\s\.\d]*(?:od|do|[^<]*době)[^<]*)<\/Ucinnost>/$1/) {
            # print daco
        }

        while ($_ =~ s/<Ucinnost[^>]*>([\s\.]*ve znění[\s\.]*)<\/Ucinnost>/$1/) {
            # print daco
        }

        ## Posunieme ucinnost ak neobsahuje cely datum
        while ($_ =~ s/<\/Ucinnost>(\.?\s*(?:[01]*\d\.|leden(?:ce)?|ledna|únor(?:a)|březen|března|duben|dubna|květen|května|červen|června|červenec|července|srpen|srpna|září|říjen|října|listopad(?:u)?|prosinec|prosince)\s*\d{4})/$1<\/Ucinnost>/) {
            # print "NIECO";
        }

        ## Serem na HMM, vsetko oznacim sam...
        $_ =~ s/<\/?Zkratka[^>]*>//g;

        ## Zkratka musi byt vsetko v uvodzovkach
        #while ($_ =~ s/(dál.{,20}„\s*)([^\s][^“]*?)(<Zkratka[^>]*>)([^<]*)(<\/Zkratka>\s*“)/$1$3$2$4$5/) {
        #    #print "Presunul som zkratku: $_\n\n\n";
        #    # print "NIECO";
        #}

        ## Skusime vyznacit 100% zkratky
        while ($_ =~ s/(dále též jen\s*(?:„|,,)\s*)([^<“]+[^\s])(\s*“)/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/(dále jen ve zkratce\s*(?:„|,,)\s*)([^<“]+[^\s])(\s*“)/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/(dále opět jen\s*(?:„|,,)\s*)([^<“]+[^\s])(\s*“)/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/(dále jen\s*(?:„|,,|")\s*)([^<“"]+[^\s])(\s*(?:“|"))/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/(dále již\s*(?:„|,,)\s*)([^<“"]+[^\s])(\s*“)/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/(dále (?:také|též)\s*(?:„|,,|")\s*)([^<“"]+[^\s])(\s*(?:“|"))/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/(dále\s*(?:„|,,|")\s*)([^<“"]+[^\s])(\s*(?:“|"))/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka: $2\n";
            # print daco
        }

        while ($_ =~ s/([\(,\-]\s*dále jen\s*)([^<“\)]+[^\s])(\s*\))/$1<Zkratka>$2<\/Zkratka>$3/) {
            #print "Zkratka bez uvodzoviek: $2\n";
        }

        ## Blacklist
        while ($_ =~ s/<Zkratka[^>]*>([^<]*(?:matka|nezletil[áý]|opatrovník|žalobkyni|žalovaný|babička|Středisko|otec|stěžovatelka|dětské|žalobkyně|účastník|období)[^<]*)<\/Zkratka>/$1/) {
            # print DACO
        }

        ## Ked institucia konci na v a za tym je slovo s velkym, tak ho pridame
        while ($_ =~ s/(<Instituce[^>]*>[^>]+ ve?)<\/Instituce>( [A-Z][^\s]+)/$1$2<\/Instituce>/) {
            # print DACO
        }

        ## Ked institucia konci na v a za tym je slovo s malym, tak to v z institucie zoberieme
        while ($_ =~ s/(<Instituce[^>]*>[^>]+) v<\/Instituce>( [a-z][^\s]+)/$1<\/Instituce> v$2/) {
            # print DACO
        }

        ## 2013-07-07 (VK)
        ## Skusim odstranit zatvorky a tak, ak su na konci znacky zakon
        while ($_ =~ s/(\)\.?)(<\/Zakon>)/$2$1/) {
            # print DACO
        }

        ## 2013-07-07 (VK)
        ## Skusim identifikovat inicialky mien
        while ($_ =~ s/<Zakon[^>]*>((?:[^<]*?\s+)?[A-ZÁČĚÉÍĽĹŇÓŘŔŠŤÚŮÝŽ]\.\s*[A-ZÁČĚÉÍĽĹŇÓŘŔŠŤÚŮÝŽ]\.(?:\s+[^<]*)?)<\/Zakon>/$1/) {
            #print "rulebased.00: '$1'\n";
        }

        ## 2013-07-07 (VK)
        ## Pridavam pravidla, ktore by mali rozdelit zakony na kusky, ak zachytavaju viac
        ## nez jeden dokument!
        while ($_ =~ s/<Zakon[^>]*>([^<]*)(,?\s*(?: a podle| a ustanovení| s ustanovením| ve spojení(?: s ustanovením)?| resp\.|\. Ustanovením)\s*)([^>]*?)<\/Zakon>/<Zakon>$1<\/Zakon>$2<Zakon>$3<\/Zakon>/) {
            #print "rulebased.01: '$1'\n";
            #print "rulebased.01: '$2'\n";
            #print "rulebased.01: '$3'\n";
            #print "-----\n";
        }
        while ($_ =~ s/<Zakon[^>]*>([^<]*)(,\s*)([A-ZÁČĚÉÍĽĹŇÓŘŔŠŤÚŮÝŽ][^>]*?)<\/Zakon>/<Zakon>$1<\/Zakon>$2<Zakon>$3<\/Zakon>/) {
            #print "rulebased.02: '$1'\n";
            #print "rulebased.02: '$2'\n";
            #print "rulebased.02: '$3'\n";
            #print "-----\n";
        }
        while ($_ =~ s/<Zakon[^>]*>([^<]*(?:zákon|zákona|zákoníku?|tr\. ř\.|zák\.|Sb.|řádu|práce|o\.\s*s\.\s*ř\.|svobod|Ústavy(?: ČR| [Čč]eské republiky)?))((?:\s+a|\s*,)\s+)([^<]*?)<\/Zakon>/<Zakon>$1<\/Zakon>$2<Zakon>$3<\/Zakon>/) {
            #print "rulebased.03: '$1'\n";
            #print "rulebased.03: '$2'\n";
            #print "rulebased.03: '$3'\n";
            #print "-----\n";
        }
        while ($_ =~ s/<Zakon[^>]*>([^<]*(?:Listiny|Úmluvy))(\s+[,a]+\s+)([^<]*?)<\/Zakon>/<Zakon>$1<\/Zakon>$2<Zakon>$3<\/Zakon>/) {
            #print "rulebased.04: '$1'\n";
            #print "rulebased.04: '$2'\n";
            #print "rulebased.04: '$3'\n";
            #print "-----\n";
        }

        ### Anotacia institucie musi obsahovat slovo soud
        my $original = $_;
        my $result = "";
        while ($original =~ /^(.*?)(<Instituce[^>]*>)([^>]*)(<\/Instituce>)(.*)$/) {
            #print "\n\n\n---------------------\n\n\n";
            #print "VSTUP: $original\n";

            my $before = $1;
            my $start_tag = $2;
            my $instituce = $3;
            my $end_tag = $4;
            my $after = $5;

            if ($instituce !~ /soud/) {
                $result .= $before . $instituce;
                $original = $after;
                #print "\tMAZEM ZNACKU $instituce!!!\n";
                next;
            }

            $result .= $before . $start_tag . $instituce . $end_tag;
            $original = $after;
        }
        $result .= $original;
        $_ = $result;

        ## Chceme vymazat nespravne otagovane ucinnosti. Pravidlo -- pred kazdym musi byt
        ## nablizku koniec tagu zakon...
        my $novy_riadok = "";
        my $povodny_riadok = $_;
        while ($povodny_riadok) {
            #print "POZERAM NA RIADOK $povodny_riadok\n";
            if ($povodny_riadok =~ /^(.*?)<Ucinnost[^>]*>.*$/) {
                $novy_riadok .= $1;
                my $before = $1;

                ## Este z toho stringu odstranim veci v zatvorke...
                $before =~ s/\([^\)]+\)//g;
                my @tokens = split(/\s+/, $before);

                my $zakon = 0;
                while (my $token = pop(@tokens)) {
                    if ($token =~ /<\/([^\s>]+)/) {
                        if ($1 eq "Zakon") {
                            $zakon = 1
                        }
                    }
                }
    
                #print "RADEK: $_\n";
                #print "\tNASLI SME ZAKON?: $zakon\n";
    
                if (!$zakon) {
                    $povodny_riadok =~ s/^(.*?)<Ucinnost[^>]*>([^<]+)<\/Ucinnost>(.*)$/$2$3/;
                }
                else {
                    $povodny_riadok =~ s/^.*?(<Ucinnost[^>]*>[^<]+<\/Ucinnost>)(.*)$/$2/;
                    $novy_riadok .= $1;
                }

                #print "\tOPRAVENE: $povodny_riadok\n";
            }
            else {
                last;
            }
        }
        $novy_riadok .= $povodny_riadok;
        $_ = $novy_riadok;

        ## Normalizacia nazvu sudov
        #$name =~ s/([Kk]rajsk|[Mm]ěstsk)(?:ého|ému|ém|ým)/$1ý/;
        #$name =~ s/([Nn]ejvyšší|[úÚ]stavní|[Oo]kresní|[Oo]bvodní|[vV]rchní)(?:[^\s]*)/$1/;
        #$name =~ s/(soud)(?:[^\s]*)/$1/;
        #$name =~ s/poboč(?:ky|ce|ku|kou)/pobočka/;
        #$name =~ s/\s*,\s*$//;
        $_ =~ s/<Instituce([^>]*)>([^<]*)<\/Instituce>/<Instituce$1 name="$2">$2<\/Instituce>/g;

        $_ =~ s/name="([Kk]rajsk|[Mm]ěstsk)(?:ého|ému|ém|ým)([^"]*)"/name="$1ý$2"/g;
        $_ =~ s/name="([Nn]ejvyšší|[úÚ]stavní|[Oo]kresní|[Oo]bvodní|[vV]rchní)(?:[^\s]*)([^"]*)"/name="$1$2"/g;
        $_ =~ s/name="([^"]*)soud(?:[^\s"]+)([^"]*)"/name="$1soud$2"/g;
        $_ =~ s/name="([^"]*)soud(?:[^\s"]+)([^"]*)"/name="$1soud$2"/g;
        $_ =~ s/name="([^"]*)poboč(?:[^\s"]*)([^"]*)"/name="$1pobočka$2"/g;
        $_ =~ s/name="([^"]*)\s*,\s*"/name="$1"/g;

        $output .= "$_\n";
    }
    close(VXML_FILE);

    return $output;
}

sub linker {
    my ($self, $filename) = @_;
    my $debug = 0;
    my $output = "";

    my %links = ();
    my %inst2id = ();

    open(VXML_FILE, "<$filename");
    binmode(VXML_FILE, ":encoding(utf8)");

    my $previous_line = "";
    while (<VXML_FILE>) {
        chomp($_);

        print "\n\n---------------------------\n" if ($debug);
        print "RIADOK: $_\n" if ($debug);
        print "---------------------------\n\n" if ($debug);

        my $line_before = "";

        while ($_ =~ s/(.*?)<Rozhodnuti_soudu[^>]*>([^<]+)<\/Rozhodnuti_soudu>(.*)/$3/) {
            my $before = $1;
            my $data = $2;

            print "HLADAM INSTITUCIU PRE $data\n" if ($debug);
            print "\tPRED = $before\n" if ($debug);

            #if (!$before) {
            #    #print "\tNie je nic pred, hladam v minulych riadkoch\n";
            #    $before = $previous_line;
            #}

            if (defined($links{$data})) {
                print "\tUZ EXISTUJE\n" if ($debug);
                next;
            }

            while ($before =~ /Instituce/ and
                   (($before =~ /.*<Instituce([^>]*)>([^<]+)<\/Instituce>/ and $2 !~ /\s+/) or
                    ($before =~ /.*<Instituce([^>]*)>([^<]+)<\/Instituce>/ and $self->_normalizaceInstituci($2) =~ /(prvního|odvolací|dovolací)/))) {
                $before =~ s/(.*)<Instituce([^>]*)>([^<]+)<\/Instituce>/$1/;
            }

            my $nasiel_som = 0;
            if ($before =~ /.*<Instituce([^>]*)>([^<]+)<\/Instituce>/) {
                $links{$data} = $self->_normalizaceInstituci($2);
                print "\tNASIEL SOM "  . $self->_normalizaceInstituci($2) . "\n" if ($debug);
                $nasiel_som = 1;
                if ($1 =~ /id="(\d+)"/ and !defined($inst2id{$links{$data}})) {
                    $inst2id{$links{$data}} = $1;
                    print "\tNASIEL SOM ID PRE '$links{$data}': $inst2id{$links{$data}}\n" if ($debug);
                }
            }

            if (!$nasiel_som and
                $line_before =~ /.*<Instituce([^>]*)>([^<]+)<\/Instituce>/) {
                $links{$data} = $self->_normalizaceInstituci($2);
                print "\tNASIEL SOM "  . $self->_normalizaceInstituci($2) . "\n" if ($debug);
                $nasiel_som = 1;
                if ($1 =~ /id="(\d+)"/ and !defined($inst2id{$links{$data}})) {
                    $inst2id{$links{$data}} = $1;
                    print "\tNASIEL SOM ID PRE '$links{$data}': $inst2id{$links{$data}}\n" if ($debug);
                }
            }

            if (!$nasiel_som and
                $data =~ /ÚS/) {
                $links{$data} = "Ústavní soud";
                print "\tNASIEL SOM PODLA KEYWORD: "  . $self->_normalizaceInstituci("Ústaní soud") . "\n" if ($debug);
            }
            
            
            $line_before .= "$before ";
        }

        $previous_line = $_;
    }
    close(VXML_FILE);

    ## Ak sme niektore rozhodnutie priradili nejakemu obecnemu nazvu (okresny sud), tak pozrieme, ci
    ## v instituciach nemame nejaky okresny sud s velkym o :-) Ak je jeden, tak tomu to dame
    foreach my $rs (keys %links) {
        if ($links{$rs} =~ /^okresní soud$/) {
            foreach my $inst (keys %{{ map { $_ => 1 } keys %inst2id}}) {
                if ($inst =~ /^Okresní soud [^\s]/) {
                    $links{$rs} = $inst;
                }
            }
        }
        
    }

    ## Teraz to prejdem este raz a do refers_to zapisem pozadovane data
    open(VXML_FILE, "<$filename");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        ## Musim pred vlastnou pracou zmazat linky, ktore tam uz su...
        $_ =~ s/refers_to="\d+"/refers_to="0"/g;

        while ($_ =~ /<Rozhodnuti_soudu([^>]*)refers_to="0"([^>]*)>([^<]+)<\/Rozhodnuti_soudu>/) {
            my $before = $1;
            my $after = $2;
            my $data = $3;

            if (defined($links{$data}) and defined($inst2id{$links{$data}})) {
                $_ =~ s/(<Rozhodnuti_soudu[^>]*)refers_to="0"([^>]*)>([^<]+)<\/Rozhodnuti_soudu>/$1refers_to="$inst2id{$links{$data}}"$2>$3<\/Rozhodnuti_soudu>/;
                #print "Rozpoznany link: $3 vydal $links{$data}\n";
            }
            else {
                $_ =~ s/(<Rozhodnuti_soudu[^>]*)refers_to="0"([^>]*)>([^<]+)<\/Rozhodnuti_soudu>/$1$2>$3<\/Rozhodnuti_soudu>/;
                #print "NERozpoznany link: $3 \n";
            }
        }

        $output .= "$_\n";
    }
    close(VXML_FILE);

    return $output;
}

## 2013-07-10 (VK)
## Linker pre zkratky
sub linkerZ {
    my ($self, $filename) = @_;

    ## Ulozim si vsetky anotacie z dokumentu
    my %anotace = ();
    open(VXML_FILE, "<$filename");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        ## Taketo skarede nacitanie sa deje kvoli gold-standard datam, ktore nie su zvalidovane a ja som lenivy
        ## to spravit :-P
        while ($_ =~ s/(.*?)<(Rozhodnuti_soudu|Zakon|Instituce|Ucinnost|Zkratka|Plne_zneni|Dokument) ([^>]+)>([^<]+)<\/(?:Rozhodnuti_soudu|Zakon|Instituce|Ucinnost|Zkratka|Plne_zneni|Dokument)>(.*)/$1$4$5/) {
            my $anotace = {
                tag => $2,
                text => $4
            };
            my $attributes = $3;
            if ($attributes =~ /id="(\d+)"/) {
                $anotace->{id} = $1;
            }
            if ($attributes =~ /start="(\d+)"/) {
                $anotace->{start} = $1;
            }
            if ($attributes =~ /end="(\d+)"/) {
                $anotace->{end} = $1;
            }
            if ($attributes =~ /refers_to="(\d+)"/) {
                $anotace->{refers_to} = $1;
            }
            if ($attributes =~ /label="([A-Z]\d+)"/) {
                $anotace->{label} = $1;
            }
            if ($attributes =~ /name="[^"]*"/) {
                $anotace->{name} = $1;
            }

            $anotace{$anotace->{id}} = $anotace;
        }
    }
    close(VXML_FILE);

    ## Debug - vypisem extrahovane anotace
    #my $ord = 0;
    #print "***\nEXTRAHOVANE ANOTACE\n***\n\n";
    #foreach my $id (sort {$a <=> $b} keys %anotace) {
    #    $ord++;
    #    my $anotace = $anotace{$id};
    #
    #    my $color_start = "";
    #    my $color_end = "";
    #    if ($anotace->{tag} eq "Zkratka") {
    #        $color_start = "\e[01;36m";
    #        $color_end = "\e[00m";
    #    }
    #
    #    print "$color_start";
    #    print "$ord\t";
    #    print "$anotace->{id}\t";
    #    print "$anotace->{label}\t";
    #    print "$anotace->{start}\t";
    #    print "$anotace->{end}\t";
    #    print "$anotace->{tag}\t";
    #    print "\t" if (length($anotace->{tag}) < 8);
    #    print "\t" if (length($anotace->{tag}) < 16);
    #    print "$anotace->{text}\n";
    #    print "$color_end";
    #}

    my %zkratky = ();
    my %plne_zneni = ();

    ## Prejdi vsetky zkratky a vrat predchadzajucu znacku
    foreach my $id (sort {$a <=> $b} keys %anotace) {
        my $zkratka = $anotace{$id};
        if ($zkratka->{tag} ne "Zkratka") {
            next;
        }

        ## Toto ma zmysel len pri evaluacii GS, kedy sa nezaoberame
        ## znackami, ktore nemaju link na PlneZneni.
        #if ($zkratka->{refers_to} == 0) {
        #    next;
        #}

        print STDERR "$filename\t";
        print STDERR sprintf("%-20.20s\t", $zkratka->{text});
        print STDERR "$zkratka->{id}\t";

        ## Hladame najblizsi predchadzajuci tag, ktory nie je Ucinnost
        ## Zaroven musim este osetrit situaciu, ze aj samotna zkratka je anotovana
        ## ako zakon alebo institucia. Osetrim to pomocou startovacich a koncovych pozicii.
        ## Plne znenie musi skoncit pred zacanim Zkratky.
        my $id_plne_zneni = $id - 1;
        while ($id_plne_zneni > 0 and
               ($anotace{$id_plne_zneni}->{tag} eq "Ucinnost" or
                $anotace{$id_plne_zneni}->{tag} eq "Plne_zneni" or
                $anotace{$id_plne_zneni}->{tag} eq "Dokument" or
                $anotace{$id_plne_zneni}->{end} > $zkratka->{start})) {
            $id_plne_zneni--;
        }

        my $plne_zneni = $anotace{$id_plne_zneni};

        ## Ak je v texte zakon, tak aj plne_zneni musi byt zakon, pouzijeme teda najblizsi
        if ($zkratka->{text} =~ /zákon/ or
            $zkratka->{text} =~ /řád/) {
            while ($id_plne_zneni > 0 and
                    $plne_zneni->{tag} ne "Zakon") {
                $id_plne_zneni--;
                $plne_zneni = $anotace{$id_plne_zneni};
            }   
        }
        elsif ($zkratka->{text} =~ /(rozhodnutí|stanovisko)/) {
            while ($id_plne_zneni > 0 and
                   $plne_zneni->{tag} ne "Rozhodnuti_soudu") {
                $id_plne_zneni--;
                $plne_zneni = $anotace{$id_plne_zneni};
            }
        }
        ## Ak je v zkratke slovo sud, je jasne, ze musime hladat najblizsiu instituciu
        elsif ($zkratka->{text} =~ /soud(?:u|em)?/) {
            while ($id_plne_zneni > 0 and
                   $plne_zneni->{tag} ne "Instituce") {
                $id_plne_zneni--;
                $plne_zneni = $anotace{$id_plne_zneni};
            }
        }

        ## Skusim vyriecit castu konstrukciu, ktora nesposobuje chybu, ale
        ## GS vyzera proste inak... Ak objavim slovny popis a hned pred nim je
        ## cisleny popis zakona, tak zoberiem radsej ten
        my $id_plne_zneni_cadet = $id_plne_zneni - 1;
        while ($id_plne_zneni_cadet > 0 and
               $anotace{$id_plne_zneni_cadet}->{tag} ne "Zakon") {
            $id_plne_zneni_cadet--;
        }
        
        if ($plne_zneni->{id} > 1 and
            $plne_zneni->{tag} eq "Zakon" and
            $plne_zneni->{text} !~ /\d/ and
            $anotace{$id_plne_zneni_cadet}->{tag} eq "Zakon" and
            ($plne_zneni->{start} - $anotace{$id_plne_zneni_cadet}->{end}) < 5) {
            $plne_zneni = $anotace{$id_plne_zneni_cadet};
        }

        print STDERR printf("%-80.80s\t", $plne_zneni->{text});

        ## Ak ide o zakon, skusime idenfifikovat len tu cast, ktora odkazuje na
        ## cely dokument, nie jeho sekcie
        my $plne_zneni_text = $plne_zneni->{text};

        ## Odstranim referencie na casti dokumentov
        $plne_zneni_text =~ s/,\s*//g if ($plne_zneni->{tag} eq "Zakon");
        $plne_zneni_text =~ s/^\d+ //;
        while ($plne_zneni_text =~ s/\d+ a (čl\.|článku|násl\.|odst\.)\s*//g) {};
        while ($plne_zneni_text =~ s/\d+ a \d+//g) {};
        $plne_zneni_text =~ s/(?:(?:v )?čl(?:ánku|\.)|§|o?dst\.)\s*(?:[l\d]+[a-z]?)?//g;
        $plne_zneni_text =~ s/(?:písm.)\s*[a-z]?\s*[\)\/]?//;
        $plne_zneni_text =~ s/vět[ay] \w+//;

        ## Odkaz do Zbierky je hned jasny
        $plne_zneni_text =~ s/.*?(č\.\s*\d+\/\d+\s*(?:Sb\.?|Z\.\s*z\.)).*/$1/;

        ## Medzery prec!
        $plne_zneni_text =~ s/^\s*//g;
        $plne_zneni_text =~ s/\s*$//g;

        ## Este to skusme lemmatizovat :P
        my $lemmatizovany = $plne_zneni_text;

        ## Len pre ZAKON
        if ($plne_zneni->{tag} eq "Zakon") {
            ## Umluva, Listina
            $lemmatizovany =~ s/([úÚ]mluv|[Ll]istin|[Úú]stav)[^\s]+/$1a/g if ($lemmatizovany !~ /k ([úÚ]mluv|[Ll]istin|[Úú]stav)[^\s]+/);
    
            ## OSR, Obchodni zakonnik, Obcansky zakonnik, Trestni rad
            $lemmatizovany =~ s/([Oo]bčansk|Dodatkov)(?:ého|ému?|ým)/$1ý/;
            $lemmatizovany =~ s/([Oo]bchodní|[Ss]oudní|[Tt]restní)(?:ho|mu?|ím)/$1/;
            $lemmatizovany =~ s/([Řř]ád|[Zz]ákoník|[Pp]rotokol)(?:u|em)/$1/;   
        }

        ## Len pre INSTITUCE
        if ($plne_zneni->{tag} eq "Instituce") {
            $lemmatizovany =~ s/([Kk]rajsk|[Mm]ěstsk|Evropsk)(?:ého|ému|ém|ým)([^"]*)/$1ý$2/g;
            $lemmatizovany =~ s/([Nn]ejvyšší|[úÚ]stavní|[Oo]kresní|[Oo]bvodní|[vV]rchní)(?:[^\s]*)([^"]*)/$1$2/g;
            $lemmatizovany =~ s/([^"]*)soud(?:[^\s"]+)([^"]*)/$1soud$2/g;
            $lemmatizovany =~ s/([^"]*)soud(?:[^\s"]+)([^"]*)/$1soud$2/g;
            $lemmatizovany =~ s/([^"]*)poboč(?:[^\s"]*)([^"]*)/$1pobočka$2/g;
            $lemmatizovany =~ s/([^"]*)\s*,\s*/$1/g;
        }

        ## Debug vysledku
        #printf("%-60.60s\t", $plne_zneni_text);
        #printf("%-60.60s\t", $anotace{$zkratka->{refers_to}}->{text});
        #if ($anotace{$zkratka->{refers_to}}->{text} =~ /$plne_zneni_text/) {
        #    print "\e[01;32mOK";
        #}
        #else {
        #    print "\e[01;31mERROR";
        #}
        #print "\e[00m\t";
        #printf("%-80.80s\t", $lemmatizovany);
        #print "\n";

        ## Zapis vysledku do hasha, id bude podla znacky Zkratka
        $plne_zneni{$plne_zneni->{id}} = {
            plne_zneni_tag => $plne_zneni,
            plne_zneni_text => $plne_zneni_text,
            lemmatizovany => $lemmatizovany,
            pre_zkatku => $zkratka->{id}
        };

        print STDERR "\n";
    }

    ## Teraz najdem maximalne ID a od neho budem vytvarat dalsie tagy
    my @ids = reverse sort {$a <=> $b} keys %anotace;
    my $id = $ids[0] + 1;

    ## Sem zapisem vysledok
    my $output = "";

    ## Teraz prejdem dokument a ak narazim na znacku v ktorej ma byt Plne_zneni, tak
    ## v nej vyznacim to Plne_zneni. Ak narazim na znacku Zkratka, tak v nej definujem refers_to
    open(VXML_FILE, "<$filename");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        my $original_line = $_;

        ## Taketo skarede nacitanie sa deje kvoli gold-standard datam, ktore nie su zvalidovane a ja som lenivy
        ## to spravit :-P
        while ($_ =~ s/(.*?)<(Rozhodnuti_soudu|Zakon|Instituce|Ucinnost|Zkratka|Plne_zneni|Dokument) ([^>]+)>([^<]+)<\/(?:Rozhodnuti_soudu|Zakon|Instituce|Ucinnost|Zkratka|Plne_zneni|Dokument)>(.*)/$1$4$5/) {
            my $anotace = {
                tag => $2,
                text => $4
            };
            my $attributes = $3;
            if ($attributes =~ /id="(\d+)"/) {
                $anotace->{id} = $1;
            }
            if ($attributes =~ /start="(\d+)"/) {
                $anotace->{start} = $1;
            }
            if ($attributes =~ /end="(\d+)"/) {
                $anotace->{end} = $1;
            }
            if ($attributes =~ /refers_to="(\d+)"/) {
                $anotace->{refers_to} = $1;
            }
            if ($attributes =~ /label="([A-Z]\d+)"/) {
                $anotace->{label} = $1;
            }
            if ($attributes =~ /name="[^"]*"/) {
                $anotace->{name} = $1;
            }

            if (defined($plne_zneni{$anotace->{id}})) {
                print STDERR "Riesim plne zneni v $plne_zneni{$anotace->{id}}->{plne_zneni_tag}->{id}\n";
                $original_line =~ s/(<[^>]*id="$plne_zneni{$anotace->{id}}->{plne_zneni_tag}->{id}"[^>]*>[^<]*)($plne_zneni{$anotace->{id}}->{plne_zneni_text})([^<]*<\/[^>]+>)/$1<Plne_zneni name="$plne_zneni{$anotace->{id}}->{lemmatizovany}" id="$id">$2<\/Plne_zneni>$3/;
                $zkratky{$plne_zneni{$anotace->{id}}->{pre_zkatku}} = $id;
                $id++;
                next;
            }

            if (defined($zkratky{$anotace->{id}})) {
                print STDERR "Riesim zkratku $anotace->{id}\n";
                if (defined($zkratky{$anotace->{id}})) {
                    print STDERR "Nasiel som zkratku, jej refers_to je $zkratky{$anotace->{id}}\n";
                    $original_line =~ s/<Zkratka([^>]*id="$anotace->{id}"[^>]*)refers_to="0"([^>]*)>/<Zkratka$1refers_to="$zkratky{$anotace->{id}}"$2>/;
                }
                else {
                    $original_line =~ s/<Zkratka([^>]*id="$anotace->{id}"[^>]*)refers_to="0"([^>]*)>/<Zkratka$1refers_to=""$2>/;
                }
            }
        }

        $output .= "$original_line\n";
    }
    close(VXML_FILE);

    return $output;
}

## Mrkneme ci uz pre dane rozhodnutie mame instituciu
sub _hasInstitution {
    my ($self, $ra_links, $rozhodnutie) = @_;
    print "_hasInstitution($rozhodnutie) ... ";

    foreach my $link (@$ra_links) {
        if ($link->{rozhodnuti}{text} eq $rozhodnutie) {
            print "nasiel som\n";
            return $link->{instituce};
        }
    }

    print "nenasiel som\n";
    return undef;
}

sub _normalizaceInstituci {
    my ($self, $name) = @_;

    $name =~ s/([Kk]rajsk|[Mm]ěstsk)(?:ého|ému|ém|ým)/$1ý/;
    $name =~ s/([Nn]ejvyšší|[úÚ]stavní|[Oo]kresní|[Oo]bvodní|[vV]rchní)(?:[^\s]*)/$1/;
    $name =~ s/(soud)(?:[^\s]*)/$1/;
    $name =~ s/poboč(ky|ce|ku|kou)/pobočka/;
    $name =~ s/\s*,\s*$//;

    return $name;
}

sub institutionTagger {
    my ($self, $filename) = @_;

    my $output = "";

    open(VXML_FILE, "<$filename");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        ## Natvrdo zoznam stringov, ktore su zakon!
        while ($_ =~ s/([^>]+)([Ss]oud(?:u|em)? (?:prvního stupně|(?:od|do)volací(?:ho|mu?)?|(?:městsk|krajsk)(?:ým?|ého|ému?)|(?:obvodní|okresní)(?:ho|mu?)?))([^<]+)/$1<Instituce>$2<\/Instituce>$3/) {
            # nic
        }

        while ($_ =~ s/([^>]+)((?:(?:[Oo]d|[Dd]o)volací(?:ho|mu?)?|(?:[Mm]ěstsk|[Kk]rajsk)(?:ý|ého|ému?|ým)|(?:Nejvyšší|Ústavní|[Oo]bvodní|[Oo]kresní)(?:ho|mu?)?) soud(?:u|em)?)([^y]+|$)/$1<Instituce>$2<\/Instituce>$3/) {
            # nic
        }

        while ($_ =~ s/^(Ústavní(?:ho|mu?)? soud(?:u|em)?)([^\w])/<Instituce>$1<\/Instituce>$2/) {
            # nic
        }

        $output .= "$_\n";
    }
    close(VXML_FILE);

    return $output;
}

sub _readFileForEvaluation {
    my ($self, $filename) = @_;

    ## Vytvorim si pole s tokenom a jeho znackou pre GS a pre T
    my @data = ();

    my $tag = "None";
    my $document = 0;
    open(XML_FILE, "<$filename");
    binmode(XML_FILE, ":encoding(utf8)");
    while (<XML_FILE>) {
        chomp($_);
        $_ =~ s/\s*id="[^"]*"//g;
        $_ =~ s/\s*label="[^"]*"//g;
        $_ =~ s/\s*refers_to="[^"]*"//g;
        $_ =~ s/\s*start="[^"]*"//g;
        $_ =~ s/\s*end="[^"]*"//g;
        $_ =~ s/\s*name="[^"]*"//g;

        ## 2013-01-11 (VK)
        ## Potrebujeme nejak ignorovat chyby v rucnych datach...
        ## (minimalne docasne, kym sa neopravia rucne)
        $_ =~ s/(\d+)(<[^>]+>)(\d+)/$1$3$2/g;
        $_ =~ s/(Praz)(<[^>]+>)(e)/$1$3$2/g;
        $_ =~ s/,,/, ,/g;
        $_ =~ s/(,)([^\s])/$1 $2/g;
        $_ =~ s/&amp;/&/g;
        $_ =~ s/&apos;/'/g;
        #if ($_ =~ /(<[^>]+>)(,)([^\s])/) {
            #print "RIADOK = $_\n\n";
        #}

        ## 2012-12-17 (VK)
        ## Osetrenie HTML Entit
        $_ =~ s/&quot;/"/g;

        if ($_ =~ s/<Document>//) {
            $document = 1;
        }

        if ($_ =~ /<\/Document>/) {
            last;
        }

        if (!$document) {
            next;
        }

        my %open_tags = ();
        my @segments = split(/</, $_);
        foreach my $segment (@segments) {
            if ($segment =~ s/^\/(\w+)>(.*)/$2/) {
                delete($open_tags{$1});
            }
            elsif ($segment =~ s/^([^\/]\w+)>(.*)/$2/) {
                $open_tags{$1} = 1;
            }

            foreach my $token ($self->tokenization($segment)) {
                if (!$token) {
                    next;
                }
                my %tags = ();
                foreach my $tag (keys %open_tags) {
                    $tags{$tag} = 1;
                }
                if (!scalar(keys %open_tags)) {
                    $tags{None} = 1;
                }

                push(@data, {token => $token, tags => \%tags});
            }
        }
    }
    close(XML_FILE);

    return @data;
}

sub initEvaluationStats {
    my ($self) = @_;

    $self->{confusion_matrix} = {};
    $self->{confusion_matrix_2} = {};
    return 1;
}

sub initLinkerEvaluationStats {
    my ($self) = @_;

    $self->{sum} = 0;
    $self->{correct} = 0;
    return 1;
}

sub printEvaluationStats {
    my ($self) = @_;

    ## Vypisem
    #print "-----------------------------\n";
    #print "INDIVIDUAL CONFUSION MATRICES\n";
    #print "-----------------------------\n\n";
    #print "Tag\tA\tB\tC\tD\n";
    #foreach my $tag (sort keys %{$self->{confusion_matrix}}) {
    #    my %confusion_matrix = %{$self->{confusion_matrix}{$tag}};
    #    print "$tag\t$confusion_matrix{tagger_yes_true_yes}\t$confusion_matrix{tagger_no_true_yes}\t$confusion_matrix{tagger_yes_true_no}\t$confusion_matrix{tagger_no_true_no}\n";
    #}
    #
    #print "-----------------------------\n";
    #print "MEASURES\n";
    #print "-----------------------------\n\n";
    #print "Tag\tAcc\tPrec\tRecall\tF-meas\n";
    foreach my $tag (sort keys %{$self->{confusion_matrix}}) {
        my %confusion_matrix = %{$self->{confusion_matrix}{$tag}};
        print "$tag\t";
        printf("%.2f\t", ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_no_true_no}) / ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_no_true_yes} + $confusion_matrix{tagger_yes_true_no} + $confusion_matrix{tagger_no_true_no}));
        printf("%.2f\t", ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_yes_true_no}) > 0 ? ($confusion_matrix{tagger_yes_true_yes}) / ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_yes_true_no}) : 0);
        printf("%.2f\t", ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_no_true_yes}) > 0 ? ($confusion_matrix{tagger_yes_true_yes}) / ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_no_true_yes}) : 0);
        printf("%.2f\t", (($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_yes_true_no}) > 0 and ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_no_true_yes}) > 0) ? 1 / (0.5 * (1 / (($confusion_matrix{tagger_yes_true_yes}) / ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_yes_true_no}))) + 0.5 * (1 / (($confusion_matrix{tagger_yes_true_yes}) / ($confusion_matrix{tagger_yes_true_yes} + $confusion_matrix{tagger_no_true_yes})))) : 0);
        print "\n";
    }

    print "-----------------------------\n";
    print "AGREGATED CONFUSION MATRIX\n";
    print "-----------------------------\n\n";
    print "COLS: GS\n";
    print "ROWS: TEST\n\n";
    print "\t" . join("\t", sort keys %{$self->{confusion_matrix_2}}) . "\n";
    foreach my $tag_a (sort keys %{$self->{confusion_matrix_2}}) {
        print "$tag_a\t";
        foreach my $tag_b (sort keys %{$self->{confusion_matrix_2}}) {
            print "$self->{confusion_matrix_2}{$tag_a}{$tag_b}\t" if (defined($self->{confusion_matrix_2}{$tag_a}{$tag_b}));
            print "0\t" if (!defined($self->{confusion_matrix_2}{$tag_a}{$tag_b}));
        }
        print "\n";
    }
}

sub linkerEvaluation {
    my ($self, $goldstandard_vxml, $tested_vxml) = @_;
    print "linkerEvaluation(@_)\n";
    
    ##
    ## GS
    ##
    my %gs_rs2id = ();
    my %gs_id2inst = ();
    open(VXML_FILE, "<$goldstandard_vxml");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        while ($_ =~ s/<Instituce[^>]*id="(\d+)"[^>]*>([^<]+)<\/Instituce>//) {
            if (!defined($gs_id2inst{$1})) {
                $gs_id2inst{$1} = $self->_normalizaceInstituci($2);
            }
        }

        while ($_ =~ s/<Rozhodnuti_soudu[^>]*refers_to="(\d+)"[^>]*>([^<]+)<\/Rozhodnuti_soudu>//) {
            if (!defined($gs_rs2id{$2})) {
                $gs_rs2id{$2} = $1;
            }
        }
    }
    close(VXML_FILE);

    ##
    ## TEST
    ##
    my %te_rs2id = ();
    my %te_id2inst = ();
    open(VXML_FILE, "<$tested_vxml");
    binmode(VXML_FILE, ":encoding(utf8)");
    while (<VXML_FILE>) {
        chomp($_);

        while ($_ =~ s/<Instituce[^>]*id="(\d+)"[^>]*>([^<]+)<\/Instituce>//) {
            if (!defined($te_id2inst{$1})) {
                $te_id2inst{$1} = $self->_normalizaceInstituci($2);
            }
        }

        while ($_ =~ s/<Rozhodnuti_soudu[^>]*refers_to="(\d+)"[^>]*>([^<]+)<\/Rozhodnuti_soudu>//) {
            if (!defined($te_rs2id{$2})) {
                $te_rs2id{$2} = $1;
            }
        }
    }
    close(VXML_FILE);
    
    print "\nZdroj\tRozhodnutie sudu\t\tGS\t\t\t\t\tTest\n----------------------------------------------------------------------------------------------------------------------\n";
    my $sum = 0;
    my $correct = 0;
    foreach my $rs (keys %gs_rs2id) {
        print "GS\t";
        print "$rs\t";

        print "\t" if (length($rs) < 8);
        print "\t" if (length($rs) < 16);
        print "\t" if (length($rs) < 24);

        print "$gs_id2inst{$gs_rs2id{$rs}}\t";

        print "\t" if (length($gs_id2inst{$gs_rs2id{$rs}}) < 16);
        print "\t" if (length($gs_id2inst{$gs_rs2id{$rs}}) < 24);
        print "\t" if (length($gs_id2inst{$gs_rs2id{$rs}}) < 32);

        $sum++;
        $self->{sum}++;

        if (defined($te_rs2id{$rs})) {
            print "$te_id2inst{$te_rs2id{$rs}}\t";

            if ($te_id2inst{$te_rs2id{$rs}} eq $gs_id2inst{$gs_rs2id{$rs}}) {
                $correct++;
                $self->{correct}++;
            }
        }
        else {
            print "?\t"
        }
        print "\n";
    }

    print "RESULT = $correct / $sum\n\n\n";

    foreach my $rs (keys %te_rs2id) {
        if (defined($gs_rs2id{$rs})) {
            next;
        }
    
        print "TE\t";
        print "$rs\t";
    
        print ".\t" if (length($rs) < 8);
        print ".\t" if (length($rs) < 16);
        print ".\t" if (length($rs) < 24);
    
        if (defined($gs_rs2id{$rs})) {
            print "$gs_id2inst{$gs_rs2id{$rs}}\t";
        }
        else {
            print "x\t";
        }
    
        print ".\t" if (length($te_id2inst{$te_rs2id{$rs}}) < 8);
        print ".\t" if (length($te_id2inst{$te_rs2id{$rs}}) < 16);
        print ".\t" if (length($te_id2inst{$te_rs2id{$rs}}) < 24);
        print "$te_id2inst{$te_rs2id{$rs}}\t";
    
        print "\n";
    }
}

sub evaluation {
    my ($self, $tagset, $goldstandard_vxml, $tested_vxml) = @_;

    ## Nacitam data do pola
    my @gs = $self->_readFileForEvaluation($goldstandard_vxml);
    my @te = $self->_readFileForEvaluation($tested_vxml);

    ## Zistim ake tagy su pouzite v subore a tie spravujem ;-)
    foreach my $annotation (@gs, @te) {
        foreach my $tag (keys %{$annotation->{tags}}) {
            if ($tag !~ /$tagset/) {
                delete($annotation->{tags}{$tag});
                if (!scalar(keys %{$annotation->{tags}})) {
                    $annotation->{tags}{None} = 1;
                }
                
            }
        }
    }

    ## Prechadzam jednotlive tagy a vyplnam maticu konfuzie
    #foreach my $tag ("Zakon", "Rozhodnuti_soudu", "Vyhlaska", "Ucinnost") {
    #foreach my $tag ("Rozhodnuti_soudu") {
    foreach my $tag (sort split(/\|/, $tagset)) {
        if (!defined($self->{confusion_matrix}{$tag})) {
            $self->{confusion_matrix}{$tag} = {
                tagger_yes_true_yes => 0,
                tagger_yes_true_no => 0,
                tagger_no_true_yes => 0,
                tagger_no_true_no => 0
            };
        }

        #print "# tokenov = " . scalar(@gs) . "\n";

        for (my $i = 0; $i < scalar(@gs); $i++) {
            ## Kontrola na zhodnost tokenov
            if ($gs[$i]->{token} ne $te[$i]->{token}) {
                print "ERROR: Tokens differ in test and goldstandard files:\n";
                print "\tToken id $i in GS: \t" . $gs[$i]->{token} . "\n";
                print "\tToken in $i in TEST: \t" . $te[$i]->{token} . "\n";
                next;
            }

            #print "$gs[$i]->{token}\tGS:" . join(",", keys %{$gs[$i]->{tags}}) . "\tTE:" . join(",", keys %{$te[$i]->{tags}}) . "\n";

            if (defined($gs[$i]->{tags}{$tag}) and defined($te[$i]->{tags}{$tag})) {
                $self->{confusion_matrix}{$tag}{tagger_yes_true_yes}++;
                next;
            }

            if (defined($gs[$i]->{tags}{$tag}) and !defined($te[$i]->{tags}{$tag})) {
                $self->{confusion_matrix}{$tag}{tagger_no_true_yes}++;
                next;
            }

            if (!defined($gs[$i]->{tags}{$tag}) and defined($te[$i]->{tags}{$tag})) {
                $self->{confusion_matrix}{$tag}{tagger_yes_true_no}++;
                next;
            }

            if (!defined($gs[$i]->{tags}{$tag}) and !defined($te[$i]->{tags}{$tag})) {
                $self->{confusion_matrix}{$tag}{tagger_no_true_no}++;
                next;
            }

            ### Teraz pripocitame token jednej bunky matice
            #if ($gs[$i]->{tag} eq $tag and
            #    $gs[$i]->{tag} eq $te[$i]->{tag}) {
            #    $self->{confusion_matrix}{$tag}{tagger_yes_true_yes}++;
            #    next;
            #}
            #
            #if ($gs[$i]->{tag} eq $tag and
            #    $gs[$i]->{tag} ne $te[$i]->{tag}) {
            #    $self->{confusion_matrix}{$tag}{tagger_no_true_yes}++;
            #    print "tagger_no_true_yes: $i: $gs[$i]->{tag} vs. $te[$i]->{tag}\n";
            #    next;
            #}
            #
            #if ($gs[$i]->{tag} ne $tag and
            #    $tag ne $te[$i]->{tag}) {
            #    $self->{confusion_matrix}{$tag}{tagger_no_true_no}++;
            #    next;
            #}
            #
            #if ($gs[$i]->{tag} ne $tag and
            #    $tag eq $te[$i]->{tag}) {
            #    $self->{confusion_matrix}{$tag}{tagger_yes_true_no}++;
            #    print "tagger_yes_true_no: $i: $gs[$i]->{token}: $gs[$i]->{tag} vs. $te[$i]->{tag}\n";
            #    next;
            #}

            print "TOKEN $i\t$gs[$i]->{tag}\t$te[$i]->{tag}\t\t$gs[$i]->{token} - $te[$i]->{token}\n";
        }
    }

    for (my $i = 0; $i < scalar(@gs); $i++) {
        foreach my $tag_gs (keys %{$gs[$i]->{tags}}) {
            foreach my $tag_te (keys %{$te[$i]->{tags}}) {
                $self->{confusion_matrix_2}{$tag_gs}{$tag_te}++;
            }
        }
    }
    
    return 1;
}

1;
