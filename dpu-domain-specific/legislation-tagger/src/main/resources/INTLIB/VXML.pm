#!/usr/bin/perl

## Author: Vincent Kriz, 2012
## E-mail: kriz@ufal.mff.cuni.cz

## This library contains methods for handling
## VXML files

my $debug = 0;

use strict;
use warnings;
use utf8;

package INTLIB::VXML;

sub new {
    my $self = {};

    bless $self;
    return $self;
}


sub loadAnnotationFromVXML {
    my ($self, $prefix, $file) = @_;

    my %id2label = ();
    my %refersto = ();

    open(FILE, "<$file");
    binmode(FILE, ":encoding(utf8)");
    while (<FILE>) {
        chomp($_);

        $_ =~ s/(label=").(\d+")/$1$prefix$2/g;

        #<Instituce name="Zemského soud" start="431" end="445" id="2" label="T2" refers_to="0">
        while ($_ =~ s/<(\w+) name="([^"]*)" start="(\d+)" end="(\d+)" id="(\d+)" label="([^"]+)" refers_to="(\d+)">//) {
            push(@{$self->{annotation}}, {id => $5, tag => $1, start => $3, end => $4, label => $6, refers_to => $7, name => $2});
            $id2label{$5} = $6;

            #print STDERR "ANNOTATION: id => $5, tag => $1, start => $3, end => $4, label => $6, refers_to => $7\n";
            
            if ($7 > 0) {
                $refersto{$5} = $7;
            }
        }

        while ($_ =~ s/<(\w+) start="(\d+)" end="(\d+)" id="(\d+)" label="([^"]+)" refers_to="(\d+)">//) {
            push(@{$self->{annotation}}, {id => $4, tag => $1, start => $2, end => $3, label => $5, refers_to => $6});
            $id2label{$4} = $5;

            #print STDERR "ANNOTATION: id => $4, tag => $1, start => $2, end => $3, label => $5, refers_to => $6\n";
            
            if ($6 > 0) {
                $refersto{$4} = $6;
            }
        }

         while ($_ =~ s/<(\w+) name="([^"]*)" start="(\d+)" end="(\d+)" id="(\d+)" label="([^"]+)">//) {
            push(@{$self->{annotation}}, {name => $2, id => $5, tag => $1, start => $3, end => $5, label => $6, refers_to => 0});
        }

        while ($_ =~ s/<(\w+) start="(\d+)" end="(\d+)" id="(\d+)" label="([^"]+)">//) {
            push(@{$self->{annotation}}, {id => $4, tag => $1, start => $2, end => $3, label => $5, refers_to => 0});
        }
    }
    close(FILE);

    foreach my $from (keys %refersto) {
        push(@{$self->{relation}}, {arg1 => $id2label{$refersto{$from}}, arg2 => $id2label{$from}});
        #print "\tRELATION FROM $from ($id2label{$from}) TO $refersto{$from} ($id2label{$refersto{$from}})\n";
    }

    #print STDERR "\tNumber of parsed annotations: " . scalar(@{$self->{annotation}}) . "\n";
    #print STDERR "\tNumber of parsed relations: " . scalar(@{$self->{relation}}) . "\n";

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

            my $exists = 0;
            foreach my $ann (@results) {   
                if ($ann->{tag} eq $annotation->{tag}) {
                    $exists = 1;
                    last;
                }
            }

            if (!$exists) {
                push(@results, {tag => "$annotation->{tag}", id => "$annotation->{id}", label => $annotation->{label}, start => $annotation->{start}, end => $annotation->{end}, name => $annotation->{name}});
            }
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

## Zmazeme anotacie, ktore sa zle prekryvaju (nesplnaju spravne uzatvorkovanie)
sub checkAnnotations {
    my ($self) = @_;

    RESTART:
    for (my $a1 = 0; $a1 < scalar(@{$self->{annotation}}); $a1++) {
        for (my $a2 = 0; $a2 < scalar(@{$self->{annotation}}); $a2++) {
            if ($self->{annotation}[$a1]->{start} < $self->{annotation}[$a2]->{start} and
                $self->{annotation}[$a1]->{end} > $self->{annotation}[$a2]->{start} and
                $self->{annotation}[$a1]->{end} < $self->{annotation}[$a2]->{end}) {
                print STDERR "\tKolizna anotacia: $self->{annotation}[$a1]->{label} <> $self->{annotation}[$a2]->{label}\n";
                splice(@{$self->{annotation}}, $a2, 1);
                goto RESTART;
            }
        }
    }
}

