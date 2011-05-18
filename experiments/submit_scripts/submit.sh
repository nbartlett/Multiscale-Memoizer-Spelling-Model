#!/bin/sh
#submit.sh

#PBS -W group_list=hpcstats
#PBS -q batch
#PBS -l mem=4000mb
#PBS -l walltime=24:00:00
#PBS -m n

#PBS -o localhost:~/hpc_nsb/Multiscale/experiments/out/
#PBS -e localhost:~/hpc_nsb/Multiscale/experiments/err/

root=/hpc/stats/users/nsb2130
data=$root/data/aiw
jar_path=$root/Multiscale/code/java/MultiscaleMemoizerSpellingModel/target/multiscalememoizerspellingmodel-1.0-jar-with-dependencies.jar

p0=$data/aiw_spelling_corrupted_10.txt
p1=$root/Multiscale/experiments/out/aiw_spelling_corrupted_10.out
p2=$data/aiw.txt
p3=20000
p4=9759
p5=true
p6=4000
p7=true

java -cp $jar_path -Xmx3g -ea edu.columbia.stat.wood.multiscalememoizerspellingmodel.experiments.SpellingErrorAIW $p0 $p1 $p2 $p3 $p4 $p5 $p6 $p7 > out_sampleLik_sample_conc.out