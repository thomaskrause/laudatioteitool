LAUDATIO TEI Tool
=================

This tool is intended as a swiss army knife for validating 
and manipulating TEI Header files that follow the subset as 
defined by the LAUDATIO project (http://www.laudatio-repository.org).

Per default version 7 of the LAUDATIO TEI Header files are used.

Usage
-----

General command line call:
java -jar teitool-<version>.jar [options] [output directory/file]

Options:
 -help                 Show help the text
 -merge <arg>          merge content from input directory into one TEI
                       header
 -config <arg>         Optional configuration file location
 -schemecorpus <arg>   Corpus header validation scheme location
 -schemedoc <arg>      Document header validation scheme location
 -schemeprep <arg>     Preparation header validation scheme location
 -split <arg>          split one TEI header into several header files
 -validate <arg>       Validate the file or directory given as argument

The scheme locations must be valid URIs and can bei either available via 
internet  (e.g. http://example.com/scheme.rng) or can be paths on your 
local system (e.g. file:///C:/MyData/scheme.rng).
Other arguments like the input or output files must be paths on you local 
computer (e.g. C:\MyData\MyHeader\output.xml).

Configuration file
==================

Instead of setting the "schemecorpus", "schemedoc" and "schemeprep" arguments
on the command line, you can also put them in a configuration file and
set the "-config" parameter.

Example:

schemecorpus=http://korpling.german.hu-berlin.de/schemata/laudatio/teiODD_LAUDATIODocument_Scheme7.rnc
schemedoc=http://korpling.german.hu-berlin.de/schemata/laudatio/teiODD_LAUDATIODocument_Scheme7.rng