sub merge {
    my ($self, $txt_file, @input_files) = @_;

    #print STDERR "merge(@_)\n";

    $self->{annotation} = [];
    $self->{refersto} = [];
    $self->{relation} = [];

    ## Nacitam anotacie zo vsetkych vstupnych suborov
    my @prefixes = ("A", "B", "C", "D");
    for (my $i = 0; $i < scalar(@input_files); $i++) {
        $self->loadAnnotationFromVXML($prefixes[$i], $input_files[$i]);
    }

    $self->checkAnnotations();

    #print "\n\n\nNove pole:\n";
    #foreach my $annotation (@{$self->{annotation}}) {
    #    print "$annotation->{label}\n";
    #}

    #return;

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
    
    open(FILE, "<$txt_file");
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
                my $name = defined($tag->{name}) ? "name=\"$tag->{name}\"" : "name=\"\"";
                $output .= "<$tag->{tag} $name id=\"$tag_id\" label=\"$tag->{label}\" refers_to=\"0\" start=\"$position\" end=\"$end_of_annotaion\">";
                $label2id{$tag->{label}} = $tag_id;
                $tag_id++;
                push(@zoznam_otvorenych_tagov, $tag->{tag});
                #print "Otvoril som tag $tag->{tag}. Fronta: @zoznam_otvorenych_tagov\n";
            }
    
            @previous_tags = @tag_names;
            $output .= "$char";
            $position++;
        }
    
        $line++;
    }
    close(FILE);
    
    ## Teraz prejdeme vystup este raz a doplnime data
    ## z anotacii relacii
    my $new_output = "";
    foreach my $line (split(/\n/, $output)) {
        #print STDERR "LINE = $line\n";
        while ($line =~ /<(\w+) name="([^"]*)" id="(\d+)" label="([A-Z]\d+)" refers_to="0" start="(\d+)" end="(\d+)">/) {
            my $tag = $1;
            my $name = $2;
            my $id = $3;
            my $label = $4;
            my $start = $5;
            my $end = $6;
            #print STDERR "\t LABEL = $label\n";

            my $refers_to = 0;
            foreach my $relation (@{$self->{relation}}) {
                #print STDERR "\t\t ARG1: '$relation->{arg1}'\t\tARG2: '$relation->{arg2}'\n";
                if ($relation->{arg2} eq "$label") {
                    $refers_to = $label2id{$relation->{arg1}};
                    #print STDERR "\t\t NASLI SME. $relation->{arg1} ~~> $label2id{$relation->{arg1}}\n";
                    last;
                }
            }
    
            #print STDERR "\t REFERS TO = $refers_to\n";
            #print STDERR "\n";
    
            if (!$refers_to) {
                $line =~ s/<(\w+) name="([^"]*)" id="(\d+)" label="([A-Z]\d+)" refers_to="0" start="(\d+)" end="(\d+)">/<$1 name="$2" id="$3" label="$4" start="$5" end="$6">/;
                next;
            }
    
            $line =~ s/<(\w+) name="([^"]*)" id="(\d+)" label="([A-Z]\d+)" refers_to="0" start="(\d+)" end="(\d+)">/<$1 name="$2" id="$3" label="$4" refers_to="$refers_to" start="$5" end="$6">/;
        }
        $new_output .= "$line\n";
    }
    
    return $new_output;
}

