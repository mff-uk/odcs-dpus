#!/usr/bin/perl

use strict;
use warnings;

if (scalar(@ARGV) != 3) {
    print STDERR "./split_data_for_cross-validation <SOURCE DIR> <CROSS DIR> <JOIN TRAIN DIR>\n";
    exit(1);
}

my $FOLDS = 10;
my $SOURCE = shift(@ARGV);
my $TEST = shift(@ARGV);
my $TRAIN = shift(@ARGV);

my @files = split(/\n/, `find $SOURCE -type f`);
for (my $i = 0; $i < scalar(@files); $i++) {
    my $file = $files[$i];
    $file =~ s/(?:.*\/)?([^\/]+)/$1/;

    my $fold = $i % $FOLDS;
    
    for (my $j = 0; $j < $FOLDS; $j++) {
        if ($fold == $j) {
            print "cp $SOURCE/$file $TEST/$fold/$file\n";
        }
        else {
            print "cp $SOURCE/$file $TRAIN/$j/$file\n";
        }
    }
}
