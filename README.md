# Geometrica

## Objective
A geometry description and rendering library. Aims to provide a way of textually describing geometric constructions and generate drawings. Example uses could be some CAD applications and generating geometry problems for students utilizing compact textual descriptions.

## Features
- Interprets constructions using a geometric description language .
- Can form visualizations of constructions using the textual description.
- Can extract additional features of the cosntruction using queries, applying geometric rules.
- Can export geometric drawings and results as images.

## Internals
Internally, the tool uses numerical optimization (a modified BFGS algorithm) with custom constraints and multiple passes to generate a visual representation of systems. Internally, geometric systems are also represented as graphs for rule-based systematic feature extraction.

## Dependencies
The code uses the GraphStream library for generating and processing graphs.