sub vxml_validation {
    my ($self, $input_file, $output_file) = @_;

    print STDERR "*** $input_file ***\n";

    my $position = 0;
    my %used_id = ();
    my %used_label = ();
    my $doc_with_positions = "";

    print STDERR "\tNacitanie suboru\n";
    
    open(INPUT_FILE, "<$input_file");
    binmode(INPUT_FILE, ":encoding(utf8)");
    while (<INPUT_FILE>) {
        chomp($_);

        ## Skip definition of XML
        if ($_ =~ /^<\?/) {
            next;
        }

        ## Remove "<Document>" tags
        $_ =~ s/<\/?Document>//;

        my $line_to_process = $_;
        my $processed_line = "";

        while ($line_to_process =~ /^(.*?)(<[^>]+>)(.*)/) {
            #print "BEFORE = $1\n";
            #print "TAG = $2\n";
            #print "AFTER = $3\n";

            my $before = $1;
            my $tag = $2;
            my $after = $3;

            $before =~ s/<[^>]+//g;
            $tag =~ s/(Zakon_CNR|Zakon_federalni|Vyhlaska|Narizeni_vlady)/Zakon/g;
            $tag =~ s/position="\d+"//;
            $tag =~ s/start="\d+"//;
            $tag =~ s/end="\d+"//;

            $used_label{$1} = 1 if ($tag =~ /label="T(\d+)"/);

            my $id = 0;
            if ($tag =~ /id="(\d+)"/) {
                $used_id{$1} = 1;
                $id = $1;
            }

            $position += length($before);
            my $tag_position = $position;

            $tag =~ s/>/ position="$tag_position">/;

            $processed_line .= $before . $tag;
            $line_to_process = $after;
        }

        $position += length($line_to_process) + 1;

        $processed_line .= $line_to_process;
        $doc_with_positions .= "$processed_line\n";
    }

    print STDERR "\tOprava zlych anotacii\n";

    ## Tu skusim opravit zle anotacie, (ak anotacia konci v strede tokenu)...
    my $corrected_doc = "";
    foreach my $line (split(/\n/, $doc_with_positions)) {
        while ($line =~ /<\/(\w+)\s*position="(\d+)">([\w\-]+)/) {
            my $tag_name = $1;
            my $position = $2;
            my $text = $3;

            $position += length($text);

            #print "POVODNE: $line\n";
            $line =~ s/<\/$tag_name\s*position="\d+">([\w\-]+)/$1<\/$tag_name position="$position">/;
            #print "TERAZ: $line\n\n";
        }
        $corrected_doc .= "$line\n";
    }

    #print "\n\n\n***********************************************************************************************\n\n\n";
    #print $doc_with_positions;
    #print "\n\n\n***********************************************************************************************\n\n\n";

    print STDERR "\tPosition\n";
    
    ## Transform position to start/end notation
    my $doc_with_startsends = "";
    foreach my $line (split(/\n/, $corrected_doc)) {
        #my $cyklus = 0;
        while ($line =~ /<(\w+)([^>]*?)\s*position="\d+">/) {
            #$cyklus++;
            my $tag_name = $1;
            $line =~ s/<$tag_name([^>]*?)\s*position="(\d+)">(.*?)<\/$tag_name position="(\d+)">/<$tag_name$1 start="$2" end="$4">$3<\/$tag_name>/;
            #if ($cyklus > 100) {
            #    print "\t\tLINE = $line\n";
            #    exit 1;
            #}
        }
        $doc_with_startsends .= "$line\n";
    }

    #print "\n\n\n***********************************************************************************************\n\n\n";
    #print $doc_with_startsends;
    #print "\n\n\n***********************************************************************************************\n\n\n";

    print STDERR "\tStarts, ends\n";

    ## Add ids into attributes
    my $id = 0;
    foreach my $_id (keys %used_id) {
        if ($_id > $id) {
            $id = $_id;
        }
    }
    $id++;

    my $doc_with_ids = "";
    foreach my $line (split(/\n/, $doc_with_startsends)) {
        #print "TO PROCESS = $line\n";
        my $new_line = "";

        if ($line !~ /</) {
            $doc_with_ids .= "$line\n";
            next;
        }

        foreach my $part (split(/</, $line)) {
            if ($part =~ /^\//) {
                $new_line .= "<$part";
                next;
            }

            if ($part =~ /(.*?)>(.*)/) {
                my $text = $2;
                my $tag = $1;

                if ($tag =~ /id="(\d+)"/ and $1 > 0) {
                    $new_line .= "<$tag>$text";
                    next;
                }

                $tag =~ s/\s*id="\d*"//;
                $tag .= " id=\"$id\"";
                $id++;

                $new_line .= "<$tag>$text";
                next;
            }

            $new_line .= "$part";
        }
        #print "PROCESSED = $new_line\n";
        $doc_with_ids .= "$new_line\n";
    }

    #print "\n\n\n***********************************************************************************************\n\n\n";
    #print $doc_with_ids;
    #print "\n\n\n***********************************************************************************************\n\n\n";

    print STDERR "\tIds\n";

    ## Add ids into attributes
    my $label = 0;
    foreach my $_label (keys %used_label) {
        if ($_label > $label) {
            $label = $_label;
        }
    }
    $label++;

    my $doc_with_labels = "";
    foreach my $line (split(/\n/, $doc_with_ids)) {
        #print "TO PROCESS = $line\n";
        my $new_line = "";

        if ($line !~ /</) {
            $doc_with_labels .= "$line\n";
            next;
        }

        foreach my $part (split(/</, $line)) {
            if ($part =~ /^\//) {
                $new_line .= "<$part";
                next;
            }

            if ($part =~ /(.*?)>(.*)/) {
                my $text = $2;
                my $tag = $1;

                if ($tag =~ /label="T(\d+)"/ and $1 > 0) {
                    $new_line .= "<$tag>$text";
                    next;
                }

                $tag =~ s/\s*label="T\d*"//;
                $tag .= " label=\"T$label\"";
                $label++;

                $new_line .= "<$tag>$text";
                next;
            }

            $new_line .= "$part";
        }
        #print "PROCESSED = $new_line\n";
        $doc_with_labels .= "$new_line\n";
    }

    print STDERR "\tRefers to\n";

    ## Add refers_to attributes
    my $doc_with_refersto = "";
    foreach my $line (split(/\n/, $doc_with_labels)) {
        my $new_line = "";

        if ($line !~ /</) {
            $doc_with_refersto .= "$line\n";
            next;
        }

        foreach my $part (split(/</, $line)) {
            if ($part =~ /^\//) {
                $new_line .= "<$part";
                next;
            }

            if ($part =~ /(.*?)>(.*)/) {
                my $text = $2;
                my $tag = $1;

                if ($tag =~ /refers_to="(\d+)"/ and $1 > 0) {
                    $new_line .= "<$tag>$text";
                    next;
                }

                $tag =~ s/\s*refers_to="\d*"//;
                $tag .= " refers_to=\"0\"";

                $new_line .= "<$tag>$text";
                next;
            }

            $new_line .= "$part";
        }

        $doc_with_refersto .= "$new_line\n";
    }

    print STDERR "\tOutput\n";
    
    ## VXML Header
    my $output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    $output .= "<Document>";
    $output .= $doc_with_refersto;
    $output .= "</Document>";

    open(OUTPUT_FILE, ">$output_file");
    binmode(OUTPUT_FILE, ":encoding(utf8)");
    print OUTPUT_FILE $output;
    close(OUTPUT_FILE);

    print STDERR "\tDone\n\n";

    return 1;
}

