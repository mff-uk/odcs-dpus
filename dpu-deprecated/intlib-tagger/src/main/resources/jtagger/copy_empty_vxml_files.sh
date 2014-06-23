for j in {0..9}; do
    for i in $j/test/gs/*; do
        subor=`echo $i | perl -e 'while (<>) {$_ =~ s/^(.*?\/?)([^\/]+)$/$2/; print $_;}'`
        echo $subor;
        cp ../experiments/train/empty_vxml/$subor $j/test/vxml/
    done
done
