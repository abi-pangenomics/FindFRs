# FindFRs
FindFRs is a Java implementation of the Frequented Regions algorithm presented at the ACM BCB 2017 conference: "Exploring Frequented Regions in Pan-Genoimc Graphs".
A _Frequented Region_ (FR) is a region in a pan-genome de Bruijn graph that is frequently traversed by a subset of the genome paths in the graph.
A path that contributes to an FR being frequent is called a _supporting path_.
The algorithm works by iteratively constructing FRs via hierarchical aglomerative clsutering and then traversing the hierarchy and selecting nodes that qualify as clusters according to the given parameters.
If you just want to run the program, download the file `FindFRs.jar` from the latest release.  If you would like to 
compile the source code, following the instructions below.  It is also possible to build the project within
Netbeans (http://netbeans.org)

## Building
You can build the project with [ant](http://ant.apache.org/).
The project dependencies are managed with [ivy](http://ant.apache.org/ivy/).  If
you don't have ivy, don't worry, the build process will automatically set up ivy
provided that you are online.

Ant is kind of like the Unix program `make`. There are a lot of build options,
but the most useful are to build the project run:

    ant

This will produce a collection of build artifacts in the `dist` folder.  To run
`FindFRs` from the project root, run

    java -jar dist/FindFRs.jar ...with FindFRs args...

To clean up build artifacts and temporary files run.

    ant clean

## Parameters
FindFRs has two required parameters: `alpha` and `kappa`.
`alpha` is the minimum fraction of the nodes in an FR that each subpath must contain.
This is referred to as the _penetrance_.
`kappa` is maximum insertion length (measured in base-pairs) that any supporting path may have.
This is referred to as the _maximum insertion_.

Additionally, there are two optional parameters: `minsup` and `minsize`.
`minsup` is the minimum number of genome paths that must meet the other parameters in order for a region to be considered frequent.
This is referred to as the _minimum support_.
`minsize` is the minimum size (measured in de Bruijn nodes) that an FR that meets the other parameters must be in order to be considered frequent.
This is referred to as the _minimum size_.

## De Bruijn Graphs
FrinFRs consumes de Bruijn graphs in the `dot` file format.
A `dot` file representation of a pan-genome De Bruijn graph can be constructed from a `fasta` using the one of the programs presented in the following works:
* "SplitMEM: a graphical algorithm for pan-genome analysis with suffix skips"
* "Efficient Construction of a Compressed de Bruijn Graph for Pan-Genome Analysis"

## Running
FindFRs can be run as follows:
```
    $ java -jar FindFRs.jar -d <dotFile> -f <faFile> -a <alpha> -k <kappa>
```
where `<dotFile>` is the pan-genome de Bruijn graph constructed for the `<faFile>`

## Output
FindFRs produces output in several files.  The files are stored in an output directory that is named based on the input .dot and .fasta files used.  The following files types are produced:

`.bed` : this file indicates the supporting subpath segments found for each FR in .bed format
`.dist.txt` : this file lists the support and average supporing path length of each FR
`.frs.txt` : this file lists the De Bruijn nodes that comprise each FR.
`.frpaths.txt` : this lists the FRs each fasta sequence passes through as it traverses the DB graph.
`.frs.txt` : this file lists the De Bruijn nodes that comprise each FR.
`.csfr.txt` : this file indicates for each fasta seqence, the frequency counts of all FRs that occured in the sequence

## Sample Input
To test FindFRs, two sample input sets are provided:

`ecoli.pan3.dot` : a dot file constructed for a simple ecoli test file (K = 10)
`ecoli.pan3.fa` : the corresponding fasta sequence file

`Yeast_10Genomes_k100.dot` : a dot file constructed for a simple ecoli test file (K = 100)
`Yeast_10Genomes_k100.fa` : the corresponding fasta sequence file