sub vxml2ann {
    my ($self, $vxml_file) = @_;
    my $output = "";

    ## Zapamatame si mapovanie id na label (pre relacie),
    ## potom samotne relacie a este id na name (aby sme priradili najblizsiu)
    ## instituciu...
    my %id2label = ();
    my %id2name = ();
    my %refers_to = ();

    ## Precislujeme labels nazad na format T\d+
    my $label_id = 0;

    ## Otvorime a nacitame XML
    open(XML_FILE, "<$vxml_file");
    binmode(XML_FILE, ":encoding(utf8)");
    while (<XML_FILE>) {
        chomp($_);

        ## Hladame tagy
        while ($_ =~ /<(\w+) ([^>]+)>/) {
            $label_id++;

            my $tag_name = $1;
            my $tag_attr = $2;

            my $name = $1 if ($tag_attr =~ /name="([^"]+)"/);
            my $start = $1 if ($tag_attr =~ /start="(\d+)"/);
            my $end = $1 if ($tag_attr =~ /end="(\d+)"/);
            my $refers_to = $1 if ($tag_attr =~ /refers_to="(\d+)"/);
            #my $label = $1 if ($tag_attr =~ /label="([A-Z]\d+)"/);
            my $label = "T$label_id";
            my $id = $1 if ($tag_attr =~ /id="(\d+)"/);

            $id2label{$id} = $label if (defined($label) and defined($id));
            $id2name{$id} = $name if (defined($name) and defined($id));

            $refers_to{$id}{arg} = $refers_to if (defined($refers_to) and defined($id) and $refers_to > 0);
            $refers_to{$id}{tag} = $tag_name if (defined($refers_to) and defined($id) and $refers_to > 0);

            if ($_ =~ /<$tag_name ([^>]+)>(.*?)<\/$tag_name>/) {
                my $text = $2;
                $text =~ s/<[^>]+>//g;

                $output .= "$label\t$tag_name $start $end\t$text\n";
            }

            $_ =~ s/<(\w+) ([^>]+)>//;
        }
    }
    close(XML_FILE);

    ## Doplnime relacie
    my $id = 0;
    foreach my $target_id (keys %refers_to) {
        $id++;

        #print STDERR "Hladam vztah pre ciel $target_id:\n";

        ## Pokusime sa najst znacku s rovnakym menom, ktora bude najblizsie
        my $source_name = $id2name{$refers_to{$target_id}{arg}};
        my $source_id = $target_id;

        #print STDERR "\tInformace o zdroji: meno = $source_name, id = $refers_to{$target_id}{arg}\n";

        if (defined($source_name)) {
            #print STDERR "\tMame meno, hladame najblizsi zdroj k id $target_id\n";

            my $previous_distance = 1;
            while (defined($id2label{$source_id - $previous_distance}) and
                   (!defined($id2name{$source_id - $previous_distance}) or
                   $id2name{$source_id - $previous_distance} ne $source_name)) {
                #print STDERR "\t\t$previous_distance: $id2label{$source_id - $previous_distance}, $id2name{$source_id - $previous_distance}\n";
                $previous_distance++;
            }
            #print STDERR "\tPrevious: $previous_distance\n";
            if (!defined($id2label{$source_id - $previous_distance})) {
                $previous_distance = 1000;
            }
            #print STDERR "\tPrevious: $previous_distance\n";

            my $afterward_distance = 1;
            while (defined($id2label{$source_id + $afterward_distance}) and
                   (!defined($id2name{$source_id + $afterward_distance}) or
                    $id2name{$source_id + $afterward_distance} ne $source_name)) {
                #print STDERR "\t\t$afterward_distance: $id2label{$source_id + $afterward_distance}, $id2name{$source_id + $afterward_distance}\n";
                $afterward_distance++;
            }
            #print STDERR "\tAfterwards: $afterward_distance\n";
            if (!defined($id2label{$source_id + $afterward_distance})) {
                $afterward_distance = 1000;
            }
            #print STDERR "\tAfterwards: $afterward_distance\n";

            $source_id = $previous_distance <= $afterward_distance ? $target_id - $previous_distance : $target_id + $afterward_distance;
        }
        else {
           $source_id = $refers_to{$target_id}{arg};
        }

        my $arg1 = $id2label{$source_id};
        my $arg2 = $id2label{$target_id};
        my $relation = $refers_to{$target_id}{tag} eq "Zkratka" ? "zkratka" : "vydavatel";

        $output .= "R$id\t$relation Arg1:$arg1 Arg2:$arg2\n";
    }

    #print STDERR "$output\n";
    
    return $output;
}

sub vxml2html {
    my ($self, $vxml_file) = @_;
    
    ## Tagset definition
    my @visualization = (
        #"Dokument           silver          white       Document",
        "Zakon              red             black       Act",
        #"Cenovy_vymer	    Navy	    white       Price",
        "Rozhodnuti_soudu   green           white       Court_decision",
        "Ucinnost	    yellow	    black       Effectiveness",
        "Instituce	    DarkGoldenRod   black       Institution",
        "Zkratka	    Aqua	    black       Abbreviation",
        #"Plne_zneni	    Aquamarine	    black       Full_meaning"
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
    open(FILE, "<$vxml_file");
    binmode(FILE, ":encoding(utf-8)");
    while (<FILE>) {
        chomp($_);

        #print "<xmp>$_</xmp>";
        
        foreach my $tag (keys %labels) {
            $_ =~ s/<$tag[^>]*>/<span style="background: $labels{$tag}{bg_colour}; color: $labels{$tag}{fg_colour}" title="$labels{$tag}{title}">/g;
            $_ =~ s/<\/$tag>/<\/span>/g;
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