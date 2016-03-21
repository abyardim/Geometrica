# Geometrica

## Objective
Geometrica is a geometry description and rendering library. It aims to provide a way of textually describing geometric constructions and generate drawings. Example uses could be some CAD applications and generating geometry problems for students utilizing compact textual descriptions.

An example with the input description in the upper-left corener, and the generated rendering and solution:

![Example input](http://i.imgur.com/NNLDcvF.png "Input")
![Example rendering](http://i.imgur.com/y41i5dy.png "Rendering") 
![Example solution](http://i.imgur.com/HyZ0otj.png "Solution") 



## Features
- Interprets constructions written in a simple geometric description language.
- Can form visualizations of constructions using the textual description.
- Can extract additional features of the cosntruction using queries, applying geometric rules.
- Can export geometric drawings and results as images.

## Internals
Internally, the tool uses numerical optimization (a modified BFGS algorithm) with custom constraints and multiple passes to generate a visual representation of systems. Geometric systems are also transformed into graphs for rule-based systematic feature extraction.

## Dependencies
The code uses the GraphStream library for generating and processing graphs.

